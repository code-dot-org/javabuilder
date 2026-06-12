module SessionIdHelper
  # The session ID is the connection ID with any trailing '=' removed, so it can
  # be used as an SQS queue name. SQS queues can only be named with the following
  # characters: alphanumeric characters, hyphens (-), and underscores (_).
  # See https://docs.aws.amazon.com/sdk-for-ruby/v3/api/Aws/SQS/Client.html#create_queue-instance_method
  # The connection ID is base64-encoded, so it can end with one or more '='
  # padding characters. We remove all of them here.
  def get_session_id(event)
    event["requestContext"]["connectionId"].sub(/=+$/, "")
  end
end
