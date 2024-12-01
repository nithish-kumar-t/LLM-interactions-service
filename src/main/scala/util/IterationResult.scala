package util

import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.{DumperOptions, Yaml}

import java.io.{BufferedWriter, File, FileWriter}
import java.time.Instant
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._

/**
 * `YAML_Helper` is a utility object that provides methods for managing conversational iteration results
 * and saving them as YAML file. It supports creating, appending, and persisting iteration results.
 */
object YAML_Helper {
  private val logger = LoggerFactory.getLogger(getClass)

  // Configure YAML options for pretty and block-style formatting
  private val options = new DumperOptions
  options.setPrettyFlow(true)
  options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK) // Use block formatting for readability

  // Initialize the YAML serializer
  private val yaml = new Yaml(options)

  /**
   * Creates an empty mutable list to store iteration results.
   *
   * @return An empty `ListBuffer` of `IterationResult` objects.
   */
  def createMutableResult(): ListBuffer[IterationResult] = {
    ListBuffer.empty[IterationResult]
  }

  /**
   * Appends a new iteration result to the results list.
   *
   * @param results     A mutable list of iteration results.
   * @param iteration   The iteration number as an integer.
   * @param question    The question or input text.
   * @param llmResp     The response from the LLM (Large Language Model).
   * @param ollamaResp  The response from the Ollama system.
   */
  def appendResult(
                    results: ListBuffer[IterationResult],
                    iteration: Int,
                    question: String,
                    llmResp: String,
                    ollamaResp: String
                  ): Unit = {
    results += IterationResult(s"Iteration-$iteration", question, llmResp, ollamaResp)
    logger.info(s"Iteration-$iteration")
  }

  /**
   * Saves the list of iteration results as a YAML file.
   *
   * @param results A mutable list of iteration results to be saved.
   *                Each result is converted into YAML format and written to a file.
   */
  def save(results: ListBuffer[IterationResult]): Unit = {
    val file = new File("src/main/resources/conversation-agents/iteration_results-" + Instant.now().toString + ".yaml")
    val writer = new BufferedWriter(new FileWriter(file))

    try {
      // Convert each iteration result into a nested YAML structure and write to the file
      results.foreach { result =>
        val entry = Map(
          result.iteration -> Map(
            "question" -> result.question,
            "LLM Response" -> result.llmResponse,
            "Ollama Response" -> result.ollamaResponse
          ).asJava
        ).asJava
        yaml.dump(entry, writer)
      }
      logger.info(s"YAML file created at: ${file.getAbsolutePath}")
    } finally {
      writer.close() // Ensure the writer is closed after the file is written
    }
  }
}

/**
 * Case class representing a single iteration result.
 *
 * @param iteration     The name of the iteration (e.g., "Iteration-1").
 * @param question      The input question or text.
 * @param llmResponse   The response from the Large Language Model (LLM).
 * @param ollamaResponse The response from the Ollama system.
 */
case class IterationResult(
                            iteration: String,
                            question: String,
                            llmResponse: String,
                            ollamaResponse: String
                          )
