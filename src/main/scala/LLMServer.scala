import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.llmServer.controller.LLMRoutes
import com.llmServer.util.ConfigLoader
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContextExecutor

/**
 * LLMServer initializes and starts an HTTP server for handling requests related to
 * language model queries and conversational agents. The server runs on localhost
 * and binds routes defined in the `LLMRoutes` object.
 */
object LLMServer extends App {
  if (!this.args.isEmpty && this.args(0).equals("local")) {
    println("Environment is------local")
    ConfigLoader.setConfig("local")
  } else {
    println("Environment is------docker")
    ConfigLoader.setConfig("docker")
  }
  private val logger = LoggerFactory.getLogger(getClass)
  implicit val system: ActorSystem = ActorSystem("LLMServerSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  // Bind the server to the routes
  val url = ConfigLoader.getConfig("server")
  Http().newServerAt("0.0.0.0", 8080).bind(LLMRoutes.routes)

  logger.info("Server running at http://localhost:8080/\nPress RETURN to stop...")
}
