::  Populate the following environment variables, and execute this file before running
::  IoT Hub to Cassandra.
::
::  For more information about where to find these values, more information here:
::
::  * https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-create-through-portal#endpoints
::  * https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-java-java-getstarted
::
::
::  Example:
::
::  SET IOTHUB_EVENTHUB_NAME = "my-iothub-one"
::
::  SET IOTHUB_EVENTHUB_ENDPOINT = "sb://iothub-ns-myioth-75186-9fb862f912.servicebus.windows.net/"
::
::  SET IOTHUB_EVENTHUB_PARTITIONS = 4
::
::  SET IOTHUB_IOTHUB_ACCESS_POLICY = "service"
::
::  SET IOTHUB_ACCESS_KEY = "1Ab23456C78d+E9fOgH1234ijklMNo5P//Q6rStuwX7="
::
::  SET IOTHUB_ACCESS_HOSTNAME = "my-iothub-one.azure-devices.net"
::
::  SET IOTHUB_CHECKPOINT_ACCOUNT = 'myazurestorage'
::
::  SET IOTHUB_CHECKPOINT_KEY = "A0BcDef1gHIJKlmn23o8PQrStUvWxyzAbc4dEFG5HOIJklMnopqR+StuVwxYzJjxsU6vnDeNTv7Ipqs8MaBcDE=="
::

:: see: Endpoints ⇒ Messaging ⇒ Events ⇒ "Event Hub-compatible name"
SET IOTHUB_EVENTHUB_NAME = ""

:: see: Endpoints ⇒ Messaging ⇒ Events ⇒ "Event Hub-compatible endpoint"
SET IOTHUB_EVENTHUB_ENDPOINT = ""

:: see: Endpoints ⇒ Messaging ⇒ Events ⇒ Partitions
SET IOTHUB_EVENTHUB_PARTITIONS = ""

:: see: Shared access policies, we suggest to use "service" here
SET IOTHUB_IOTHUB_ACCESS_POLICY = ""

:: see: Shared access policies ⇒ key name ⇒ Primary key
SET IOTHUB_ACCESS_KEY = ""

:: see: Shared access policies ⇒ key name ⇒ Connection string ⇒ HostName
SET IOTHUB_ACCESS_HOSTNAME = ""

:: When using checkpoints stored in Azure Blob, this is the Azure Storage Account name
SET IOTHUB_CHECKPOINT_ACCOUNT = ""

:: When using checkpoints stored in Azure Blob, this is the Azure Storage Account secret key
SET IOTHUB_CHECKPOINT_KEY = ""
