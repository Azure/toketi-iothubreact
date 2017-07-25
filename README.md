[![Maven Central][maven-badge]][maven-url]
[![Bintray][bintray-badge]][bintray-url]
[![Build][build-badge]][build-url]
[![Issues][issues-badge]][issues-url]
[![Gitter][gitter-badge]][gitter-url]

IoTHubReact
===========

IoTHub React is an Akka Stream library that can be used **to read events** from
[Azure IoT Hub](https://azure.microsoft.com/en-us/services/iot-hub/), via a **reactive stream** with
**asynchronous back pressure**, and **to send commands** to connected devices.
Azure IoT Hub is a service used to connect thousands to millions of devices to the Azure cloud.

The library can be used both in Java and Scala, providing a fluent DSL for both programming
languages, similarly to the approach used by Akka.

The following is a simple example showing how to use the library in Scala. A stream of incoming
telemetry data is read, parsed and converted to a `Temperature` object, and then filtered based on
the temperature value:

```scala
IoTHub().source()
    .map(m ⇒ parse(m.contentAsString).extract[Temperature])
    .filter(_.value > 100)
    .to(console)
    .run()
```

and the equivalent code in Java:

```java
TypeReference<Temperature> type = new TypeReference<Temperature>() {};

new IoTHub().source()
    .map(m -> (Temperature) jsonParser.readValue(m.contentAsString(), type))
    .filter(x -> x.value > 100)
    .to(console())
    .run(streamMaterializer);
```

The following shows how to send a command to devices connected to Azure IoT Hub, for instance when
the device is measuring a high temperature, this sends a command to "turn fan ON":

```scala
val turnFanOn  = MessageToDevice("Turn fan ON")

IoTHub()
    .source()
    .filter(MessageSchema("temperature"))
    .map(m ⇒ parse(m.contentAsString).extract[Temperature])
    .filter(_.value > 85)
    .map(t ⇒ turnFanOn.to(t.deviceId))
    .to(hub.sink())
```

#### Streaming from IoT hub to _any_

An interesting example is reading telemetry data from Azure IoT Hub, and sending it to a Kafka
topic, so that it can be consumed by other services downstream:

```scala
...
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.clients.producer.ProducerRecord
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer

case class KafkaProducer(bootstrapServer: String)(implicit val system: ActorSystem) {

  protected val producerSettings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
    .withBootstrapServers(bootstrapServer)

  def getSink() = Producer.plainSink(producerSettings)

  def packageMessage(elem: String, topic: String): ProducerRecord[Array[Byte], String] = {
    new ProducerRecord[Array[Byte], String](topic, elem)
  }
}
```

```scala
val kafkaProducer = KafkaProducer(bootstrapServer)

IoTHub().source()
    .map(m ⇒ parse(m.contentAsString).extract[Temperature])
    .filter(_.value > 100)
    .runWith(kafkaProducer.getSink())
```

## Source options

### IoT hub partitions

The library supports reading from a subset of
[partitions](https://azure.microsoft.com/en-us/documentation/articles/event-hubs-overview),
to enable the development of distributed applications. Consider for instance the scenario of a
client application deployed to multiple nodes, where each node processes independently a subset of
the incoming telemetry.

```scala
val p1 = 0
val p2 = 3

IoTHub().source(Seq(p1, p2))
    .map(m ⇒ parse(m.contentAsString).extract[Temperature])
    .filter(_.value > 100)
    .to(console)
    .run()
```

### Starting point

Unless specified, the stream starts from the beginning of the data present in each partition.
It's possible to start the stream from a given date and time too:

```scala
val start = java.time.Instant.now()

IoTHub().source(start)
    .map(m ⇒ parse(m.contentAsString).extract[Temperature])
    .filter(_.value > 100)
    .to(console)
    .run()
```

### Multiple options

`IoTHub().source()` provides a quick API to specify the start time or the partitions. To specify
more options, you can use the `SourceOptions` class, combining multiple settings:

```scala
val options = SourceOptions()
  .partitions(0,2,3)
  .fromTime(java.time.Instant.now())
  .withRuntimeInfo()
  .saveOffsets()

IoTHub().source(options)
    .map(m ⇒ parse(m.contentAsString).extract[Temperature])
    .filter(_.value > 100)
    .to(console)
    .run()
```

### Stream processing restart - saving the current position

The library provides a mechanism to restart the stream from a recent *checkpoint*, to be resilient
to restarts and crashes.
*Checkpoints* are saved automatically, with a configured frequency, on a storage provided.
For instance, the stream position can be saved every 30 seconds and/or every 500 messages
(the values are configurable), in a table in Cassandra, or in a DocumentDb Collection
(Azure CosmosDb SQL) or using Azure blobs.

Currently the position may be saved in two different ways. The first, simpler method is accomplished
by saving in a concurrent thread, delayed by time and/or count, depending
on the configuration settings. The second requires slightly more coding but allows the developer to
implement [at-least-once delivery semantics](http://www.cloudcomputingpatterns.org/at_least_once_delivery/),
due to the fact the offset saves can be included downstream of processing in your graph.

For more information about the checkpointing feature, [please read here](CHECKPOINTING.md).

## Build configuration

IoTHubReact is available in Maven Central for Scala 2.11 and 2.12. To import the library into your
project, add the following reference in your `build.sbt` file:

```libraryDependencies += "com.microsoft.azure.iot" %% "iothub-react" % "0.9.1"```

or this dependency in `pom.xml` file when working with Maven:

```xml
<dependency>
    <groupId>com.microsoft.azure.iot</groupId>
    <artifactId>iothub-react_2.12</artifactId>
    <version>0.9.1</version>
</dependency>
```

IoTHubReact internally uses some libraries like Azure IoT SDK, Azure Storage SDK, Akka etc.
If your project depends on these libraries too, your can override the versions, explicitly importing
the packages in your `build.sbt` and `pom.xml` files. If you encounter some incompatibility with
future versions of these, please let us know opening an issue, or sending a PR.

### IoTHub configuration

By default IoTHubReact uses an `application.conf` configuration file to fetch the parameters
required to connect to Azure IoT Hub. The connection and authentication values to use, can be found
in the [Azure Portal](https://portal.azure.com):

Properties required to receive Device-to-Cloud (D2C) messages:

* **hubName**: see `Endpoints` ⇒ `Messaging` ⇒ `Events` ⇒ `Event Hub-compatible name`
* **hubEndpoint**: see `Endpoints` ⇒ `Messaging` ⇒ `Events` ⇒ `Event Hub-compatible endpoint`
* **hubPartitions**: see `Endpoints` ⇒ `Messaging` ⇒ `Events` ⇒ `Partitions`
* **accessConnString**: see `Shared access policies` ⇒ `key name`

Properties required to send Cloud-to-Device (C2D) commands:

* **accessConnString**: see `Shared access policies` ⇒ `key name`

The values should be stored in your `application.conf` resource (or equivalent). Optionally you can
reference environment settings if you prefer, for example to hide sensitive data.

```
iothub-react {

  connection {
    hubName          = "<Event Hub compatible name>"
    hubEndpoint      = "<Event Hub compatible endpoint>"
    hubPartitions    = <the number of partitions in your IoT Hub>
    accessConnString = "<access policy connection string>"
  }
}
````

Example using environment settings:

```
iothub-react {

  connection {
    hubName          = ${?IOTHUB_EVENTHUB_NAME}
    hubEndpoint      = ${?IOTHUB_EVENTHUB_ENDPOINT}
    hubPartitions    = ${?IOTHUB_EVENTHUB_PARTITIONS}
    accessConnString = ${?IOTHUB_ACCESS_CONNSTRING}
  }
}
````

Note that the library will automatically use these exact environment variables, unless overridden
in your configuration file (all the default settings are stored in
[reference.conf](src/main/resources/reference.conf)).

Although using a configuration file is the preferred approach, it's also possible to inject a
different configuration at runtime, providing an object implementing the `IConfiguration` interface.

The logging level can be managed overriding Akka configuration, for example:

```
akka {
  # Options: OFF, ERROR, WARNING, INFO, DEBUG
  loglevel = "WARNING"
}
```

There are other settings, to tune performance and connection details:

* **streaming.consumerGroup**: the
  [consumer group](https://azure.microsoft.com/en-us/documentation/articles/event-hubs-overview)
  used during the connection
* **streaming.receiverBatchSize**: the number of messages retrieved on each call to Azure IoT hub.
  The default (and maximum) value is 999.
* **streaming.receiverTimeout**: timeout applied to calls while retrieving messages. The default
  value is 3 seconds.
* **streaming.retrieveRuntimeInfo**: when enabled, the messages returned by `IoTHub.Source` will
  contain some runtime information about the last message in each partition. You can use this
  information to calculate how many telemetry events remain to process.

The complete configuration reference (and default values) is available in
[reference.conf](src/main/resources/reference.conf).

Samples
========

The project includes several demos in Java and Scala, showing some of the use cases and how IoThub
React API works. All the demos require an instance of Azure IoT hub, with some devices and messages.

1. **DisplayMessages** [Java]: how to stream Azure IoT hub withing a Java application, filtering
   temperature values greater than 60C
1. **SendMessageToDevice** [Java]: how to turn on a fan when a device reports a temperature higher
   than 22C
1. **AllMessagesFromBeginning** [Scala]: simple example streaming all the events in the hub.
1. **OnlyRecentMessages** [Scala]: stream all the events, starting from the current time.
1. **OnlyTwoPartitions** [Scala]: shows how to stream events from a subset of partitions.
1. **MultipleDestinations** [Scala]: shows how to read once and deliver events to multiple destinations.
1. **FilterByMessageSchema** [Scala]: how to filter events by message schema. Note: the name of the
   schema must be set by the device using the `$$MessageSchema` message property. In future this
   will be a system property, explicitly supported by Azure IoT SDK.
1. **FilterByDeviceID** [Scala]: how to filter events by device ID. The device ID is automatically
   set by Azure IoT SDK.
1. **CloseStream** [Scala]: show how to close the stream
1. **SendMessageToDevice** [Scala]: shows the API to send messages to connected devices.
1. **PrintTemperature** [Scala]: stream all Temperature events and print data to the console.
1. **Throughput** [Scala]: stream all events and display statistics about the throughput.
1. **Throttling** [Scala]: throttle the incoming stream to a defined speed of events/second.
1. **Checkpoint_While_Processing** [Scala]: demonstrates how the stream can be restarted without
    losing its position, using an optimistic approach (the position is stored in parallel, with some
    configurable delay fomr the moment of processing). The current position is stored in a Cassandra
    table (we suggest to run a docker container for the purpose of the demo,
    e.g. `docker run -ip 9042:9042 --rm cassandra`).
1. **Checkpoint_After_Processing** [Scala]: demonstrates how the stream can be restarted without
    losing its position, using At Least Once Delivery semantic (this guarantees that every event
    is delivered at least once, regardless of frequency and crashes).The current position is stored
    in a Cassandra table (we suggest to run a docker container for
    the purpose of the demo, e.g. `docker run -ip 9042:9042 --rm cassandra`).
1. **StartFromStoredOffsetsButDontWriteNewOffsets** [Scala]: shows how to use the saved checkpoints
    to start streaming from a known position, without changing the value in the storage. If the
    storage doesn't contain checkpoints, the stream starts from the beginning.
1. **StartFromStoredOffsetsIfAvailableOrByTimeOtherwise** [Scala]: similar to the previous
    demo, with a fallback datetime when the storage doesn't contain checkpoints.
1. **StreamIncludingRuntimeInformation** [Scala]: shows how runtime information works.
1. **SendMessageToDevice** [Scala]: another example showing how to send 2 different messages to
    connected devices.

We provide a [device simulator](tools/devices-simulator/README.md) in the tools section,
which will help simulating some devices sending sample telemetry events.

When ready, you should either edit the `application.conf` configuration files
([scala](samples-scala/src/main/resources/application.conf) and
[java](samples-java/src/main/resources/application.conf))
with your credentials, or set the corresponding environment variables.
Follow the instructions described in the previous section on how to set the correct values.

The root folder includes also a script showing how to set the environment variables in
[Linux/MacOS](setup-env-vars.sh) and [Windows](setup-env-vars.cmd).

The demos can be executed using the scripts included in the root folder (`run_<language>_samples.sh`
and `run_<language>_samples.cmd`):

* [`run_scala_samples.sh`](run_scala_samples.sh): execute Scala demos
* [`run_java_samples.sh`](run_java_samples.sh): execute Java demos


Running the tests
=================

You can use the included `build.sh` script to execute all the unit and functional tests in the suite.

The functional tests require an existing Azure IoT Hub resource, that yous should setup. For the
tests to connect to your IoT Hub, configure your environment using the `setup-env-vars.*` scripts
mentioned above in this page.


Other docs
==========

* [Contributing](CONTRIBUTING.md)
* [Checkpointing](CHECKPOINTING.md)
* [API specs](https://azure.github.io/toketi-iothubreact)






[maven-badge]: https://img.shields.io/maven-central/v/com.microsoft.azure.iot/iothub-react_2.11.svg
[maven-url]: http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22iothub-react_2.11%22
[bintray-badge]: https://img.shields.io/bintray/v/microsoftazuretoketi/toketi-repo/iothub-react.svg
[bintray-url]: https://bintray.com/microsoftazuretoketi/toketi-repo/iothub-react
[build-badge]: https://img.shields.io/travis/Azure/toketi-iothubreact.svg
[build-url]: https://travis-ci.org/Azure/toketi-iothubreact
[issues-badge]: https://img.shields.io/github/issues/azure/toketi-iothubreact.svg?style=flat-square
[issues-url]: https://github.com/azure/toketi-iothubreact/issues
[gitter-badge]: https://img.shields.io/gitter/room/azure/toketi-repo.js.svg
[gitter-url]: https://gitter.im/azure-toketi/iothub-react
