import json
import boto3
import os
import base64
from botocore.config import Config
import llmQuery_pb2


def lambda_handler(event, context):
    # Initialize Bedrock client
    os.environ['BEDROCK_MODEL_ARN'] = 'arn:aws:bedrock:us-east-2:908027374121:inference-profile/us.meta.llama3-1-8b-instruct-v1:0'

    config = Config(
        read_timeout=30000,  # Increased from 10 to 30 seconds
        connect_timeout=10,
        retries={
            'max_attempts': 3,
            'mode': 'standard'
        })

    # Extract input from the event
    print("event")
    print(event)
    input_data = event.get('body', "")
    # user_input = input_data.get('input', 'Hello, world!')

    # input = json.loads(input_data)
    print(input)

    input_bytes = base64.b64decode(input_data)

    # Decode the bytes to a UTF-8 string
    decoded_string = input_bytes.decode('utf-8')
    reqBody = {}

    for param in decoded_string.strip().split("\n"):
        k, v = param.split(":")
        reqBody[k] = v.strip().strip('\"')

    user_input = reqBody.get('input', 'Hello, world!')
    max_words = int(reqBody.get('maxWords', 100))

    bedrock_client = boto3.client(service_name='bedrock-runtime', config=config)


    # Define the model ARN (replace with your specific model ARN)
    model_arn = os.environ.get('BEDROCK_MODEL_ARN')
    # print(model_arn)

    if not model_arn:
        return {
            'statusCode': 500,
            'body': json.dumps('Model ARN not configured.')
        }

    try:
        # Invoke the Bedrock model
        response = bedrock_client.invoke_model(
            modelId=model_arn,
            body=json.dumps({
                "prompt": user_input
                # "max_tokens_to_sample": 100
            }),
            contentType='application/json'
        )

        # Read and decode the response
        response_payload = response['body'].read().decode('utf-8')
        response_json = json.loads(response_payload)

        # Extract the generated text (this depends on the model's response structure)
        generated_text = response_json.get('generation', {})

        print(response_payload)
        print(response_json)

        llm_response = llmQuery_pb2.LlmQueryResponse()
        llm_response.input = user_input
        llm_response.output = generated_text
        serialized_response = llm_response.SerializeToString()

        return {
            'statusCode': 200,
            'body': serialized_response,
            'isBase64Encoded': True,
            'headers': {
                'Content-Type': 'application/grpc+proto'
            }
        }

    except Exception as e:
        return {
            'statusCode': 500,
            'body': json.dumps(f'Error invoking Bedrock model: {str(e)}')
        }