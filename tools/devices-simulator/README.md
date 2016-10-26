# Device simulator

The program simulates some Temperature and Humidity sensors. The values gradually
increase and decrease, within a range of 6 minutes, with random peaks.

# How to run the device simulator

First of all you should create an Azure IoT hub instance. You can create a free instance 
selecting the "F1 Free" scale tier.

Once you have an IoT hub ready, you should take note of a couple of settings:

1. the **connection string** from hte **Shared access policies** panel, for 
   the **iothubowner** policy.
2. the **hub name**, which is the name you entered when creating the hub. You can find the name
   in the iothubowner connectionfrom, it's the part preceding ".azure-devices.net".
    
## Create the devices

Creating devices is extremely simple thanks to the IoT hub explorer, which you can run from
the command line.

From here on you should use a terminal with Node.js and Node Package Manager (npm) ready to use.

To install IoT hub explorer, open a terminal and execute

```bash
npm install -g iothub-explorer
```

Once you have IoT hub explorer installed, let's create the devices:
 
1. **Login, using the connection string obtained earlier:**

```bash
CONNSTRING="... iothubowner connection string ..."
iothub-explorer login '$CONNSTRING'
```

2. **Using a bash terminal, create some devices**

```bash
for i in {1000..1010}; do iothub-explorer create device$i --display="deviceId"; done
```

## Prepare the simulator

The simulator needs the credentials for each device just created. The following command will
create a `credentials.js` file with the settings required.

Open a bash terminal in the same folder of this README document and execute:

```bash
CONNSTRING="... iothubowner connection string ..."
DATA=$(iothub-explorer list --display="deviceId,authentication.SymmetricKey.primaryKey," --raw|sort|awk '{ print $0 ","}')
echo "var connString = '$CONNSTRING';" > credentials.js
echo "var hubDevices = [$DATA];" >> credentials.js
```

## Run the simulator

Open a bash terminal in the same folder of this README document and execute:

```bash
npm run send
```

At this point you should see that the simulator will be sending data every second, for
each device created.
