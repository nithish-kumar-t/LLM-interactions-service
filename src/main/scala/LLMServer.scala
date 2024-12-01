import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.llmServer.controller.LLMRoutes

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

/**
 * LLMServer initializes and starts an HTTP server for handling requests related to
 * language model queries and conversational agents. The server runs on localhost
 * and binds routes defined in the `LLMRoutes` object.
 */
object LLMServer extends App {
  implicit val system: ActorSystem = ActorSystem("LLMServerSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  // Bind the server to the routes
  val bindingFuture = Http().newServerAt("localhost", 8080).bind(LLMRoutes.routes)

  println("Server running at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine()

  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}
