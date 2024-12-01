# LLM-interactions-service


### Author: Nithish Kumar Thathaiahkalva
<!-- ### UIN :  -->
### Email: nthat@uic.edu

##  Description

This project focused on developing a conversational agent using a microservice-based architecture. The agent processes user queries, interacts with a Large Language Model (LLM) deployed on AWS, and generates conversational responses. The implementation includes key components such as an HTTP-based RESTful service, integration with AWS Lambda and API Gateway. The interaction with the conversational agent is fully automated, once a user submits a request, the agent continuously queries the LLM based on responses until the specified condition is satisfied.



##  Project structure
### This Project is continuation of HomeWork-2 where we trained the model and added context to for generating sentences.

1. **This project consists of 2 parts**:

   a. First is a simple microservice using Akka Http in scala, for clients to interact with the LLM

   b. Second is a AWS Lambda function, where the bedrock code will be residing.

2. For the first part we are


### 1. Environment and Configuration Setup
   - Loads environment-specific configurations for training and text generation, including settings such as the number of epochs, batch size, learning rate, input/output paths, etc.
   - Supports configuration for both local (local file system) and cloud (Amazon S3) environments.

### 2. Data Loading and Preparation
   - Loads the input text data into an RDD (Resilient Distributed Dataset) in Spark.
   - Applies preprocessing steps to trim whitespace, filter out empty lines, and cache the data to optimize performance.
   - For cloud environments, reads data directly from S3 buckets; otherwise, reads from the local file system.
   - Dataset used for training the model [Wikipedia Text](https://huggingface.co/datasets/Daniel-Saeedi/wikipedia/blob/main/wikipedia-10.txt).

   **Intermediate Result**: A preprocessed, cached RDD containing lines of text, ready for tokenization and training.

### 3. Tokenizer Setup
   - The `Tokenizer` class tokenizes text into sequences of indices, mapping each word to a unique integer.
   - This encoding prepares the text data for feeding into the neural network.

   **Intermediate Result**: A `Tokenizer` instance that can encode words into integer sequences and decode integer sequences back into words.

### 4. Training Data Splitting
   - Splits the tokenized RDD into training and validation sets.
   - Generates training samples by creating context windows (sequences of words) and corresponding target words using a sliding window approach.

   **Intermediate Result**: Two RDDs (one for training and another for validation) with samples consisting of input sequences (context windows) and their corresponding target words, 80% is used for training and 20% for validating.

### 5. Model Initialization and Serialization
   - Builds and initializes a neural network model (MultiLayerNetwork) with a specific configuration.
   - Serializes the model into a byte array format for broadcasting across Spark workers.

   **Intermediate Result**: A serialized version of the initialized model, ready to be distributed for training.

### 6. Distributed Training Process
   - Conducts training in parallel across Spark partitions, with each partition holding a portion of the training samples.
   - For each epoch:
      - Deserializes the model for use within each partition.
      - Processes batches by feeding sequences and targets into the model, which calculates loss and adjusts weights.
      - Logs metrics such as loss, accuracy, and memory usage for each epoch.
      - At the end of each epoch, averages models from all partitions to update the main model.

   **Intermediate Result**: Accumulated metrics (e.g., average loss, accuracy) and an updated model after each epoch. Metrics are stored in a buffer for eventual logging or saving.

### 7. Text Generation using the Trained Model
   - After training, the model generates new sentences based on an initial seed text.
   - Uses a temperature-based sampling method for controlled randomness in word selection.

   **Intermediate Result**: Generated sentences based on the seed text, stored as a single string.

### 8. Saving Results
   - Saves all intermediate metrics (stored in a buffer) and the generated text to the specified output location.
   - Writes results to S3 in cloud environments or to the local file system otherwise.

   **Final Output**:
   - A CSV file with metrics across epochs (e.g., loss, accuracy, epoch duration).
   - A text file with the generated sentences based on the seed text.

<img width="1286" alt="image" src="https://github.com/user-attachments/assets/e79f3e3c-3ad0-4773-b7fe-c285114fe46e">




## Getting Started

### Prerequisites
- **Docker**
- **Akka Http**
- **Ollama**
- **Protobuf**
- **Java**
- **Amazon S3** (passkeys are required for doing S3 file IO from local environment)

### Usage
Run this project by starting the server in local

```bash
sbt run LLMServer
```


### Environment
OS: Mac

### IDE: IntelliJ IDEA 2022.2.3 (Ultimate Edition)

### SCALA Version: 2.13.13

[//]: # (SBT Version: 1.10.3)

### Ollama Version: 1.0.79

Running the test file
Test Files can be found under the directory src/test

````
sbt clean compile test
````

## Running the project in local.

1) Clone this repository
```
git clone git@github.com:nithish-kumar-t/LLM-interactions-service.git
```


2) cd to the Project
```
cd LLM-interactions-service
```
3) update the jars
```
sbt clean update
```

4) Create fat jat using assembly
```
sbt assembly
# This will create a fat Jar
```

5) we can then run UT's and FT's using below
```
sbt test
```

6) SBT application can contain multiple mains, this project has 2, so to check the correct main
```
➜LLM-decoder-using-spark git:(feature) ✗ sbt
[info] started sbt server
sbt:LLM-hw2-jar>
[info] * LLMServer
[info] * com.llmServer.service.AutomatedConversationalAgent
[success] Total time: 0 s, completed Dec 1, 2024, 12:06:59 AM

```


## Running the Project in AWS



## Prerequisites

1. **AWS BedRock**: Set up Aws bedrock api to use it in the lamba function.

2. **AWS Account**: Create an AWS account and familiarize yourself with AWS Lambda, EC2, and AWS Api Gateway.

3. **Protobuf**: Ensure that you have the google protobuf module downloaded in the 

4. **Scala, Java and Spark**: Make sure Scala, Java and Hadoop (Scala 2.13.13, Java 11.0.25 Ollama: llama3.2)   are installed and configured correctly.

5. **Git and GitHub**: Use Git for version control and host your project repository on GitHub.

6. **IDE**: Use an Integrated Development Environment (IDE) for coding and development.



## Usage

Follow these steps to execute the project:

1. **Local Setup**: Ensure you downloaded Ollama in Local and pulled model with id **llama3.2**

2. **Configuration**: Set up the necessary configuration parameters for Ollama and AWS Api gateway URI for triggering lambda. 

3. **API Execution**:

   a. Using Postman, trigger the query-llm endpoint. This will initiate a gRPC call to the API Gateway, which in turn triggers the Lambda function. The Lambda function processes the request and returns the serialized response back to the client.

   b. Now trigger start-conversation-agent endpoint, which is the entry point for conversational agent.

4. **Results**: Examine the results obtained from the conversational agent

   a. Conversations are stored in YAML file src/main/resources/conversation-agents/iteration_results-{TIME-STAMP}.yaml



## Unit / Regression Testing

**Code coverage report**


<img width="402" alt="image" src="https://github.com/user-attachments/assets/1916fe44-2369-4f48-979c-dc36cab12358">




