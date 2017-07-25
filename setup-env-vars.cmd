:: You can use this script to set the required environment variables when running samples and
:: tests from the command line.
::
:: The environment variables are used by the default configuration, however you can customize
:: the configuration to your needs if you don't want to use environment variables.
::
:: For more information about where to find the IoT Hub settings, see here:
::
:: * https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-create-through-portal#endpoints
:: * https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-java-java-getstarted
::
::
:: Example:
::
::  SETX IOTHUB_EVENTHUB_NAME                      "my-iothub-one"
::  SETX IOTHUB_EVENTHUB_ENDPOINT                  "sb://iothub-ns-myioth-75186-9fb862f912.servicebus.windows.net/"
::  SETX IOTHUB_EVENTHUB_PARTITIONS                4
::  SETX IOTHUB_ACCESS_CONNSTRING                  "HostName=mytest-test-iothub.azure-devices.net;SharedAccessKeyName=iothubowner;SharedAccessKey=AAAcccf1gHIJJJmn23o8PQrStUvWxyzAbc4dEFG5HOI="
::  SETX IOTHUB_CHECKPOINT_AZSTORAGE_ACCOUNT       "myazurestorage"
::  SETX IOTHUB_CHECKPOINT_AZSTORAGE_KEY           "AAAcccf1gHIJJJmn23o8PQrStUvWxyzAbc4dEFG5HOIJklMnopqR+StuVwxYzJjxsU6vnDeNTv7Ipqs8MaBcDE=="
::  SETX IOTHUB_CHECKPOINT_COSMOSDBSQL_CONNSTRING  "AccountEndpoint=https://abcdefghilmno.documents.azure.com:443/;AccountKey=AAAcccf1gHIJJJmn23o8PQrStUvWxyzAbc4dEFG5HOIJklMnopqR+StuVwxYzJjxsU6vnDeNTv7Ipqs8MaBcDE==;"
::

:: see: Endpoints ⇒ Messaging ⇒ Events ⇒ `Event Hub-compatible name`
SETX IOTHUB_EVENTHUB_NAME ""

:: see: Endpoints ⇒ Messaging ⇒ Events ⇒ `Event Hub-compatible endpoint`
SETX IOTHUB_EVENTHUB_ENDPOINT ""

:: see: Endpoints ⇒ Messaging ⇒ Events ⇒ Partitions
SETX IOTHUB_EVENTHUB_PARTITIONS ""

:: see: Shared access policies ⇒ key name ⇒ Connection string
SETX IOTHUB_ACCESS_CONNSTRING ""

:: When checkpointing in Azure Blob, this is the Azure Storage Account name
SETX IOTHUB_CHECKPOINT_AZSTORAGE_ACCOUNT ""

:: When checkpointing in Azure Blob, this is the Azure Storage Account secret key
SETX IOTHUB_CHECKPOINT_AZSTORAGE_KEY ""

:: When checkpointing in Azure CosmosDb SQL (DocumentDb), set this connection string
SETX IOTHUB_CHECKPOINT_COSMOSDBSQL_CONNSTRING ""
