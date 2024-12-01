import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import JsonFormats._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
import protobuf.llmQuery._

import scala.util.{Failure, Success}

object LLMRoutes {
  private val logger = LoggerFactory.getLogger(getClass)

  def routes(implicit system: ActorSystem): Route = {
    // ExecutionContext for handling Futures
    implicit val ec: ExecutionContext = system.dispatcher

    concat(
      path("query-llm") {
        get {
          entity(as[LlmQueryRequest]) { request =>
            // Use onSuccess to handle the asynchronous API call
            onSuccess(LambdaInvocationService.queryLLM(request)) { response =>
              complete(response)
            }
          }
        }
      },
      path("start-conversation-agent") {
        get {
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
          complete(StatusCodes.OK, "LLM REST Service is up and running!")
        }
      }
    )
  }
}
