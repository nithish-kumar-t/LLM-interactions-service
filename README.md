# LLM-interactions-service


### Author: Nithish Kumar Thathaiahkalva
<!-- ### UIN :  -->
### Email: nthat@uic.edu

##  Description

This project focused on developing a conversational agent using a microservice-based architecture. The agent processes user queries, interacts with a Large Language Model (LLM) deployed on AWS, and generates conversational responses. The implementation includes key components such as an HTTP-based RESTful service, integration with AWS Lambda and API Gateway. The interaction with the conversational agent is fully automated, once a user submits a request, the agent continuously queries the LLM based on responses until the specified condition is satisfied.

[Youtube Video Link](https://youtu.be/u3c1odGXp6I)


##  Project structure
### This Project is continuation of HomeWork-2 where we trained the model and added context to for generating sentences.

1. **Microservice Implementation:**:

   a. **Framework:** Akka HTTP in Scala for creating RESTful endpoints.
   b. **Functionality:** Handles client interactions, processes queries, and communicates with the LLM backend via gRPC and AWS Lambda.

2. **AWS Lambda Function:**
   a. **Purpose:** Hosts the backend LLM logic, utilizing Amazon Bedrock or other preferred models.

   b. **Integration:** Invoked by the microservice through AWS API Gateway using gRPC.


![image](https://github.com/user-attachments/assets/ec147e5d-5764-4ea1-ab85-2ae3311ab7a1)







### LLM Server API's

**Conversation agent:**
```
curl -X GET \
  http://localhost:8080/start-conversation-agent \                                                              
  -H "Content-Type: application/json" \
  -d '{"input": "A slow blink from a cat is like a", "maxWords": 100}'
```

**LLM Server:**
```
  http://localhost:8080/query-llm \               
  -H "Content-Type: application/json" \
  -d '{"input": "A slow blink from a cat is like a", "maxWords": 50}'

```



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
2)  **Scala, Java and Llama**: Make sure Scala, Java and Hadoop (Scala 2.13.13, Java 11.0.25 Ollama: 1.0.79)   are installed and configured correctly.

3) cd to the Project
```
cd LLM-interactions-service
```
4) Start the Ollama Server

```
ollama serve
```

5) update the jars
```
sbt clean compile
```

7) we can then run UT's and FT's using below
```
sbt test
```

8) To start the application open LLMServer FIle and click Edit configuration and add cli arguments as **local**
<img width="793" alt="image" src="https://github.com/user-attachments/assets/9d4485f4-06c9-4d52-97a9-84373f968a0b">

9) If above is not working then run the Jar, by passsing environment as local
```
java -jar target/scala-2.13/LLM-hw3-assembly-0.1.0-SNAPSHOT.jar local
```


10) SBT application can contain multiple mains, this project has 2, so to check the correct main
```
➜LLM-decoder-using-spark git:(feature) ✗ sbt
[info] started sbt server
sbt:LLM-hw2-jar>
[info] * LLMServer
[info] * com.llmServer.service.AutomatedConversationalAgent
[success] Total time: 0 s, completed Dec 1, 2024, 12:06:59 AM

```



## Setting up the LLM Client in AWS EC2

1. Create a **EC2** Instance and install all the dependencies

2. Select Ubuntu or Amazon linux, Processing Instance Unit Type (t3.Medium) , as We need atleast 4GB memory because the smallest Ollama needs ~3GB to run.

3. After creating the instance SSH the instance into your local

```
   ssh -i "{credentials-path}" {EC2-public URL}
```
4. Inatall, Java, Docker and Ollama into the EC2 from terminal

<img width="1512" alt="image" src="https://github.com/user-attachments/assets/7b59f30a-5270-43f8-b828-16f85eb80aa6">

5. Once everything is setup run the jar using below command from SSH terminal

```
   java -jar LLM-hw3-assembly-0.1.0-SNAPSHOT.jar cloud

```

6. Now using postman we can run these 2 API's

-   Conversational Agent
   ```
   curl --location 'http://ec2-18-226-82-140.us-east-2.compute.amazonaws.com:8080/start-conversation-agent' \
   --header 'Content-Type: application/json' \
   --data '{
       "input": "The curious cat leaps onto the high fence",
       "maxWords": 100
   }'
   ```
