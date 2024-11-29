import spray.json._
import DefaultJsonProtocol._
import protobuf.llmQuery.{LlmQueryRequest, LlmQueryResponse}



// Intermediate case classes for proto objects
case class LlmQueryRequestCase(input: String)
case class LlmQueryResponseCase(output: String)

case class LLMResponse(input: String, output: String)

// JSON Formats
object JsonFormats {

  // JSON formats for intermediate case classes
  implicit val llmQueryRequestCaseFormat: RootJsonFormat[LlmQueryRequestCase] = jsonFormat1(LlmQueryRequestCase)
  implicit val llmQueryResponseCaseFormat: RootJsonFormat[LlmQueryResponseCase] = jsonFormat1(LlmQueryResponseCase)

  implicit val LLMQueryResponse: RootJsonFormat[LLMResponse] = jsonFormat2(LLMResponse)

  // Custom formats for proto-generated classes
  // Custom formats for proto-generated classes
  implicit val llmQueryRequestFormat: RootJsonFormat[LlmQueryRequest] = new RootJsonFormat[LlmQueryRequest] {
    override def write(obj: LlmQueryRequest): JsValue = {
      LlmQueryRequestCase(obj.input).toJson
    }
    override def read(json: JsValue): LlmQueryRequest = {
      val caseClass = json.convertTo[LlmQueryRequestCase]
      LlmQueryRequest(caseClass.input)
    }
  }

  implicit val llmQueryResponseFormat: RootJsonFormat[LlmQueryResponse] = new RootJsonFormat[LlmQueryResponse] {
    override def write(obj: LlmQueryResponse): JsValue = {
      LlmQueryResponseCase(obj.output).toJson
    }
    override def read(json: JsValue): LlmQueryResponse = {
      val caseClass = json.convertTo[LlmQueryResponseCase]
      LlmQueryResponse(caseClass.output)
    }
  }
}
