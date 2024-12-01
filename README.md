# LLM-interactions-service


### Author: Nithish Kumar Thathaiahkalva
<!-- ### UIN :  -->
### Email: nthat@uic.edu

##  Description

This project focused on developing a conversational agent using a microservice-based architecture. The agent processes user queries, interacts with a Large Language Model (LLM) deployed on AWS, and generates conversational responses. The implementation includes key components such as an HTTP-based RESTful service, integration with AWS Lambda and API Gateway. The interaction with the conversational agent is fully automated, once a user submits a request, the agent continuously queries the LLM based on responses until the specified condition is satisfied.



##  Project structure
### This Project is continuation of HomeWork-2 where we trained the model and added context to for generating sentences.

1. **Microservice Implementation:**:

   a. **Framework:** Akka HTTP in Scala for creating RESTful endpoints.
   b. **Functionality:** Handles client interactions, processes queries, and communicates with the LLM backend via gRPC and AWS Lambda.

2. **AWS Lambda Function:**
   a. **Purpose:** Hosts the backend LLM logic, utilizing Amazon Bedrock or other preferred models.

   b. **Integration:** Invoked by the microservice through AWS API Gateway using gRPC.


<img width="605" alt="image" src="https://github.com/user-attachments/assets/b38e964c-c7a2-4b46-8b74-7ddae0c62a4a">


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
2) cd to the Project
```
cd LLM-interactions-service
```

3) update the jars
```
sbt clean update
```

4) we can then run UT's and FT's using below
```
sbt test
```

5) SBT application can contain multiple mains, this project has 2, so to check the correct main
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

4. **Scala, Java and Spark**: Make sure Scala, Java and Hadoop (Scala 2.13.13, Java 11.0.25 Ollama: 1.0.79)   are installed and configured correctly.

5. Install Ollama and Download Models 
   Sign Up for Ollama: Create an account on Ollama if you haven't already.
   Install Ollama Local Server: Follow the Ollama Installation Guide.
   Download a Model: For example, download llama3:latest.
```
bash
Copy code
ollama pull llama3:latest
```

6. **Git and GitHub**: Use Git for version control and host your project repository on GitHub.

7. Configure Application Settings

Update the application.conf file with your Ollama and AWS configurations.

```
ollama {
  host = "http://localhost:11434"
  model = "llama3:latest"
  request-timeout-seconds = 500
}

aws {
  region = "your-aws-region"
  access-key = "YOUR_AWS_ACCESS_KEY"
  secret-key = "YOUR_AWS_SECRET_KEY"
}
```

8. **IDE**: Use an Integrated Development Environment (IDE) for coding and development.



## Usage

Follow these steps to execute the project:

1. **Local Setup**: Ensure you downloaded Ollama in Local and pulled model with id **llama3.2**

2. **Configuration**: Set up the necessary configuration parameters for Ollama and AWS Api gateway URI for triggering lambda. 

3. **API Execution**:

   a. Using Postman, trigger the query-llm endpoint. This will initiate a gRPC call to the API Gateway, which in turn triggers the Lambda function. The Lambda function processes the request and returns the serialized response back to the client.

   b. Now trigger start-conversation-agent endpoint, which is the entry point for conversational agent.

4. **Results**: Examine the results obtained from the conversational agent

   a. Conversations are stored in YAML file src/main/resources/conversation-agents/iteration_results-{TIME-STAMP}.yaml



###Data File Formats

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



## Unit / Regression Testing

**Code coverage report**


<img width="402" alt="image" src="https://github.com/user-attachments/assets/1916fe44-2369-4f48-979c-dc36cab12358">