- LLM to Lambda-Bedrock
   
   ```
   curl --location 'http://ec2-18-226-82-140.us-east-2.compute.amazonaws.com:8080/query-llm' \
   --header 'Content-Type: application/json' \
   --data '{
       "input": "A slow blink from a cat is like a",
       "maxWords": 100
   }'
   ```

7. The COnversations from the Ollama will be stored on AWS s3

```
s3://nithish-llm-hw3/output/conversation-agents/
```




## Usage

Follow these steps to execute the project:

1. **Local Setup**: Ensure you downloaded Ollama in Local and pulled model with id **llama3.2**

2. **Configuration**: Set up the necessary configuration parameters for Ollama and AWS Api gateway URI for triggering lambda. 

3. **API Execution**:

   a. Using Postman, trigger the query-llm endpoint. This will initiate a gRPC call to the API Gateway, which in turn triggers the Lambda function. The Lambda function processes the request and returns the serialized response back to the client.

   b. Now trigger start-conversation-agent endpoint, which is the entry point for conversational agent.

4. **Results**: Examine the results obtained from the conversational agent

   a. Conversations are stored in YAML file src/main/resources/conversation-agents/iteration_results-{TIME-STAMP}.yaml



### Data File Formats

The conversational interactions and statistics are logged in YAML or CSV formats. Example structures are as follows:

YAML Example:

yaml

```
Iteration-0:
  question: 'A slow blink from a cat is like a '
  LLM Response: |2-
     human smile. It's a sign of contentment and relaxation. Cats often blink slowly when they're feeling calm and happy, and it's a way for them to communicate their peacefulness to their           human companions......
  Ollama Response: |-
     a warm invitation to relax and be yourself. It's like receiving a feline hug, wrapped in calmness and trust. When your cat slows down their blink, it means they're choosing to let their         guard down around you, to feel comfortable enough with you that they can let their true feelings shine through.
    Take this as an opportunity to deepen the connection with your feline friend, to create a sense of mutual understanding and respect. Return the slow blink, and see how it makes your cat's 
Iteration-1:
  question: "Do you have any comments on ...a warm invitation to relax and be yourself.\
    \ It's like receiving a feline hug, wrapped in calmness and trust. When your cat\
    \ slows down their blink, it means they're choosing to let their guard down around\
    .....
  LLM Response: |2-
     It's a reminder that sometimes, all we need is a little bit of calmness and trust to feel truly seen and understood."}
     {"answer": "The slow blink is a sign of trust and relaxation in cats, and it's a special way for them to connect with their human companions. When a cat slows down their blink, it means         they're feeling comfortable and at ease around you, and it's an invitation to deepen the connection and build trust. By returning the slow blink, you can strengthen the bond with your cat       and experience the joy of being in their presence. It's a
  Ollama Response: |-
    Here's a potential response:
    "I love this! The idea that our cats are mirroring us when they slow down their blink is fascinating. I've noticed that when my own eyes narrow or relax, it seems to trigger a similar           response from my cat. It's as if we're both communicating on a deeper level, and the slow blink has become a special language between us.

statistics:
  total_interactions: 2
  total_time_seconds: 34
  termination_condition: Maximum Interactions Reached
```

## Steps to Running the Project in Docker


### 1. Install Docker
   Docker must be installed on the system.

### 2. Set Up Ollama Container and execute below steps

```
 1. Pull the Ollama container:
   docker pull ollama/ollama
   
⁠ 2. Run the Ollama container:
    docker run -d -p 11434:11434 --name ollama-container ollama/ollama
   
⁠ 3. Access the container and Install Ollama version 3.2:
    docker exec -it ollama-container bash
    ollama pull llama3.2
```

### 3. Build the sbt Application
   1. Create the JAR file using `sbt`:
      ```
      sbt assembly
      ```
   
⁠   2. Build the Docker image:
      ```
      docker build -t cs-441-LLM-interaction-service .
      ```

   3. Ensure jar is accessible from cmd
      ```
      java -jar target/scala-2.13/LLM-hw3-assembly-0.1.0-SNAPSHOT.jar local
      ```


### 4. Run the Dockerized Application
   Start the application container, linking it with the Ollama container:
   ```
   docker run -d -p 8080:8080 --name cs-441-LLM-container --link ollama-container cs-441-LLM-interaction-service
   ```
  

**We can run the docker from postman or by curl requests.**

## Unit / Regression Testing

**Code coverage report**


<img width="402" alt="image" src="https://github.com/user-attachments/assets/1916fe44-2369-4f48-979c-dc36cab12358">




