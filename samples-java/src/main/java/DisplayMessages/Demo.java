// Copyright (c) Microsoft. All rights reserved.

package DisplayMessages;

import akka.Done;
import akka.NotUsed;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.iot.iothubreact.IoTMessage;
import com.microsoft.azure.iot.iothubreact.javadsl.IoTHub;

import java.util.concurrent.CompletionStage;

/**
 * Retrieve messages from IoT hub and display the data in the console
 */
public class Demo extends ReactiveStreamingApp {

    public static void main(String args[]) {

        // Source retrieving messages from one IoT hub partition (e.g. partition 2)
        //Source messagesFromOnePartition = new IoTHubPartition(2).source();

        // Source retrieving messages from all IoT hub partitions
        Source<IoTMessage, NotUsed> messagesFromAllPartitions = new IoTHub().source();

        // JSON parser setup
        ObjectMapper jsonParser = new ObjectMapper();

        TypeReference<Temperature> type = new TypeReference<Temperature>() {
        };

        messagesFromAllPartitions
                .map(m -> (Temperature) jsonParser.readValue(m.contentAsString(), type))
                .filter(x -> x.value > 100)
                .to(console())
                .run(streamMaterializer);
    }

    public static Sink<Temperature, CompletionStage<Done>> console() {
        return Sink.foreach(m -> System.out.println("Device: " + m.deviceId + ": temperature: " + m.value));
    }
}
