package controller

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration._

class LLMRouterTest extends AnyWordSpec with Matchers with ScalatestRouteTest {

  // Increase default timeout for route testing
  implicit val routeTestTimeout: RouteTestTimeout = RouteTestTimeout(20.seconds)

  // Instantiate the routes
  val routes: Route = LLMRoutes.routes
  val requestBody = """{"input": "A quick brown fox", "maxWords": 50}"""

  "LLM API Routes" should {

    "respond to /query-llm" in {
      // Send the request to the route and use `check` for assertions
      Post("/query-llm", HttpEntity(ContentTypes.`application/json`, requestBody)) ~> routes ~> check {
        // Add more detailed logging or debugging if needed
        println(s"Response status: ${status}")
        println(s"Response entity: ${responseAs[String]}")

        handled shouldBe true // Ensure the route handled the request
        status should not be StatusCodes.InternalServerError
      }
    }

    "respond to /start-conversation-agent" in {

      // Send POST request
      Post("/start-conversation-agent", HttpEntity(ContentTypes.`application/json`, requestBody)) ~> routes ~> check {
        // Ensure the route processes the request without errors
        handled shouldBe true
        status should not be StatusCodes.InternalServerError
      }
    }

    "respond to /health" in {
      // Send GET request
      Get("/health") ~> routes ~> check {
        // Ensure the route processes the request without errors
        handled shouldBe true
        status shouldBe StatusCodes.OK
      }
    }
  }
}
