// Copyright (c) Microsoft. All rights reserved.

package DisplayMessagesWithAcknowledgement;

import akka.NotUsed;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.iot.iothubreact.MessageFromDevice;
import com.microsoft.azure.iot.iothubreact.SourceOptions;
import com.microsoft.azure.iot.iothubreact.javadsl.IoTHub;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Map;

import static java.lang.System.out;

/**
 * Retrieve messages from IoT hub and display the data in the console
 */
public class Main extends ReactiveStreamingApp
{
    static ObjectMapper jsonParser = new ObjectMapper();

    public static void main(String args[])
    {
        // Source retrieving messages from two IoT hub partitions (e.g. partition 0 and 3)
        int[] partitions = {0, 2};
        SourceOptions options = new SourceOptions().partitions(partitions);
        Source<MessageFromDevice, NotUsed> messagesFromTwoPartitions1 = new IoTHub().source(options);

        // Same, but different syntax using one of the shortcuts
        Source<MessageFromDevice, NotUsed> messagesFromTwoPartitions2 = new IoTHub().source(Arrays.asList(0, 3));

        IoTHub hub = new IoTHub();
        // Source retrieving from all IoT hub partitions for the past 24 hours
        Source<MessageFromDevice, NotUsed> messages = hub.source(Instant.now().minus(1, ChronoUnit.DAYS));

        messages
                .filter(m -> m.messageSchema().equals("temperature"))
                .map(m -> parseTemperature(m))
                .filter(x -> x != null && (x.value < 18 || x.value > 22))
                .via(console())
                .to(hub.offsetSink(32))
                .run(streamMaterializer);
    }

    public static Flow<Temperature, MessageFromDevice, NotUsed> console()
    {
        return Flow.of(Temperature.class).map(m -> {
                    if (m.value <= 18)
                    {
                        out.println("Device: " + m.deviceId + ": temperature too LOW: " + m.value);
                    } else
                    {
                        out.println("Device: " + m.deviceId + ": temperature to HIGH: " + m.value);
                    }
                    return m.passThrough;
                });
    }

    @SuppressWarnings("unchecked")
    public static Temperature parseTemperature(MessageFromDevice m)
    {
        try
        {
            Map<String, Object> hash = jsonParser.readValue(m.contentAsString(), Map.class);
            Temperature t = new Temperature();
            t.value = Double.parseDouble(hash.get("value").toString());
            t.deviceId = m.deviceId();
            t.passThrough = m;
            return t;
        } catch (Exception e)
        {
            return null;
        }
    }
}
