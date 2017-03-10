package com.microsoft.azure.iot.iothubreact.checkpointing

import com.microsoft.azure.iot.iothubreact.checkpointing.backends.cassandra.lib.Auth
import com.typesafe.config.{Config, ConfigException}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FeatureSpec, GivenWhenThen}

class ConfigurationTest extends FeatureSpec with GivenWhenThen with MockitoSugar {

  info("As a configured instance")
  info("I want logic around returned values to be consistent with application expectations")

  val confPath = "iothub-react.checkpointing."
  feature("Configuration Cassandra authorization") {

    scenario("Only one of username or password is supplied") {
      var cfg = mock[Config]
      when(cfg.getString(confPath + "storage.cassandra.username")).thenReturn("username")
      when(cfg.getString(confPath + "storage.cassandra.password")).thenThrow(new ConfigException.Missing("path"))
      assert(CPConfiguration.cassandraAuth == None)

      cfg = mock[Config]
      when(cfg.getString(confPath + "storage.cassandra.username")).thenThrow(new ConfigException.Missing("path"))
      when(cfg.getString(confPath + "storage.cassandra.password")).thenReturn("password")
      assert(CPConfiguration   .cassandraAuth == None)
    }

    scenario("Both username and password are supplied") {
      var cfg = mock[Config]
      when(cfg.getString(confPath + "storage.cassandra.username")).thenReturn("username")
      when(cfg.getString(confPath + "storage.cassandra.password")).thenReturn("password")
      assert(CPConfiguration   .cassandraAuth == Some(Auth("username", "password")))
    }
  }

  feature("Storage namespace") {

    scenario("Cassandra has a special namespace value") {
      var cfg = mock[Config]
      when(cfg.getString(confPath + "storage.namespace")).thenReturn("")

      when(cfg.getString(confPath + "storage.backendType")).thenReturn("anythingbutcassandra")
      assert(CPConfiguration   .storageNamespace == "iothub-react-checkpoints")

      when(cfg.getString(confPath + "storage.backendType")).thenReturn("AZUREBLOB")
      assert(CPConfiguration   .storageNamespace == "iothub-react-checkpoints")

      when(cfg.getString(confPath + "storage.backendType")).thenReturn("CASSANDRA")
      assert(CPConfiguration   .storageNamespace == "iothub_react_checkpoints")
    }
  }
}
