// src/main/scala/ConversationalAgent.scala

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives.complete
import io.github.ollama4j.OllamaAPI
import io.github.ollama4j.utils.Options
import org.slf4j.LoggerFactory
import protobuf.llmQuery.LlmQueryRequest

import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._
import scala.util.{Success, Failure}


object AutomatedConversationalAgent {
  private val logger = LoggerFactory.getLogger(getClass)
  private val OLLAMA_HOST = "ollama.host"
  private val OLLAMA_REQUEST_TIMEOUT = "ollama.request-timeout-seconds"
  private val OLLAMA_MODEL = "ollama.model"
  private val OLLAMA_QUERIES_RANGE = "ollama.range"
  private val LLAMA_PREFIX = "how can you respond to the statement "
  private val LLAMA_TO_LAMBDA_PREFIX = "Do you have any comments on "

  val llamaAPI: OllamaAPI = new OllamaAPI(ConfigLoader.getConfig(OLLAMA_HOST))
  llamaAPI.setRequestTimeoutSeconds(ConfigLoader.getConfig(OLLAMA_REQUEST_TIMEOUT).toLong)
  val llamaModel = ConfigLoader.getConfig(OLLAMA_MODEL)
  val range = ConfigLoader.getConfig(OLLAMA_QUERIES_RANGE).toInt

  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      logger.error("input seed text not passed")
      sys.exit(-1)
    }
    val seedText = args(0)
    val protoRequest: LlmQueryRequest = new LlmQueryRequest(seedText, 100)

    // Create an ActorSystem
    implicit val system: ActorSystem = ActorSystem("AutomatedConversationalAgentSystem")

    start(protoRequest)
  }

  def start (protoRequest: LlmQueryRequest)(implicit system: ActorSystem) : Unit = {
    implicit val ec: ExecutionContext = system.dispatcher

    val results = YAML_Helper.createMutableResult()
    var nextRequest = protoRequest

    Iterator.range(0, range).foreach { itr =>
      try {
        val responseFuture = ApiInvocationHelper.queryLLM(nextRequest)

        responseFuture.onComplete {
          case Success(response) => {
            val input = protoRequest.input + " "
            val output = response.output
            val llamaResult = llamaAPI.
              generate(llamaModel, LLAMA_PREFIX + input + output, false, new Options(Map.empty[String, Object].asJava))
            val llamaResp = llamaResult.getResponse
            println(llamaResp)
            YAML_Helper.appendResult(results, itr, input, output, llamaResp)
            nextRequest = new LlmQueryRequest(LLAMA_TO_LAMBDA_PREFIX + llamaResp, 10)
          }
          case Failure(exception) =>
            println(s"Error occurred: ${exception.getMessage}")
        }
      } catch {
        case e: Exception =>
          println("PROCESS FAILED", e.getMessage)
          e.printStackTrace()
      } finally {
        YAML_Helper.save(results)
      }
    }
  }
}
