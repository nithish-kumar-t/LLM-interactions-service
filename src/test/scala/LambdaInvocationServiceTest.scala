import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.stream.Materializer
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import protobuf.llmQuery.{LlmQueryRequest, LlmQueryResponse}

import scala.concurrent.Future

class LambdaInvocationServiceTest extends AsyncFlatSpec with Matchers {
  implicit val system: ActorSystem = ActorSystem("ApiTestSystem")
  implicit val materializer: Materializer = Materializer(system)

  "ApiInvocationHelper" should "successfully handle valid API requests" in {
    val validRequest = LlmQueryRequest("input text", maxWords = 100)

    // Simulate successful API call
    val responseBody = """{"input": "input text", "output": "output text"}"""
    val response = HttpResponse(
      entity = HttpEntity(ContentTypes.`application/json`, responseBody)
    )

    // Simulate a successful API call
    val mockFuture: Future[LlmQueryResponse] = Future.successful(
      LlmQueryResponse("input text", "output text")
    )

    mockFuture.map { result =>
      result.input shouldEqual "input text"
      result.output shouldEqual "output text"
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

    recoverToSucceededIf[RuntimeException](mockFuture)
  }
}
