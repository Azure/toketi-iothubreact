[![Maven Central][maven-badge]][maven-url]
[![Bintray][bintray-badge]][bintray-url]
[![Build][build-badge]][build-url]
[![Issues][issues-badge]][issues-url]
[![Gitter][gitter-badge]][gitter-url]

# IoTHubReact 
IoTHub React is an Akka Stream library that can be used to read data from 
[Azure IoT Hub](https://azure.microsoft.com/en-us/services/iot-hub/), via a 
reactive stream with asynchronous back pressure. Azure IoT Hub is a service
used to connect thousands to millions of devices to the Azure cloud.

A simple example on how to use the library is the following:
```scala
new IoTHub().source()
    .map(m => jsonParser.readValue(m.contentAsString, classOf[Temperature]))
    .filter(_.value > 100)
    .to(console)
    .run()
```    
A stream of incoming telemetry data is read, parsed and converted to a 
`Temperature` object and filtered based on the temperature value. 


A more interesting example is reading telemetry data from Azure IoTHub, and
sending it to a Kafka topic, so it can be consumed by other services downstream:
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
 
IoTHub.source()
    .map(m => jsonParser.readValue(m.contentAsString, classOf[Temperature]))
    .filter(_.value > 100)
    .runWith(kafkaProducer.getSink())
```    

The library supports also reading from a single 
[IoTHub partition](https://azure.microsoft.com/en-us/documentation/articles/event-hubs-overview), 
so a service that consumes IoTHub events can create multiple streams and
process them independently.
```scala
val partitionNumber = 1
IoTHub.source(partitionNumber)
    .map(m => jsonParser.readValue(m.contentAsString, classOf[Temperature]))
    .filter(_.value > 100)
    .to(console)
    .run()
```    

## Build configuration
IoTHubReact is available on Maven Central, you just need to add the following 
reference in your `build.sbt` file:

```scala
libraryDependencies ++= {
  val iothubReactV = "0.6.0"
  
  Seq(
    "com.microsoft.azure.iot" %% "iothub-react" % iothubReactV
  )
}
```

or this dependency in `pom.xml` file if working with Maven:

```xml
<dependency>
    <groupId>com.microsoft.azure.iot</groupId>
    <artifactId>iothub-react_2.11</artifactId>
    <version>0.6.0</version>
</dependency>
```

### IoTHub configuration
IoTHubReact uses a configuration file to fetch the parameters required to 
connect to Azure IoTHub.  
The exact values to use can be found in the [Azure Portal](https://portal.azure.com):

* **namespace**: it is the first part of the _Event Hub-compatible endpoint_,
  which usually has this format: `sb://<namespace>.servicebus.windows.net/`
* **name**: see _Event Hub-compatible name_
* **keyname**: usually the value is `service`
* **key**: the Primary Key can be found under 
  _shared access policies/service_ policy (it's a base64 encoded string)

The values should be stored in your `application.conf` resource (or equivalent),
which can reference environment settings if you prefer.

```
iothub {
  namespace  = "<IoT hub namespace>"
  name       = "<IoT hub name>"
  keyName    = "<IoT hub key name>"
  key        = "<IoT hub key value>"
  partitions = <IoT hub partitions>
}
````

Example using environment settings:

```
iothub {
  namespace  = ${?IOTHUB_NAMESPACE}
  name       = ${?IOTHUB_NAME}
  keyName    = ${?IOTHUB_ACCESS_KEY_NAME}
  key        = ${?IOTHUB_ACCESS_KEY_VALUE}
  partitions = ${?IOTHUB_PARTITIONS}
}
````

There are other settings, to tune performance and connection details:

* **consumerGroup**: the 
  [consumer group](https://azure.microsoft.com/en-us/documentation/articles/event-hubs-overview)
  used during the connection
* **receiverBatchSize**: the number of messages retrieved on each call to 
  Azure IoT hub. The default (and maximum) value is 999.
* **receiverTimeout**: timeout applied to calls while retrieving messages. The
  default value is 3 seconds.

## Samples
In order to run the samples you need to have either the IoTHub settings defined
via environment variables, or in the config file. Follow the instructions in
the previous section on how to set the correct values.

* [`samples-scala`](samples-scala/src/main/scala):
  You can use `sbt run` to run the demos.
* [`samples-java`](samples-java/src/main/java): 
  You can use 
  `mvn clean compile exec:java -Dexec.mainClass="DisplayMessages.Demo"`
  to run the demo app.

## Future work
* improve asynchronicity by using EventHub SDK async APIs
* add support for checkpointing so the library can be used in scenarios where
  the consuming service needs to be resilient to crashes/restarts
* add Sink for Cloud2Device scenarios. `IoTHub.Sink` will allow cloud services
  to send messages to devices (via Azure IoTHub)

# Contribute Code

If you want/plan to contribute, we ask you to sign a 
[CLA](https://cla.microsoft.com/) (Contribution license Agreement). A friendly
bot will remind you about it when you submit a pull-request.

If you are sending a pull request, we kindly request to check the code style
with IntelliJ IDEA, importing the settings from `Codestyle.IntelliJ.xml`.




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
