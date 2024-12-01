package com.llmServer.util

import com.typesafe.config.{Config, ConfigFactory}

/**
 * `ConfigLoader` is a utility object for loading and retrieving application configurations.
 * It uses Typesafe Config to manage configuration properties from `application.conf` or other sources.
 */
object ConfigLoader {

  // Load the configuration from the default configuration file (application.conf)
  val config: Config = ConfigFactory.load()

  def loadConfig(): Config = {
    config
  }

  /**
   * Retrieves a specific configuration value as a string.
   *
   * @param key The key of the configuration property to retrieve.
   * @return The value of the configuration property as a string.
   */
  def getConfig(key: String): String = {
    config.getString(key)
  }
}
