// Copyright (c) Microsoft. All rights reserved.

package DisplayMessages;

import akka.Done;
import akka.NotUsed;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.microsoft.azure.iot.iothubreact.javadsl.IoTHub;
import com.microsoft.azure.iot.iothubreact.IoTMessage;

import java.util.concurrent.CompletionStage;

/**
 * Retrieve messages from IoT hub and display the data in the console
 */
public class Demo extends ReactiveStreamingApp {

    static Source<IoTMessage, NotUsed> messagesFromAllPartitions;

    public static void main(String args[]) {

        // Source retrieving messages from one IoT hub partition (0 to N-1, where N is
        // defined at deployment time)
        //Source messagesFromOnePartition = new IoTHub().source(PARTITION);

        // Source retrieving messages from all IoT hub partitions
        Source messagesFromAllPartitions = new IoTHub().source();

        messagesFromAllPartitions
                .to(console())
                .run(streamMaterializer);
    }

    public static Sink<IoTMessage, CompletionStage<Done>> console() {
        return Sink.foreach(m -> System.out.println(m.deviceId() + ": " + m.contentAsString()));
    }
}
