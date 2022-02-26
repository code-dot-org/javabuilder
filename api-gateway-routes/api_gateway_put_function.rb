require 'aws-sdk-s3'

def lambda_handler(event:, context:)
  region = context.invoked_function_arn.split(':')[3]
  client = Aws::S3::Client.new(region: region)
  client.put_object(
    body: event["body"],
    bucket: ENV["OUTPUT_BUCKET_NAME"],
    key: event["pathParameters"]["id"] + "/sources.json"
  )
end