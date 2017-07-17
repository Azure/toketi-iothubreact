// Copyright (c) Microsoft. All rights reserved.

package DisplayMessagesWithAcknowledgement;

import com.microsoft.azure.iot.iothubreact.MessageFromDevice;

public class Temperature {
    String deviceId;
    Double value;
    String time;
    MessageFromDevice passThrough;
}
