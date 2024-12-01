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

// Convert request to JSON
import protobuf.llmQuery.{LlmQueryRequest, LlmQueryResponse}
import spray.json._
object LambdaInvocationService {
  private val logger = LoggerFactory.getLogger(getClass)

  def queryLLM(protoRequest: LlmQueryRequest)(implicit system: ActorSystem): Future[LlmQueryResponse] = {
    implicit val ec = system.dispatcher
    implicit val materializer = ActorMaterializer()

    val url = ConfigLoader.getConfig("lambdaApiGateway")
    val maxWords :Int =
      if(protoRequest.maxWords != 0)
        protoRequest.maxWords
      else
        ConfigLoader.getConfig("maxWords").toInt

    // HTTP Entity
    val httpEntity = HttpEntity.Strict(
      ContentTypes.`application/grpc+proto`,
      akka.util.ByteString(protoRequest.toProtoString.getBytes)
    )

    // Create HTTP request
    val httpRequest = HttpRequest(
      method = HttpMethods.GET,
      uri = Uri(url),
      entity = httpEntity
    )

    // Send request and handle response
    val responseFuture = Http().singleRequest(httpRequest).flatMap { response =>
      response.status.intValue() match {
        // HTTP codes with 200-299 are all happy paths,
        case statusCode if statusCode >= 200 && statusCode < 300 =>
          // Extract response body and parse JSON
          logger.info(response.headers.toString())
          //If parsing is taking more than 5 seconds, we are halting the process
          response.entity.toStrict(5.seconds).map { entity =>
            val responseBody = entity.getData().utf8String

            //Based on client needs, re-sizing the response
            val resp = responseBody.parseJson.convertTo[LlmQueryResponse]
            if (resp.output.split(" ").toList.size >maxWords) {
              val processedResp =
                new LlmQueryResponse(resp.input, resp.output.split(" ").toList.slice(0, maxWords).mkString(" "))
              logger.info(processedResp.toString)
              processedResp
            } else {
              logger.info(resp.toString)
              resp
            }
          }
        // HTTP codes with 400-499 range are client error responses., Eg: 404, wrong path, or 401, Auth error
        // HTTP codes with 500-599 range indicate server error responses. Eg: service unavailable 503
        case statusCode if statusCode >= 400 && statusCode <= 599  =>
          // Handling error cases
          val errorMsg = s"API call failed with status: ${response.status}, \n error Message: ${response.entity},"
          logger.error(errorMsg)
          Future.failed(new RuntimeException(errorMsg))
      }
    }

    responseFuture
  }
}
