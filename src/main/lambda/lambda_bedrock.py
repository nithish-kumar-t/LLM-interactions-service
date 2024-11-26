import json
import boto3
import os
import logging


# Configure logging
logger = logging.getLogger()
logger.setLevel(logging.INFO)

def lambda_handler(event, context):
    # Initialize Bedrock client
    os.environ['BEDROCK_MODEL_ARN'] = 'arn:aws:bedrock:us-east-2:908027374121:inference-profile/us.meta.llama3-2-1b-instruct-v1:0'

    bedrock_client = boto3.client(service_name='bedrock-runtime')

    # Extract input from the event
    user_input = event.get('input', 'Hello, world!')

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
            }),
            contentType='application/json'
        )

        # Read and decode the response
        response_payload = response['body'].read().decode('utf-8')
        response_json = json.loads(response_payload)

        # Extract the generated text (this depends on the model's response structure)
        generated_text = response_json.get('generation', {})
        logger.info(response_payload)

        return {
            'statusCode': 200,
            'body': json.dumps({
                'input': user_input,
                'output': generated_text
            })
        }

    except Exception as e:
        return {
            'statusCode': 500,
            'body': json.dumps(f'Error invoking Bedrock model: {str(e)}')
        }