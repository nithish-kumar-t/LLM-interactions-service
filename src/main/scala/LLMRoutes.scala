package controller

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives.{entity, _}
import akka.http.scaladsl.server.Route
import org.slf4j.LoggerFactory
import protobuf.llmQuery._
import service.{AutomatedConversationalAgent, LambdaInvocationService}
import util.JsonFormats._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object LLMRoutes {
  private val logger = LoggerFactory.getLogger(getClass)

  def routes(implicit system: ActorSystem): Route = {
    // ExecutionContext for handling Futures
    implicit val ec: ExecutionContext = system.dispatcher

    concat(
      path("query-llm") {
        post {
          entity(as[LlmQueryRequest]) { request =>
            // Use onSuccess to handle the asynchronous API call
            onSuccess(LambdaInvocationService.queryLLM(request)) { response =>
              complete(response)
            }
          }
        }
      },
      path("start-conversation-agent") {
        post {
          entity(as[LlmQueryRequest]) { request =>
            // Start AutomatedConversationalAgent in a separate thread
             Future {
              logger.info("Starting Automated Conversational Agent...")
              AutomatedConversationalAgent.start(request)
              logger.info(s"Successfully completed the execution of the client...")
            }.onComplete{
               case Success(value) => println(s"Successfully completed the execution of the client $value")
               case Failure(ex)    => println(s"An error occurred when executing the client: $ex")
            }

            // Immediately respond to the client
            complete (
              StatusCodes.Accepted,
              "Conversation started, Please check for file in location " +
                "src/main/resources/agent-resp/convestn-{timestamp}"
            )
          }
        }
      },
      path("health") {
        get {
          complete(HttpResponse(StatusCodes.OK, entity = "Service is up and healthy"))
        }
      }
    )
  }
}
