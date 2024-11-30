import org.yaml.snakeyaml.{DumperOptions, Yaml}
import scala.collection.mutable.ListBuffer
import java.io.{File, FileWriter}
import scala.jdk.CollectionConverters._

case class IterationResult(
                            iteration: String,
                            question: String,
                            llmResponse: String,
                            ollamaResponse: String
                          )


object YAML_Helper {
  private val options = new DumperOptions
  options.setPrettyFlow(true)
  options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)

  def createMutableResult(): ListBuffer[Map[String, Any]] = {
    // List of iteration results
    val results = ListBuffer[Map[String, Any]]()
    results
  }


  def appendResult(results:  ListBuffer[Map[String, Any]]  , iteration : Int, question: String, llmResp: String, ollamaResp: String): Any = {
    results += Map(
      "Itr-"+iteration.toString -> Map(
        "question" -> question,
        "LLM Response" -> llmResp,
        "Ollama Response" -> ollamaResp
      )
    )
  }

  def save(results: ListBuffer[Map[String, Any]]): Unit = {
    // File to write YAML
    val yaml = new Yaml(options)
    val file = new File("src/main/resources/iteration_results.yaml")
    val writer = new FileWriter(file)

    try {
      // Dump results to YAML
      yaml.dump(results.toList.asJava, writer)
      println(s"YAML file created at: ${file.getAbsolutePath}")
    } finally {
      writer.close()
    }
  }
}
