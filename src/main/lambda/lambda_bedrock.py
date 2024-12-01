import json
import base64
import os
import logging
from typing import Dict, Any

import boto3
from botocore.config import Config
from botocore.exceptions import ClientError

import llmQuery_pb2

# Configure logging
logger = logging.getLogger()
logger.setLevel(logging.INFO)

def parse_input(input_data: str) -> Dict[str, str]:
    """
    Parse the base64 encoded input data into a dictionary.

    Args:
        input_data (str): Base64 encoded input string

    Returns:
        Dict[str, str]: Parsed request body
    """
    try:
        # Decode the Proto object, that encoded by client
        input_bytes = base64.b64decode(input_data)
        decoded_string = input_bytes.decode('utf-8')

        # Parse key-value pairs
        req_body = {}
        for param in decoded_string.strip().split("\n"):
            k, v = param.split(":", 1)
            req_body[k.strip()] = v.strip().strip('\"')

        return req_body
    except Exception as e:
        logger.error(f"Error parsing input: {e}")
        raise ValueError(f"Invalid input format: {e}")

def create_bedrock_client(config: Config) -> boto3.client:
    """
    Create and return a Bedrock runtime client.

    Args:
        config (Config): Boto3 client configuration

    Returns:
        boto3.client: Configured Bedrock runtime client
    """
    try:
        return boto3.client(service_name='bedrock-runtime', config=config)
    except Exception as e:
        logger.error(f"Error creating Bedrock client: {e}")
        raise

def invoke_bedrock_model(client, model_arn: str, user_input: str) -> str:
    """
    Invoke the Bedrock model with the given input.

    Args:
        client: Bedrock runtime client
        model_arn (str): ARN of the Bedrock model
        user_input (str): Input prompt for the model

    Returns:
        str: Generated text from the model
    """
    try:
        response = client.invoke_model(
            modelId=model_arn,
            body=json.dumps({
                "prompt": user_input
            }),
            contentType='application/json'
        )

        response_payload = response['body'].read().decode('utf-8')
        response_json = json.loads(response_payload)

        return response_json.get('generation', '')
    except ClientError as e:
        logger.error(f"Bedrock model invocation error: {e}")
        raise

def create_protobuf_response(input_text: str, output_text: str) -> bytes:
    """
    Create a protobuf serialized response.

    Args:
        input_text (str): Original input
        output_text (str): Generated output

    Returns:
        bytes: Serialized protobuf response
    """
    llm_response = llmQuery_pb2.LlmQueryResponse()
    llm_response.input = input_text
    llm_response.output = output_text
    return llm_response.SerializeToString()

def lambda_handler(event: Dict[str, Any], context: Any) -> Dict[str, Any]:
    """
    Lambda function handler for Bedrock model inference.

    Args:
        event (Dict): Lambda event dictionary
        context (Any): Lambda context object

    Returns:
        Dict: Lambda response with status code, body, and headers
    """
    # Configuration setup
    config = Config(
        read_timeout=30000,
        connect_timeout=10,
        retries={
            'max_attempts': 3,
            'mode': 'standard'
        }
    )

    # Set model ARN from environment variable
    model_arn = os.environ.get('BEDROCK_MODEL_ARN',
                               'arn:aws:bedrock:us-east-2:908027374121:inference-profile/us.meta.llama3-1-8b-instruct-v1:0')

    try:
        # Extract and parse input
        input_data = event.get('body', "")
        req_body = parse_input(input_data)

        # Extract parameters
        user_input = req_body.get('input', 'Hello, world!')
        max_words = int(req_body.get('maxWords', 100))

        # Validate model ARN
        if not model_arn:
            raise ValueError('Model ARN not configured')

        # Create Bedrock client and invoke model
        bedrock_client = create_bedrock_client(config)
        generated_text = invoke_bedrock_model(bedrock_client, model_arn, user_input)

        # serializing the response to send back to client
        serialized_response = create_protobuf_response(user_input, generated_text)

        return {
            'statusCode': 200,
            'body': serialized_response,
            'isBase64Encoded': True,
            'headers': {
                'Content-Type': 'application/grpc+proto'
            }
        }

    except Exception as e:
        logger.error(f"Lambda execution error: {e}")
        return {
            'statusCode': 500,
            'body': json.dumps({
                'error': 'Failed to process request',
                'details': str(e)
            })
        }