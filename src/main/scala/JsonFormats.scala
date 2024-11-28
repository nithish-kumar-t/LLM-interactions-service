import spray.json._
import DefaultJsonProtocol._

// Case classes with proper structure
case class LLMRequest(input: String)
case class LLMResponse(input: String, output: String)

// JSON Formats
object JsonFormats {
  implicit val llmRequestFormat: RootJsonFormat[LLMRequest] = jsonFormat1(LLMRequest)

  // using JsonFormat2 to format custom objects
  implicit val responseObjectFormat: RootJsonFormat[LLMResponse] = jsonFormat2(LLMResponse)
}