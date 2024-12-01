package service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import org.slf4j.LoggerFactory
import util.ConfigLoader
import util.JsonFormats.llmQueryResponseFormat

import scala.concurrent.Future
import scala.concurrent.duration._

// Import required protobuf and JSON libraries
import protobuf.llmQuery.{LlmQueryRequest, LlmQueryResponse}
import spray.json._

/**
 * `LambdaInvocationService` provides a utility to invoke an external Lambda function, through aws api gateway.
 * We are marshalling and un-marshaling data to and from the gateway using gRPC payloads.
 */
object LambdaInvocationService {
  private val logger = LoggerFactory.getLogger(getClass)

  /**
   * Sends a request to the external LLM API and returns the response as a future.
   *
   * @param protoRequest The protobuf-based request to be sent to the LLM API.
   * @param system       Implicit `ActorSystem` for handling asynchronous operations.
   * @return A `Future` containing the parsed LLM query response.
   */
  def queryLLM(protoRequest: LlmQueryRequest)(implicit system: ActorSystem): Future[LlmQueryResponse] = {
    implicit val ec = system.dispatcher
    implicit val materializer = ActorMaterializer()

    // Load API Gateway URL and default configurations
    val url = ConfigLoader.getConfig("lambdaApiGateway")
    val maxWords: Int =
      if (protoRequest.maxWords != 0)
        protoRequest.maxWords
      else
        ConfigLoader.getConfig("maxWords").toInt

    // Construct HTTP entity for the gRPC request
    val httpEntity = HttpEntity.Strict(
      ContentTypes.`application/grpc+proto`,
      akka.util.ByteString(protoRequest.toProtoString.getBytes)
    )

    // Create the HTTP request
    val httpRequest = HttpRequest(
      method = HttpMethods.GET,
      uri = Uri(url),
      entity = httpEntity
    )

    // Send the HTTP request and process the response
    val responseFuture = Http().singleRequest(httpRequest).flatMap { response =>
      response.status.intValue() match {
        // Handle success (HTTP status codes 200-299)
        case statusCode if statusCode >= 200 && statusCode < 300 =>
          // Log the response headers
          logger.info(response.headers.toString())

          // Extract and process the response body within a 5-second timeout
          response.entity.toStrict(5.seconds).map { entity =>
            val responseBody = entity.getData().utf8String

            // Parse the response JSON to `LlmQueryResponse`
            val resp = responseBody.parseJson.convertTo[LlmQueryResponse]

            // Truncate the response output if it exceeds the `maxWords` limit
            if (resp.output.split(" ").toList.size > maxWords) {
              val processedResp = new LlmQueryResponse(
                resp.input,
                resp.output.split(" ").toList.slice(0, maxWords).mkString(" ")
              )
              logger.info(processedResp.toString)
              processedResp
            } else {
              logger.info(resp.toString)
              resp
            }
          }

        // Handle client errors (HTTP status codes 400-499) and server errors (HTTP status codes 500-599)
        case statusCode if statusCode >= 400 && statusCode <= 599 =>
          val errorMsg = s"API call failed with status: ${response.status}, \n error Message: ${response.entity},"
          logger.error(errorMsg)
          Future.failed(new RuntimeException(errorMsg))
      }
    }

    responseFuture
  }
}
