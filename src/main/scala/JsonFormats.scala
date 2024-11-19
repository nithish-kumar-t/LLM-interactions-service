import spray.json.DefaultJsonProtocol._

// Define case classes for requests and responses
case class LLMRequest(query: String)
case class LLMResponse(response: String)

// Import these implicits to enable JSON marshalling/unmarshalling
object JsonFormats {
  import spray.json.DefaultJsonProtocol._

  implicit val llmRequestFormat = jsonFormat1(LLMRequest)
  implicit val llmResponseFormat = jsonFormat1(LLMResponse)
}
