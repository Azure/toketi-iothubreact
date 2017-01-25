#  Populate the following environment variables, and execute this file before running
#  IoT Hub to Cassandra.
#
#  For more information about where to find these values, more information here:
#
#  * https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-create-through-portal#endpoints
#  * https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-java-java-getstarted
#
#
#  Example:
#
#  $env:IOTHUB_EVENTHUB_NAME = 'my-iothub-one'
#
#  $env:IOTHUB_EVENTHUB_ENDPOINT = 'sb://iothub-ns-myioth-75186-9fb862f912.servicebus.windows.net/'
#
#  $env:IOTHUB_EVENTHUB_PARTITIONS = 4
#
#  $env:IOTHUB_IOTHUB_ACCESS_POLICY = 'service'
#
#  $env:IOTHUB_ACCESS_KEY = '6XdRSFB9H61f+N3uOdBJiKwzeqbZUj1K//T2jFyewN4='
#
#  SET IOTHUB_ACCESS_HOSTNAME = "my-iothub-one.azure-devices.net"
#

# see: Endpoints ⇒ Messaging ⇒ Events ⇒ "Event Hub-compatible name"
$env:IOTHUB_EVENTHUB_NAME = ''

# see: Endpoints ⇒ Messaging ⇒ Events ⇒ "Event Hub-compatible endpoint"
$env:IOTHUB_EVENTHUB_ENDPOINT = ''

# see: Endpoints ⇒ Messaging ⇒ Events ⇒ Partitions
$env:IOTHUB_EVENTHUB_PARTITIONS = ''

# see: Shared access policies, we suggest to use "service" here
$env:IOTHUB_IOTHUB_ACCESS_POLICY = ''

# see: Shared access policies ⇒ key name ⇒ Primary key
$env:IOTHUB_ACCESS_KEY = ''

# see: Shared access policies ⇒ key name ⇒ Connection string ⇒ HostName
$env:IOTHUB_ACCESS_HOSTNAME = ''
