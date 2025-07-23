require 'aws-sdk-s3'
require_relative 'auth_response_helper'
include AuthResponseHelper

def lambda_handler(event:, context:)
  authorizer = event["requestContext"]["authorizer"]["lambda"]
  authorization_error_response = AuthResponseHelper.get_error_response(authorizer)
  return authorization_error_response unless authorization_error_response == nil

  region = context.invoked_function_arn.split(':')[3]
  client = Aws::S3::Client.new(region: region)
  client.put_object(
    body: event["body"],
    bucket: ENV["CONTENT_BUCKET_NAME"],
    key: authorizer["sid"].to_s + "/sources.json"
  )
end
