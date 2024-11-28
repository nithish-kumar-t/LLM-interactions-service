import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import JsonFormats._

object LLMRoutes {

  def queryLLM(request: LLMRequest): LLMResponse = {
    // Simulate a response from the LLM
    LLMResponse(s"The LLM response for '${request.query}' is: Example response")
  }

  val routes: Route = concat(
    path("query-llm") {
      get {
        entity(as[LLMRequest]) { request =>
          val response = queryLLM(request)
          complete(response)
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
