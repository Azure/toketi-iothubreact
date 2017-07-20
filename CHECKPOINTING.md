Stream partitions offset checkpointing, i.e. saving the position of the stream
==============================================================================

The library provides a mechanism to restart the stream from a recent *checkpoint*, to be resilient
to restarts and crashes. For each partition, the library saves the current *offset*, a value
pointing to the current stream position. Currently, the library supports checkpoints stored either
in Azure Blobs, CosmosDb SQL (DocumentDb) or Cassandra.

## Checkpoint storage

To store checkpoints in Azure blobs, the configuration looks like the following.
The namespace is used as the name for the Blob container.

```
iothub-react{

  checkpointing {

    storage {

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

To store checkpoints in CosmosDB SQL (DocumentDb), the configuration looks like the following.
The namespace is used as the name for the Database and the Collection, which are created
automatically.

```
iothub-react{

  checkpointing {

    storage {

      namespace = "iothub-react-checkpoints"

      backendType = "CosmosDbSQL"

      cosmosdbsql {
        connString = "AccountEndpoint=https://_______.documents.azure.com:443/;AccountKey=_______;"
      }
    }
  }
}
```

To store checkpoints in Cassandra, the configuration looks like the following.
The namespace is used as the name for the Keyspace container.

```
iothub-react{

  checkpointing {

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

For a full configuration detail, see the Configuration section below.

The checkpointing feature is not enabled by default, so the library will not save the stream offsets
automatically. To use checkpointing with automatic commits, use the `saveOffsetsOnPull` option when
creating the stream:

```scala
val options = SourceOptions().saveOffsetsOnPull()

IoTHub().source(options)
    .map( /* ... */ )
    .filter( /* ... */ )
    .to( /* ... */ )
    .run()
```

We plan to allow plugging in custom storage backends, by implementing a simple
[interface](src/main/scala/com/microsoft/azure/iot/iothubreact/checkpointing/Backends/CheckpointBackend.scala)
to read and write the stream position. Let us know if you are interested!

Checkpointing logic
===================

The stream position (also known as "offset" or "offsets") can be saved in two different ways:
**out-of-process** or **post-execution** with
[at-least-once delivery semantics](http://getakka.net/docs/persistence/at-least-once-delivery).

When saving the stream position out-of-process, the **offset** is saved periodically, with a
configured frequency. This approach is quite simple, one only needs to enable checkpointing with
one line of code, then the library takes care of all the details. This is the recommended approach
when processing the last message in the stream is more important, and the risk of skipping some old
event is acceptable.

The post-execution checkpointing method instead, requires particular attention on the
part of a developer, with some extra code. However it has the advantage of a reasonable expectation
that all messages are processed, regardless of crashes, restarts, and very long processing times.
This is the recommended approach when missing even a singe message is not acceptable.

In order to reduce storage operations, both methods support delayed writes. The timing configuration
looks like the following:

```
iothub-react{

  checkpointing {
    frequency = 15s
    countThreshold = 1000
    timeThreshold = 30s
  }
}
```

In the example above, the stream offsets will be saved at least every 15 seconds, OR at least after
1000 messages. For a full configuration detail, see the Configuration section below.

## Out-of-Process Checkpointing

As an example, IoT Hub React can be configured to save the stream position once every 15 seconds,
and/or every 500 messages (values are only an example).
However, the logic performing the writes doesn't know if all messages have been processed, and it
might save a position ahead of the actual stream processing. This might happen for example, when
each message takes seconds to be processed, due to some external dependency.

The configurable delay allows to manage this risk. For instance, saving the position less
frequently, once every 5 minutes, will reduce the risk of skipping messages in case of a restart.

The out-of-process checkpointing is useful when the risk of skipping messages is acceptable, for
instance when processing the last message in the stream is more important than processing each
individual event.

To use out-of-process checkpointing, just use the `saveOffsetsOnPull` option:

```scala
val options = SourceOptions().saveOffsetsOnPull()

IoTHub().source(options)
    .map( /* ... */ )
    .filter( /* ... */ )
    .to( /* ... */ )
    .run()
```

## Post-Execution Checkpointing, i.e. At-Least-Once Delivery

At-least-once delivery semantic (ALOS) guarantees that stream offsets are saved after, and only
after, the processing task is complete. This in turn requires a different approach
to building Akka streaming graphs. All configuration options listed above still apply, but the code
needs some extra logic to save the the stream offset **after** processing the messages.

The most important coding aspect, is that regardless of messages transformation (e.g.
using `map()`), the original message needs to be carried through the streaming graph, and passed
in input to the `offsetSaveSink` which saves the stream offset.

The following example illustrates how the code looks like. The example maps the original stream
message to a `TemperatureWithPassThrough` object, filters out temperatures lower than 100, outputs
the temperature to the console, and finally we saves the position.

```scala
case class TemperatureWithPassThrough(
  temp: Temperature,
  passThrough: MessageFromDevice) // hold the original message

val options = SourceOptions().fromSavedOffsets()
val hub = IoTHub()

hub.source(options)
    .map(m ⇒ TemperatureWithPassThrough(jsonParser.readValue(m.contentAsString, classOf[Temperature]), m))
    .filter(_.temp.value > 100)
    .map { in ⇒
      println(s"Temperature: ${in.temp}")
      in.passThrough // return the original message
    }
    .to(hub.offsetSaveSink()) // save the offset
    .run()
```

Things to note about the example:
* all processing occurs upstream of the offset save
* the `TemperatureWithPassThrough` class holds a copy of the original message
* the `map` function returns the original message
* `hub.offsetSaveSink()` is the last step in the flow

Checkpointing behavior
======================

### Configuration

The following table describes the impact of the settings within the `iothub-react.checkpointing`
configuration block. For further information, you can also check the
[reference.conf](src/main/resources/reference.conf) file.

| Setting | Type | Example | Description |
|---------|------|---------|-------------|
| **frequency**           | duration             | 15s         | How often to check if the offset in memory should be saved to storage. The check is scheduled after at least one message has been received, for each partition individually. |
| **countThreshold**      | int                  | 1000        | How many messages to stream before saving the position. The setting is applied to each partition individually. The value should be big enough to take into account buffering and batching. |
| **timeThreshold**       | duration             | 60s         | In case of low traffic (i.e. when not reaching countThreshold), save a stream position older than this value. |
| storage.**rwTimeout**   | duration             | 5000ms      | How long to wait, when writing to the storage, before triggering a storage timeout exception. |
| storage.**namespace**   | string               | "mycptable" | The table/container which will contain the checkpoints data. When streaming data from multiple IoT Hubs, you can use this setting to use separate tables/containers. |
| storage.**backendType** | string or class name | "AzureBlob" | Currently "AzureBlob", "CosmosDbSQL" and "Cassandra" are supported. The name of the backend to use to write to the storage. |

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
* **Checkpointing**: whether saving the stream offset is enabled (with `saveOffsetsOnPull`)
* **Start point**: whether the client provides a starting position (date or offset) or ask for all
the events from the beginning
* **Saved position**: whether there is a position saved in the storage

### Edge cases

* Azure IoT Hub stores messages up to 7 days. It's possible that the position stored doesn't exist
  anymore. In such case the stream will start from the first message available.
* If the checkpoint position is ahead of the last available message, the stream will fail with an
  error. This can happen only with invalid configurations where two streams are sharing the
  same checkpoints.
