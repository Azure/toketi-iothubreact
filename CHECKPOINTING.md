# Stream partitions offset checkpointing, aka saving the position of the stream

The library provides a mechanism to restart the stream from a recent *checkpoint*, to be resilient
to restarts and crashes. For each partition, the library saves the current *offset*, a value
pointing to the current stream position.

The stream position can be saved in two different ways: **out-of-process** or **post-execution**
with [at-least-once delivery semantics](http://getakka.net/docs/persistence/at-least-once-delivery).

When saving the stream position out-of-process, the **offsets** are saved periodically in a
storage table or blob, with a configured frequency. For example, the stream position can be
saved in Azure blobs, or in Cassandra, every 15 seconds and/or every 500 messages. The logic
performing the writes may save offset for messages that have yet to be processed, this is where
the configurable delay allows to manage this risk. The longer it takes to process a message,
the higher the risk of missing messages after a restart.

The post-execution checkpointing method requires more code on the part of a developer, however
it has the advantage of a reasonable expectation that all messages are processed, regardless of
crashes, restarts, and very long processing times. This is the recommended approach when missing
even a singe message is not acceptable.

### Out-of-Process Checkpointing

To store checkpoints in Azure blobs, using a time or threashold based logic, the configuration
looks like the following:

```
iothub-react{

  [... other settings ...]

  checkpointing {
    frequency = 15s
    countThreshold = 1000
    timeThreshold = 30s

    storage {
      rwTimeout = 5s
      namespace = "iothub-react-checkpoints"

      backendType = "AzureBlob"
      azureblob {
        lease = 15s
        useEmulator = false
        protocol = "https"
        account = "..."
        key = "..."
      }
    }
  }
}
```

To store checkpoints in Cassandra, the configuration looks like the following:

```
iothub-react{

  [... other settings ...]

  checkpointing {
    frequency = 15s
    countThreshold = 1000
    timeThreshold = 30s

    storage {
      rwTimeout = 5s
      namespace = "iothub_react_checkpoints"

      backendType = "Cassandra"
      cassandra {
        cluster = "localhost:9042"
        replicationFactor = 1
        username = "..."
        password = "..."
      }
    }
  }
}
```

We plan to allow plugging in custom storage backends, by implementing a simple
[interface](src/main/scala/com/microsoft/azure/iot/iothubreact/checkpointing/Backends/CheckpointBackend.scala)
to read and write the stream position. Let us know if you are interested!

The checkpointing feature is not enabled by default, so the library will not save the stream offsets
automatically. To use checkpointing, use the `saveOffsets` option when creating the stream:

```scala
val options = SourceOptions()
  .fromTime(java.time.Instant.now())
  .saveOffsets()

IoTHub().source(options)
    .map(m ⇒ jsonParser.readValue(m.contentAsString, classOf[Temperature]))
    .filter(_.value > 100)
    .to(console)
    .run()
```

# Post-Execution At-Least-Once Checkpointing

At-least-once delivery semantic (ALOS) guarantees require that stream offsets are checkpointed after, and
only after, the processing task is complete. This in turn requires a different approach to building Akka
streaming graphs. Note that all configuration options listed above apply to the system, except for those
governing frequency of saves (frequency, countThreshold, timeThreshold).

The following example illustrates how code with ALOS looks like:

```scala
val options = SourceOptions().fromSavedOffsets()
val hub = IoTHub()
hub.source(options)
    .map(m ⇒ jsonParser.readValue(m.contentAsString, classOf[Temperature]))
    .filter(_.value > 100)
    .via(console)
    .to(hub.offsetSink(32)) //32 is the parallelism used to make concurrent saves
    .run()
```

Note that in this graph, all processing occurs upstream of the offset save, and that we used the
`offsetSink` method to store the stream position.

# Checkpointing behavior

### Configuration

The following table describes the impact of the settings within the `iothub-react.checkpointing`
configuration block. For further information, you can also check the
[reference.conf](src/main/resources/reference.conf) file.

| Setting | Type | Example | Description |
|---------|------|---------|-------------|
| **frequency**           | duration             | 15s         | How often to check if the offset in memory should be saved to storage. The check is scheduled after at least one message has been received, for each partition individually. |
| **countThreshold**      | int                  | 1000        | How many messages to stream before saving the position. The setting is applied to each partition individually. The value should be big enough to take into account buffering and batching. |
| **timeThreshold**       | duration             | 60s         | In case of low traffic (i.e. when not reaching countThreshold), save a stream position older than this value.|
| storage.**rwTimeout**   | duration             | 5000ms      | How long to wait, when writing to the storage, before triggering a storage timeout exception. |
| storage.**namespace**   | string               | "mycptable" | The table/container which will contain the checkpoints data. When streaming data from multiple IoT Hubs, you can use this setting to use separate tables/containers. |
| storage.**backendType** | string or class name | "AzureBlob" | Currently "AzureBlob" and "Cassandra" are supported. The name of the backend, or the class FQDN, to use to write to the storage. This provides a way to inject custom storage logic. |

### Runtime

The following table describes the system behavior, based on **API parameters** and stored **state**.

| Checkpointing | Start point | Saved position | Behavior |
|:---:|:---:|:-------:|---|
| No  | No  | No      | The stream starts from the beginning
| No  | No  | **Yes** | The stream starts from the beginning, unless you use `fromSavedPosition`
| No  | Yes | No      | The stream starts from the 'start point' provided
| No  | Yes | **Yes** | The stream starts from the 'start point' provided
| Yes | No  | No      | The stream starts from the beginning
| Yes | No  | **Yes** | The stream starts from the beginning, unless you use `fromSavedPosition`
| Yes | Yes | No      | The stream starts from the 'start point' provided
| Yes | Yes | **Yes** | The stream starts from the saved position

Legend:
* **Checkpointing**: whether saving the stream offset is enabled (with `saveOffsets`)
* **Start point**: whether the client provides a starting position (date or offset) or ask for all
the events from the beginning
* **Saved position**: whether there is a position saved in the storage

### Edge cases

* Azure IoT Hub stores messages up to 7 days. It's possible that the position stored doesn't exist
  anymore. In such case the stream will start from the first message available.
* If the checkpoint position is ahead of the last available message, the stream will fail with an
  error. This can happen only with invalid configurations where two streams are sharing the
  same checkpoints.
