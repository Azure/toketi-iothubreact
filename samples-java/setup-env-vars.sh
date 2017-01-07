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
#  export IOTHUB_EVENTHUB_NAME="my-iothub-one"
#
#  export IOTHUB_EVENTHUB_ENDPOINT="sb://iothub-ns-myioth-75186-9fb862f912.servicebus.windows.net/"
#
#  export IOTHUB_EVENTHUB_PARTITIONS=4
#
#  export IOTHUB_IOTHUB_ACCESS_POLICY="service"
#
#  export IOTHUB_ACCESS_KEY="6XdRSFB9H61f+N3uOdBJiKwzeqbZUj1K//T2jFyewN4="
#
#  export IOTHUB_ACCESS_HOSTNAME="my-iothub-one.azure-devices.net"
#

# see: Endpoints ⇒ Messaging ⇒ Events ⇒ "Event Hub-compatible name"
export IOTHUB_EVENTHUB_NAME=""

# see: Endpoints ⇒ Messaging ⇒ Events ⇒ "Event Hub-compatible endpoint"
export IOTHUB_EVENTHUB_ENDPOINT=""

# see: Endpoints ⇒ Messaging ⇒ Events ⇒ Partitions
export IOTHUB_EVENTHUB_PARTITIONS=""

# see: Shared access policies, we suggest to use "service" here
export IOTHUB_IOTHUB_ACCESS_POLICY=""

# see: Shared access policies ⇒ key name ⇒ Primary key
export IOTHUB_ACCESS_KEY=""

# see: Shared access policies ⇒ key name ⇒ Connection string ⇒ HostName
export IOTHUB_ACCESS_HOSTNAME=""
