# Device simulator

The program simulates some Temperature and Humidity sensors. The values gradually
increase and decrease, within a range of 6 minutes, with random peaks.

# How to run the device simulator

First of all you should create an Azure IoT hub instance. You can create a free instance 
selecting the "F1 Free" scale tier.

Once you have an IoT hub ready, you should take note of:

* the **connection string** from the **Shared access policies** panel, for 
   the **device** policy.
    
## Create the devices

Creating devices is vey simple, thanks to the IoT hub explorer, which you can run from
the command line.

From here on you should use a terminal with Node.js and Node Package Manager (npm) ready to use.

To install IoT hub explorer, open a terminal and execute

```bash
npm install -g iothub-explorer
```

Once the IoT hub explorer is installed, proceed to create the devices:
 
* **Login, using the connection string obtained earlier:**

```bash
CONNSTRING="... device connection string ..."
iothub-explorer login '$CONNSTRING'
```

* **Create some devices**, e.g. using using a Bash terminal:

```bash
for i in {1000..1010}; do iothub-explorer create device$i --display="deviceId"; done
```

## Prepare the simulator

In order to run, the simulator needs the credentials for each device just created. 
The following command creates a `credentials.js` file with the settings required.

From the terminal, 'cd' into the same folder of this README document, and execute:

```bash
export CONNSTRING="... device connection string ..."
./download_credentials.sh
unset CONNSTRING
```

## Run the simulator

Open a bash terminal in the same folder of this README document and execute:

```bash
npm run send
```

The simulator will start sending data every second, for each device created.
