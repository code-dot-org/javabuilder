require 'aws-sdk-lambda'
require 'jwt'
require_relative 'jwt_helper'
require_relative 'token_status'
require_relative 'token_validator'
include JwtHelper
include TokenStatus

# We set up AWS to delete per-token dynamodb records 120 seconds after creation
TOKEN_RECORD_TTL_SECONDS = 120

# The lambda handler takes an event with the query string parameter 'Authorization=token',
# where the token is a JWT token generated by dashboard. It checks the validity of the
# token and returns a policy that either allows or disallows the user from uploading their
# content to Javabuilder. This authorizer is specialized to work with AWS API Gateway HTTP
# APIs.
def lambda_handler(event:, context:)
  origin = event['headers']['origin']
  jwt_token = event['queryStringParameters']['Authorization']
  route_arn = event['routeArn']

  standardized_origin = JwtHelper.get_standardized_origin(origin)
  decoded_token = JwtHelper.decode_token(jwt_token, standardized_origin)
  return JwtHelper.generate_deny(route_arn) unless decoded_token

  token_payload = decoded_token[0]
  token_status = get_token_status(token_payload, standardized_origin, context)
  return JwtHelper.generate_allow_with_error(route_arn, token_status) unless token_status == TokenStatus::VALID_HTTP

  JwtHelper.generate_allow(route_arn, token_payload)
end

def get_token_status(token_payload, origin, context)
  validator = TokenValidator.new(token_payload, origin, context)
  validator.validate
end
