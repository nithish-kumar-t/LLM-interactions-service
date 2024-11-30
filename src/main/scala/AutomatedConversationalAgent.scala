import akka.actor.ActorSystem
import protobuf.llmQuery.{LlmQueryRequest, LlmQueryResponse}
import io.github.ollama4j.OllamaAPI
import io.github.ollama4j.utils.Options
import org.slf4j.LoggerFactory

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.jdk.CollectionConverters._

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

  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      logger.error("input seed text not passed")
      sys.exit(-1)
    }

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

  def start(protoRequest: LlmQueryRequest)(implicit system: ActorSystem): Unit = {
    // Initialize Ollama API

    val llamaAPI = new OllamaAPI(ConfigLoader.getConfig(OLLAMA_HOST))
    llamaAPI.setRequestTimeoutSeconds(ConfigLoader.getConfig(OLLAMA_REQUEST_TIMEOUT).toLong)
    val llamaModel = ConfigLoader.getConfig(OLLAMA_MODEL)
    val range = ConfigLoader.getConfig(OLLAMA_QUERIES_RANGE).toInt

    val results = YAML_Helper.createMutableResult()
    var currentRequest = protoRequest

    // Use sequence to ensure synchronous execution
    Iterator.range(0, range).foreach { itr =>
      try {
        // Synchronously wait for the LLM query to complete
        this.synchronized{
          val response = Await.result(ApiInvocationHelper.queryLLM(currentRequest), 10.seconds)
          val input = currentRequest.input + " "
          val output = response.output

          // Synchronously generate Llama response
          val llamaResult = llamaAPI.generate(
            llamaModel,
            LLAMA_PREFIX + input + output,
            false,
            new Options(Map.empty[String, Object].asJava)
          )
          val llamaResp = llamaResult.getResponse

          println(llamaResp)
          YAML_Helper.appendResult(results, itr, input, output, llamaResp)

          // Prepare next request for the next iteration
          currentRequest = new LlmQueryRequest(LLAMA_TO_LAMBDA_PREFIX + llamaResp, 100)

          // Return the result if needed
          println("****************")
          // println(itr, input, output, llamaResp)
        }
        // (itr, input, output, llamaResp)
      } catch {
        case e: Exception =>
          logger.error(s"PROCESS FAILED at iteration $itr: ${e.getMessage}", e)
          throw e
      }
    }

    // Wait for all futures to complete
    try {
//      val finalResults = Await.result(Future.sequence(sequencedFutures), (range * 10).seconds)
//      logger.info(s"Completed ${finalResults.size} iterations")
    } catch {
      case e: Exception =>
        logger.error(s"Error in processing: ${e.getMessage}", e)
    } finally {
      YAML_Helper.save(results)
    }
  }

//  def helper() : Unit = {
//
//  }

}
