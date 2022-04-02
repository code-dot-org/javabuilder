require 'aws-sdk-lambda'
require 'aws-sdk-dynamodb'
require 'jwt'
require_relative 'jwt_helper'
require_relative 'token_status'
include JwtHelper
include TokenStatus

# The lambda handler takes an event with the query string parameter 'Authorization=token',
# where the token is a JWT token generated by dashboard or a string indicating this is
# a basic connection test from a user. It checks the validity of the token and returns
# a policy that either allows or disallows the user from continuing to Javabuilder. This
# authorizer is specialized to work with AWS API Gateway WebSocket APIs.
def lambda_handler(event:, context:)
  origin = event['headers']['Origin']
  jwt_token = event['queryStringParameters']['Authorization']
  method_arn = event['methodArn']
  # Return early if this is the user connectivity test
  if jwt_token == 'connectivityTest'
    return JwtHelper.generate_policy('connectivityTest', "Allow", method_arn, {connectivityTest: true})
  end

  standardized_origin = JwtHelper.get_standardized_origin(origin)
  decoded_token = JwtHelper.decode_token(jwt_token, standardized_origin)
  return JwtHelper.generate_deny(method_arn) unless decoded_token

  token_payload = decoded_token[0]
  token_status = get_token_status(context, token_payload['sid'])
  return JwtHelper.generate_deny(method_arn) unless token_status == TokenStatus::VALID_WEBSOCKET

  JwtHelper.generate_allow(method_arn, token_payload)
end

def get_token_status(context, sid)
  client = Aws::DynamoDB::Client.new(region: get_region(context))
  response = client.get_item(
    table_name: ENV['token_status_table'],
    key: {token_id: sid}
  )
  item = response.item

  unless item
    puts "TOKEN VALIDATION ERROR: #{TokenStatus::UNKNOWN_ID} token_id: #{sid}"
    # return TokenStatus::UNKNOWN_ID
    return TokenStatus::VALID_WEBSOCKET
  end

  if item['used']
    puts "TOKEN VALIDATION ERROR: #{TokenStatus::USED} token_id: #{sid}"
    # return TokenStatus::USED
    return TokenStatus::VALID_WEBSOCKET
  end

  unless item['vetted']
    puts "TOKEN VALIDATION ERROR: #{TokenStatus::NOT_VETTED} token_id: #{sid}"
    # return TokenStatus::NOT_VETTED
    return TokenStatus::VALID_WEBSOCKET
  end

  client.update_item(
    table_name: ENV['token_status_table'],
    key: {token_id: sid},
    update_expression: 'SET used = :u',
    expression_attribute_values: {':u': true}
  )

  TokenStatus::VALID_WEBSOCKET
end

# ARN is of the format arn:aws:lambda:{region}:{account_id}:function:{lambda_name}
def get_region(context)
  context.invoked_function_arn.split(':')[3]
end
