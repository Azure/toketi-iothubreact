# Stream position checkpoint

The library provides a mechanism to restart the stream from a recent *checkpoint*, to be resilient
to restarts and crashes.

*Checkpoints* are saved automatically, with a configured frequency, on a storage provided.

For instance, the stream position can be saved in Azure blobs, or in Cassandra, every 15 seconds or 
more (the value is configurable), or every N messages.

Currently **at-least-once** behavior is not supported, because the position is saved concurrently 
(although delayed), so it's possible that the position saved is ahead of your logic processing each 
event. We plan to support *at-least-once*, guaranteeing that the position saved is always equal
or behind the one already processed.

To store checkpoints in Azure blobs the configuration looks like the following:

```
iothub-react{

  [... other settings ...]
  
  checkpointing {
    enabled = true
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
    enabled = true
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

The checkpointing feature must be enabled in the configuration, however, the library will not save 
the position automatically, unless instructed. To use checkpointing, use the `savePosition` option:

```scala
val options = SourceOptions()
  .fromTime(java.time.Instant.now())
  .savePosition()

IoTHub().source(options)
    .map(m ⇒ jsonParser.readValue(m.contentAsString, classOf[Temperature]))
    .filter(_.value > 100)
    .to(console)
    .run()
```

# Checkpointing behavior

### Configuration

The following table describes the impact of the settings within the `iothub-react.checkpointing` 
configuration block. For further information, you can also check the 
[reference.conf](src/main/resources/reference.conf) file.

| Setting | Type | Example | Description |
|---------|------|---------|-------------|
| **enabled**             | bool                 | true        | Global switch to enable/disable the checkpointing feature. The "savePosition" option works only when this is enabled. |
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
* **Checkpointing**: whether checkpointing (saving the stream position) is enabled or disabled
* **Start point**: whether the client provides a starting position (date or offset) or ask for all 
the events from the beginning
* **Saved position**: whether there is a position saved in the storage 

### Edge cases

* Azure IoT Hub stores messages up to 7 days. It's possible that the position stored doesn't exist
  anymore. In such case the stream will start from the first message available.
* If the checkpoint position is ahead of the last available message, the stream will fail with an
  error. This can happen only with invalid configurations where two streams are sharing the 
  same checkpoints.
