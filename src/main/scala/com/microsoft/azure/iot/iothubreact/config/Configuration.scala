// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.config

import com.microsoft.azure.iot.iothubreact.checkpointing.{CPConfiguration, ICPConfiguration}
import com.typesafe.config.{Config, ConfigFactory}

import scala.language.postfixOps

trait IConfiguration {

  // Connection/Authentication configuration
  val connect: IConnectConfiguration

  // Streaming configuration
  val streaming: IStreamConfiguration

  // Stream position checkpointing configuration
  val checkpointing: ICPConfiguration
}

object Configuration {

  def apply(): IConfiguration = new Configuration()

  def apply(configData: Config): IConfiguration = new Configuration(configData)
}

/** Hold IoT Hub React configuration settings
  *
  * @see https://github.com/typesafehub/config for information about the configuration file formats
  */
class Configuration(configData: Config) extends IConfiguration {

  // Parameterless ctor
  def this() = this(ConfigFactory.load)

  lazy val connect      : IConnectConfiguration = new ConnectConfiguration(configData)
  lazy val streaming    : IStreamConfiguration  = new StreamConfiguration(configData)
  lazy val checkpointing: ICPConfiguration      = new CPConfiguration(configData)
}
