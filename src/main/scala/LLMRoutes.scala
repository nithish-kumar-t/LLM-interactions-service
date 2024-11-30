import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import JsonFormats._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.concurrent.duration._

// Convert request to JSON
import spray.json._

import protobuf.llmQuery.{LlmQueryRequest, LlmQueryResponse}

object LLMRoutes {
  private val logger = LoggerFactory.getLogger(getClass)

  private def queryLLM(protoRequest: LlmQueryRequest)(implicit system: ActorSystem): Future[LlmQueryResponse] = {
    implicit val ec = system.dispatcher
    implicit val materializer = ActorMaterializer()

    val url = ConfigLoader.getConfig("lambdaApiGateway")
    val maxWords :Int =
      if(protoRequest.maxWords != 0)
        protoRequest.maxWords
      else
        ConfigLoader.getConfig("maxWords").toInt

    // Create HTTP request
    val httpRequest = HttpRequest(
      method = HttpMethods.GET,
      uri = Uri(url),
      headers = List(`Content-Type`(ContentTypes.`application/grpc+proto`)),
      entity = HttpEntity(ContentTypes.`application/grpc+proto`, protoRequest.toProtoString.getBytes)
    )

    // Send request and handle response
    val responseFuture = Http().singleRequest(httpRequest).flatMap { response =>
      response.status.intValue() match {
        // HTTP codes with 200-299 are all happy paths,
        case statusCode if statusCode >= 200 && statusCode < 300 =>
          // Extract response body and parse JSON
          logger.info(response.toString())
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

  def routes(implicit system: ActorSystem): Route = concat(
    path("query-llm") {
      get {
        entity(as[LlmQueryRequest]) { request =>
          // Use onSuccess to handle the asynchronous API call
          onSuccess(queryLLM(request)) { response =>
            complete(response)
          }
        }
      }
    },
    path("health") {
      get {
        complete(StatusCodes.OK, "LLM REST Service is up and running!")
      }
    }
  )
}
