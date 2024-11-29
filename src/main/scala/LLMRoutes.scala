import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import JsonFormats._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import scala.concurrent.Future
import scala.concurrent.duration._

// Convert request to JSON
import spray.json._

import protobuf.llmQuery.{LlmQueryRequest, LlmQueryResponse}

object LLMRoutes {

  private def queryLLM(request: LLMRequest)(implicit system: ActorSystem): Future[LlmQueryResponse] = {
    implicit val ec = system.dispatcher
    implicit val materializer = ActorMaterializer()

    val url = "https://tfz33jek7j.execute-api.us-east-2.amazonaws.com/PRODStage/queryLLM"

    //val llmReq : LlmQueryRequest =
    val protoRequest : LlmQueryRequest = new LlmQueryRequest(request.input, 0)

    // Create HTTP request
    val httpRequest = HttpRequest(
      method = HttpMethods.GET,
      uri = Uri(url),
      headers = List(`Content-Type`(ContentTypes.`application/grpc+proto`)),
      entity = HttpEntity(ContentTypes.`application/grpc+proto`, protoRequest.toProtoString.getBytes)
    )

    // Send request and handle response
    val responseFuture = Http().singleRequest(httpRequest).flatMap { response =>
      response.status match {
        case StatusCodes.OK =>
          // Extract response body and parse JSON
          println(response)
          response.entity.toStrict(5.seconds).map { entity =>
            val responseBody = entity.getData().utf8String

            println(responseBody)
            responseBody.parseJson.convertTo[LlmQueryResponse]
          }
        case _ =>
          // Handle error cases
          Future.failed(new RuntimeException(s"API call failed with status: ${response.status}"))
      }
    }

    responseFuture
  }

  def routes(implicit system: ActorSystem): Route = concat(
    path("query-llm") {
      get {
        entity(as[LLMRequest]) { request =>
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