// Copyright (c) Microsoft. All rights reserved.

import scopt.OptionParser

case class Parameters(
    hubName: String = "",
    deviceId: String = "",
    accessKey: String = "",
    contentType: String = "",
    contentFormat: String = "",
    content: String = "",
    verbose: Boolean = false) {

  def build: OptionParser[Parameters] = {
    new scopt.OptionParser[Parameters]("device-to-cloud-send") {

      override def showUsageOnError = true

      head("device-to-cloud-send", "0.1.0")
      opt[String]('h', "hub").required.valueName("<hub name>").action((x, c) => c.copy(hubName = x)).text("IoT hub name")
      opt[String]('d', "device").required.valueName("<device ID>").action((x, c) => c.copy(deviceId = x)).text("IoT device ID")
      opt[String]('k', "key").required.valueName("<device access key>").action((x, c) => c.copy(accessKey = x)).text("IoT device auth key")
      opt[String]('t', "type").valueName("<content type>").action((x, c) => c.copy(contentType = x)).text("Message type, e.g. temperature, humidity etc.")
      opt[String]('f', "format").valueName("<content format>").action((x, c) => c.copy(contentFormat = x)).text("Message format, e.g. json")
      opt[Unit]('v', "verbose").action((_, c) => c.copy(verbose = true)).text("Verbose flag")
      arg[String]("<content>").unbounded().required().action((x, c) => c.copy(content = x)).text("Message content")

      help("help").text("Prints this usage text")
      note("\nAzure IoT hub tools - https://github.com/Azure/toketi-iothubreact")
    }
  }
}
