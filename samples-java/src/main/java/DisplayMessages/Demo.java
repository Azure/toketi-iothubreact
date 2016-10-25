// Copyright (c) Microsoft. All rights reserved.

package DisplayMessages;

import akka.Done;
import akka.NotUsed;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.iot.iothubreact.IoTMessage;
import com.microsoft.azure.iot.iothubreact.javadsl.IoTHub;
import com.microsoft.azure.iot.iothubreact.javadsl.IoTHubPartition;

import java.util.Map;
import java.util.concurrent.CompletionStage;

import static java.lang.System.out;

/**
 * Retrieve messages from IoT hub and display the data in the console
 */
public class Demo extends ReactiveStreamingApp {

    static ObjectMapper jsonParser = new ObjectMapper();

    public static void main(String args[]) {

        // Source retrieving messages from one IoT hub partition (e.g. partition 2)
        Source<IoTMessage, NotUsed> messagesFromOnePartition = new IoTHubPartition(2).source();

        // Source retrieving messages from all IoT hub partitions
        Source<IoTMessage, NotUsed> messagesFromAllPartitions = new IoTHub().source();

        messagesFromAllPartitions
                .filter(m -> m.model() == "temperature")
                .map(m -> parseTemperature(m))
                .filter(x -> x != null && x.value > 60)
                .to(console())
                .run(streamMaterializer);
    }

    public static Sink<Temperature, CompletionStage<Done>> console() {
        return Sink.foreach(m -> out.println("Device: " + m.deviceId + ": temperature: " + m.value));
    }

    public static Temperature parseTemperature(IoTMessage m) {
        try {
            Map<String, Object> hash = jsonParser.readValue(m.contentAsString(), Map.class);
            Temperature t = new Temperature();
            t.value = (Double) hash.get("value");
            t.deviceId = m.deviceId();
            return t;
        } catch (Exception e) {
            return null;
        }
    }
}
