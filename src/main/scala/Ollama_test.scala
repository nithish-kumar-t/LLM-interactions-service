// src/main/scala/ConversationalAgent.scala

import com.typesafe.config.{Config, ConfigFactory}
import io.github.ollama4j.OllamaAPI
import io.github.ollama4j.models.OllamaResult
import io.github.ollama4j.utils.Options


object Ollama_test {
  val config: Config = ConfigFactory.load()
  def main(args: Array[String]): Unit = {
    val ollamaAPI: OllamaAPI = new OllamaAPI(config.getString("ollama.host"))
    ollamaAPI.setRequestTimeoutSeconds(config.getString("ollama.request-timeout-seconds").toLong)


    try {
      val result: OllamaResult = ollamaAPI.generate(config.getString("ollama.model"), "RRR is a RAM Charan Movie", false, new Options(null))
      println(result.getResponse)
    } catch {
      case e: Exception =>
        println("PROCESS FAILED", e.getMessage)
        e.printStackTrace()
    }
  }
}
