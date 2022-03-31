require 'aws-sdk-lambda'
require 'aws-sdk-dynamodb'
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
  token_status = get_token_status(context, token_payload, standardized_origin)
  return JwtHelper.generate_deny(route_arn) unless token_status == TokenStatus::VALID_HTTP

  JwtHelper.generate_allow(route_arn, token_payload)
end

def get_token_status(context, token_payload, origin)
  region = get_region(context)
  validator = TokenValidator.new(token_payload, origin, region)

  puts TokenStatus::ALREADY_EXISTS unless validator.log_token
  puts TokenStatus::USER_BLOCKED if validator.user_blocked?
  puts TokenStatus::TEACHERS_BLOCKED if validator.teachers_blocked?
  puts TokenStatus::USER_OVER_HOURLY_LIMIT if validator.user_over_hourly_limit?
  puts TokenStatus::USER_OVER_DAILY_LIMIT if validator.user_over_daily_limit?
  puts TokenStatus::TEACHERS_OVER_HOURLY_LIMIT if validator.teachers_over_hourly_limit?
  validator.log_token
  validator.mark_token_as_vetted

  TokenStatus::VALID_HTTP
end

# ARN is of the format arn:aws:lambda:{region}:{account_id}:function:{lambda_name}
def get_region(context)
  context.invoked_function_arn.split(':')[3]
end
