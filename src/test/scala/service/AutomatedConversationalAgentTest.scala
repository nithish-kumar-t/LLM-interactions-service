package service

import org.mockito.MockitoSugar
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import protobuf.llmQuery.LlmQueryRequest

import java.nio.file.Files

class AutomatedConversationalAgentTest extends BaseScalaTest with MockitoSugar {
  val llmQueryRequest = new LlmQueryRequest("A quick brown fox", 10)

  "AutomatedConversationalAgent" should " successfully run conversation" in {
    val args: Array[String] = Array.fill(1)("A quick brown fox")
    AutomatedConversationalAgent.main(args)
    Files.list(directory).count() shouldBe >= (1L)
  }

  "AutomatedConversationalAgent" should " without any arguments program should terminate" in {
//    val args: Array[String] = Array.fill(1)("A quick brown fox")
    AutomatedConversationalAgent.main(Array.empty[String])
  }
}

