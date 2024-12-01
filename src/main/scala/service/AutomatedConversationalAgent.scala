package service

import akka.actor.ActorSystem
import io.github.ollama4j.OllamaAPI
import io.github.ollama4j.utils.Options
import org.slf4j.LoggerFactory
import protobuf.llmQuery.LlmQueryRequest
import util.{ConfigLoader, YAML_Helper}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

/**
 * `AutomatedConversationalAgent` is a service that facilitates automated conversations
 * by invoking a Lambda LLM (Language Model) and Ollama API in sequence.
 */
object AutomatedConversationalAgent {
  private val logger = LoggerFactory.getLogger(getClass)

  // Configuration keys
  private val OLLAMA_HOST = "ollama.host"
  private val OLLAMA_REQUEST_TIMEOUT = "ollama.request-timeout-seconds"
  private val OLLAMA_MODEL = "ollama.model"
  private val OLLAMA_QUERIES_RANGE = "ollama.range"

  // Prefixes for query generation
  private val LLAMA_PREFIX = "how can you respond to the statement "
  private val LLAMA_TO_LAMBDA_PREFIX = "Do you have any comments on "

  /**
   * This function is mainly for test purpose.
   *
   * @param args Command-line arguments, where the first argument is expected to be the seed text.
   */
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      logger.error("Input seed text not passed.")
    } else {
      val seedText = args(0)
      val protoRequest: LlmQueryRequest = new LlmQueryRequest(seedText, 100)

      // Create an ActorSystem
      implicit val system: ActorSystem = ActorSystem("AutomatedConversationalAgentSystem")

      try {
        start(protoRequest)
      } finally {
        system.terminate()
      }
    }
  }

  /**
   * Starts the conversational agent by iteratively interacting with Lambda LLM and Ollama APIs.
   *
   * @param protoRequest Initial request containing the seed text and other parameters.
   * @param system Implicit `ActorSystem` for asynchronous operations.
   */
  def start(protoRequest: LlmQueryRequest)(implicit system: ActorSystem): Unit = {
    // Initialize Ollama API
    val llamaAPI = new OllamaAPI(ConfigLoader.getConfig(OLLAMA_HOST))
    llamaAPI.setRequestTimeoutSeconds(ConfigLoader.getConfig(OLLAMA_REQUEST_TIMEOUT).toLong)
    val llamaModel = ConfigLoader.getConfig(OLLAMA_MODEL)
    val range = ConfigLoader.getConfig(OLLAMA_QUERIES_RANGE).toInt

    val results = YAML_Helper.createMutableResult()
    var nextRequest = protoRequest

    // Iteratively process queries and responses
    Iterator.range(0, range).foreach { itr =>
      try {
        // Synchronously wait for the LLM query to complete
        this.synchronized {
          val response = Await.result(LambdaInvocationService.queryLLM(nextRequest), 15.seconds)
          val input = nextRequest.input + " "
          val output = response.output

          // Generate a response using the Ollama API
          val llamaResult = llamaAPI.generate(
            llamaModel,
            LLAMA_PREFIX + input + output,
            false,
            new Options(Map.empty[String, Object].asJava)
          )
          val llamaResp = llamaResult.getResponse

          logger.info(llamaResp)
          YAML_Helper.appendResult(results, itr, input, output, llamaResp)

          // Prepare the next request for the next iteration
          nextRequest = new LlmQueryRequest(LLAMA_TO_LAMBDA_PREFIX + llamaResp, 100)

          logger.info("Iteration completed successfully.")
        }
      } catch {
        case e: Exception =>
          logger.error(s"PROCESS FAILED at iteration $itr: ${e.getMessage}", e)
          throw e
      }
    }

    // Save all the conversation results to a YAML file
    YAML_Helper.save(results)
  }
}
