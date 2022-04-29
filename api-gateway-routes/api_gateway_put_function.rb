require 'aws-sdk-s3'
require_relative 'auth_error_response_helper'
include AuthErrorResponseHelper

def lambda_handler(event:, context:)
  authorizer = event["requestContext"]["authorizer"]["lambda"]
  authorization_error_response = AuthErrorResponseHelper.get_response(authorizer)
  return authorization_error_response unless authorization_error_response.nil?

  region = context.invoked_function_arn.split(':')[3]
  client = Aws::S3::Client.new(region: region)
  client.put_object(
    body: event["body"],
    bucket: ENV["CONTENT_BUCKET_NAME"],
    key: "#{authorizer['sid']}/sources.json"
  )
end
