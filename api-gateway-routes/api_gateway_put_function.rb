require 'aws-sdk-s3'

def lambda_handler(event:, context:)
  region = context.invoked_function_arn.split(':')[3]
  client = Aws::S3::Client.new(region: region)
  client.put_object(
    body: event["body"],
    bucket: ENV["CONTENT_BUCKET_NAME"],
    key: event["requestContext"]["authorizer"]["lambda"]["sid"].to_s + "/sources.json"
  )
end