package com.llmServer.controller

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.llmServer.service.{AutomatedConversationalAgent}
import org.slf4j.LoggerFactory
import com.llmServer.util.JsonFormats._

import scala.concurrent.{ExecutionContext, Future}
import protobuf.llmQuery._
import service.LambdaInvocationService

import scala.util.{Failure, Success}

/**
 * `LLMRoutes` object defines HTTP routes for interacting with a language model.
 * It includes endpoints for querying the language model, starting a conversational agent,
 * and checking the health of the service.
 */
object LLMRoutes {
  private val logger = LoggerFactory.getLogger(getClass)

  /**
   * Defines HTTP routes for the LLM service.
   *
   * @param system Implicit ActorSystem for managing actor lifecycle.
   * @return Route object representing all HTTP endpoints.
   */
  def routes(implicit system: ActorSystem): Route = {
    // ExecutionContext for handling asynchronous operations
    implicit val ec: ExecutionContext = system.dispatcher

    concat(
      /**
       * Endpoint: POST /query-llm
       * Handles a request to query the language model. Accepts a JSON payload with input text
       * and parameters, processes the query asynchronously, and returns the result.
       */
      path("query-llm") {
        post {
          entity(as[LlmQueryRequest]) { request =>
            // Asynchronously handle the LLM query
            onSuccess(LambdaInvocationService.queryLLM(request)) { response =>
              complete(response) // Return the LLM response as JSON
            }
          }
        }
      },

      /**
       * Endpoint: POST /start-conversation-agent
       * Initiates a conversation agent using the provided input. The agent runs asynchronously
       * in a separate thread, and an immediate acknowledgment response is sent back to the client.
       */
      path("start-conversation-agent") {
        post {
          entity(as[LlmQueryRequest]) { request =>
            // Start the Automated Conversational Agent in a separate thread
            Future {
              logger.info("Starting Automated Conversational Agent...")
              AutomatedConversationalAgent.start(request)
              logger.info(s"Successfully completed the execution of the client...")
            }.onComplete {
              case Success(value) => println(s"Successfully completed the execution of the client $value")
              case Failure(ex)    => println(s"An error occurred when executing the client: $ex")
            }

            // Send an immediate acknowledgment response
            complete(
              StatusCodes.Accepted,
              "Conversation started, Please check for file in location " +
                "src/main/resources/agent-resp/convestn-{timestamp}"
            )
          }
        }
      },

      /**
       * Endpoint: GET /health
       * Provides a health check endpoint to verify that the LLM service is up and running.
       */
      path("health") {
        get {
          complete(StatusCodes.OK, "LLM REST Service is up and running!") // Respond with a success message
        }
      }
    )
  }
}
