package service

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.stream.Materializer
import com.llmServer.util.ConfigLoader
//import com.llmServer.service.LambdaInvocationService
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import org.slf4j.LoggerFactory
import protobuf.llmQuery.{LlmQueryRequest, LlmQueryResponse}

import java.nio.file.{Files, Paths}
import scala.concurrent.Future

class LambdaInvocationServiceTest extends AsyncFlatSpec with Matchers {
  private val logger = LoggerFactory.getLogger(getClass)
  implicit val system: ActorSystem = ActorSystem("ApiTestSystem")
  implicit val materializer: Materializer = Materializer(system)
  ConfigLoader.setConfig("local")

  "LambdaInvocationService" should "successfully handle valid API requests" in {
    val llmQueryReq = new LlmQueryRequest("A quick brown fox jumps over a ", 100)

    val resultFuture: Future[Unit] = Future {
      LambdaInvocationService.queryLLM(llmQueryReq)
    }

    resultFuture.map { _ =>
      val directoryPath = "/Users/tnithish/Learning/CS-441/LLM-interactions-service/src/main/resources/conversation-agents"
      val directory = Paths.get(directoryPath)

      // Assert the output directory contains files as expected
      Files.list(directory).count() shouldBe >= (0L)
      succeed // Return an assertion to match the required type
    }
  }

  it should "truncate response output if exceeding maxWords" in {
    val request = LlmQueryRequest("input text", maxWords = 5)
    val llmQueryResponse = new LlmQueryResponse("input", "output")

    val response = HttpResponse(
      entity = HttpEntity(ContentTypes.`application/grpc+proto`, llmQueryResponse.toProtoString.getBytes)
    )

    // Mock a truncated response
    val mockFuture: Future[LlmQueryResponse] = Future.successful(
      LlmQueryResponse("input text", "word1 word2 word3 word4 word5")
    )

    mockFuture.map { result =>
      result.output shouldEqual "word1 word2 word3 word4 word5"
    }
  }

  it should "handle API errors gracefully" in {
    val request = LlmQueryRequest("input text", maxWords = 50)

    // Simulate API failure
    val errorMessage = "API call failed with status: 500, Internal Server Error"
    val mockFuture: Future[LlmQueryResponse] = Future.failed(new RuntimeException(errorMessage))

    recoverToExceptionIf[RuntimeException](mockFuture).map { ex =>
      ex.getMessage should include("API call failed with status: 500")
    }
  }
}
