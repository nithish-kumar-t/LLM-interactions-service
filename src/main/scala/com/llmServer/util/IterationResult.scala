package com.llmServer.util

import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.{DumperOptions, Yaml}
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.{Files, Paths}
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
  def save(results: ListBuffer[IterationResult], totalInteractions:Int, totalTimeSeconds : Long, terminationCondition: String): Unit = {
    val conversationPath = ConfigLoader.getConfig("conversationPath")
    val finalOutput = populateDataInYaml(results, totalInteractions, totalTimeSeconds, terminationCondition)
    if (conversationPath.startsWith("s3://")) {
      // Save to S3
      saveToS3(conversationPath, finalOutput)
    } else {
      // Save locally
      saveLocally(conversationPath, finalOutput)
    }
  }

  /**
   * Saves the YAML content to a local file system.
   *
   * @param directory             The local directory path where the file will be saved.
   * @param finalOutput            A java object of final consolidated payload
   */
  private def saveLocally(directory: String, finalOutput: java.util.Map[String, Object] ): Unit = {
    // Check if directory exists or not, if not create one.
    val directoryPath= Paths.get(directory)
    if (!Files.exists(directoryPath)) {
      logger.info(s"directory created successfully at $directory")
      Files.createDirectories(directoryPath)
    }
    val file = new File(s"$directory/" + "conversation-result-" +Instant.now().toString + ".yaml")
    val writer = new BufferedWriter(new FileWriter(file))

    try {
      yaml.dump(finalOutput, writer)

      logger.info(s"YAML file created at: ${file.getAbsolutePath}")
    } finally {
      writer.close() // Ensure the writer is closed after the file is written
    }

  }

  /**
   * Saves the YAML content to an Amazon S3 bucket.
   *
   * @param s3Path                The S3 URI where the file will be uploaded (e.g., s3://bucket-name/path).
   * @param finalOutput           A java object of final consolidated payload
   */
  private def saveToS3(s3Path: String,  finalOutput: java.util.Map[String, Object] ): Unit = {
    // Parse S3 URI
    val uri = new java.net.URI(s3Path)
    val bucket = uri.getHost
    val keyPrefix = uri.getPath.stripPrefix("/")

    // Create a unique key for the file
    val timestamp = Instant.now().toString.replace(":", "-")
    val key = if (keyPrefix.isEmpty) s"conversation-result-$timestamp.yaml" else s"$keyPrefix/conversation-result-$timestamp.yaml"

    val yamlContent = yaml.dump(finalOutput)

    // Initialize S3 client
    val s3Client = S3Client.builder()
      .region(Region.of(ConfigLoader.getConfig("aws.region")))
      .credentialsProvider(
        StaticCredentialsProvider.create(
          AwsBasicCredentials.create(
            ConfigLoader.getConfig("aws.access-key"), ConfigLoader.getConfig("aws.secret-key")
          )
        )
      )
      .build()

    try {
      // Create PutObjectRequest
      val putObjectRequest = PutObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .contentType("application/x-yaml")
        .build()

      // Upload the YAML content
      s3Client.putObject(putObjectRequest, RequestBody.fromString(yamlContent))

      logger.info(s"YAML file uploaded to S3 at: s3://$bucket/$key")
    } catch {
      case e: Exception =>
        logger.error("Failed to upload YAML file to S3", e)
    } finally {
      s3Client.close()
    }
  }

  /**
   * Helper method to populate data from conversations and statistics
   *
   * @param results               A mutable list of iteration results.
   * @param totalInteractions     Total number of interactions.
   * @param totalTimeSeconds     Total time in seconds for the conversation.
   * @param terminationCondition The condition that caused the conversation to terminate.
   */
  private def populateDataInYaml(results: ListBuffer[IterationResult], totalInteractions: Int, totalTimeSeconds: Long, terminationCondition: String): java.util.Map[String, Object] = {
    // Prepare the YAML content in memory
    val conversationEntries = results.map { result =>
      Map(
        result.iteration -> Map(
          "question" -> result.question,
          "LLM Response" -> result.llmResponse,
          "Ollama Response" -> result.ollamaResponse
        ).asJava
      ).asJava
    }.asJava

    val statistics = Map(
      "statistics" -> Map(
        "total_interactions" -> totalInteractions,
        "total_time_seconds" -> totalTimeSeconds,
        "termination_condition" -> terminationCondition
      ).asJava
    ).asJava

    val finalOutput = Map(
      "conversation" -> conversationEntries,
      "statistics" -> statistics
    ).asJava
    finalOutput
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

