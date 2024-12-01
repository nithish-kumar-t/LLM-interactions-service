package com.llmServer.util

import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory

import scala.collection.mutable

/**
 * `ConfigGetter` is a utility object for loading and retrieving application configurations.
 * It uses Typesafe Config to manage configuration properties from `application.conf` or other sources.
 */
object ConfigLoader {
  private val logger = LoggerFactory.getLogger(getClass)
  val configMap: mutable.Map[String, String] = mutable.Map()
  // Load the configuration from the default configuration file (application.conf)
  val config: Config = ConfigFactory.load()

  //Loading all the config when starting of the application
  def setConfig(env : String): Unit = {
    configMap.put("ollama.host", config.getString(s"$env.ollama.host"))
    configMap.put("ollama.request-timeout-seconds", config.getString(s"$env.ollama.request-timeout-seconds"))
    configMap.put("ollama.model", config.getString(s"$env.ollama.model"))
    configMap.put("ollama.range", config.getString(s"$env.ollama.range"))
    configMap.put("maxWords", config.getString("maxWords"))
    configMap.put("lambdaApiGateway", config.getString("lambdaApiGateway"))
    configMap.put("conversationPath", config.getString(s"$env.conversationPath"))
    configMap.put("server", config.getString(s"$env.server"))

    if (env.equals("cloud")) {
      configMap.put("aws.region", config.getString(s"$env.region"))
      configMap.put("aws.access-key", config.getString(s"$env.access-key"))
      configMap.put("aws.secret-key", config.getString(s"$env.secret-key"))
    }

    logger.info(configMap.mkString("\n"))
  }

  def getConfig(key: String): String = {
    configMap.getOrElse(key, "")
  }

}
