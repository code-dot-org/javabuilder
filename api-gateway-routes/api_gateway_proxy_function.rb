require 'json'
require 'aws-sdk-sqs'
require 'aws-sdk-lambda'
require 'aws-sdk-apigatewaymanagementapi'
require 'uri'
require_relative 'auth_response_helper'
include AuthResponseHelper

MAX_SQS_RETRIES = 3
INITIAL_RETRY_SLEEP_S = 0.5
CONNECTED_MESSAGE = 'CONNECTED'.freeze

def lambda_handler(event:, context:)
  routeKey = event["requestContext"]["routeKey"]
  if routeKey == "$connect"
    return on_connect(event, context)
  elsif routeKey == "$disconnect"
    return on_disconnect(event, context)
  else
    return on_default(event, context)
  end
end

def on_connect(event, context)
  region = get_region(context)

  request_context = event["requestContext"]
  authorizer = request_context['authorizer']

  authorization_error_response = AuthResponseHelper.get_error_response(authorizer)
  # If there is an authorization error, allow the websocket connection to open,
  # but early return so we don't actually invoke the Build and Run lambda and try
  # to create an SQS queue. We will send the auth error message via the websocket
  # once the connection has been successfully made (in on_default).
  # This should only happen in the rare scenario that a request/token is valid
  # for the HTTP authorizer and API, but somehow becomes invalid when the websocket
  # connection is made.
  if authorization_error_response
    # Add some logging to make sure we know this is happening.
    puts get_formatted_log("AUTHORIZATION ERROR: #{authorization_error_response[:body]}", request_context, nil)
    return { statusCode: 200, body: authorization_error_response[:body] }
  end

  # log entire request context object for debugging
  puts get_formatted_log("CONNECT REQUEST CONTEXT: #{request_context}", request_context, nil)

  # Return early if this is the user connectivity test
  if is_connectivity_test(authorizer)
    return { statusCode: 200, body: "connection successful" }
  end

  # -- Create SQS for this session --
  sqs_client = Aws::SQS::Client.new(region: region, retry_mode: "adaptive")
  queue_name = get_session_id(event) + '.fifo'
  # Create the queue with retries if we get throttled
  sqs_queue = sqs_operation_with_retries(:create_queue, request_context, sqs_client, queue_name)

  # -- Create Lambda for this session --
  lambda_client = Aws::Lambda::Client.new(region: region)
  payload = {
    :queueUrl => sqs_queue.queue_url,
    :connectionId => request_context["connectionId"],
    :levelId => authorizer["level_id"],
    :options => authorizer["options"],
    :iss => authorizer["iss"],
    :channelId => authorizer["channel_id"],
    :miniAppType => authorizer["mini_app_type"],
    :javabuilderSessionId => authorizer['sid'],
    :queueName => queue_name,
    :executionType => authorizer['execution_type'],
    :canAccessDashboardAssets => authorizer['can_access_dashboard_assets']
  }

  response = nil
  function_name = nil
  if authorizer['mini_app_type'] == 'neighborhood'
    function_name = ENV['BUILD_AND_RUN_NEIGHBORHOOD_PROJECT_LAMBDA_ARN']
  elsif authorizer['mini_app_type'] == 'console'
    function_name = ENV['BUILD_AND_RUN_CONSOLE_PROJECT_LAMBDA_ARN']
  elsif authorizer['mini_app_type'] == 'theater'
    function_name = ENV['BUILD_AND_RUN_THEATER_PROJECT_LAMBDA_ARN']
  else
    # log so we know we saw an invalid mini app
    invalid_message = "invalid mini-app"
    puts get_formatted_log(invalid_message, request_context, 400)
    return { statusCode: 400, body: invalid_message }
  end

  response = lambda_client.invoke({
    function_name: function_name,
    invocation_type: 'Event',
    payload: JSON.generate(payload)
  })

  { statusCode: response['status_code'], body: "done" }
end

def on_disconnect(event, context)
  authorizer = event["requestContext"]["authorizer"]
  authorization_error_response = AuthResponseHelper.get_error_response(authorizer)
  # The return value is not actually interpreted by the client, but early return here so that
  # we don't try to delete a queue that doesn't exist.
  return authorization_error_response if authorization_error_response

  sqs = Aws::SQS::Client.new(region: get_region(context), retry_mode: "adaptive")
  request_context = event['requestContext']

  # Delete the queue with retries if we get throttled
  sqs_operation_with_retries(:delete_queue, request_context, sqs, event, context)

  { statusCode: 200, body: "success"}
end

def on_default(event, context)
  client = Aws::ApiGatewayManagementApi::Client.new(
    region: get_region(context),
    endpoint: get_api_endpoint(event, context)
  )
  connection_id = event["requestContext"]["connectionId"]

  authorizer = event["requestContext"]["authorizer"]
  authorization_error_response = AuthResponseHelper.get_error_response(authorizer)
  # If there is an authorization error, send the error message via the websocket
  # and return early
  if authorization_error_response
    resp = client.post_to_connection({
      data: authorization_error_response[:body],
      connection_id: connection_id
    })
    return authorization_error_response
  end

  message = event["body"]
  # Return early if this is the user connectivity test
  if message == 'connectivityTest'
    resp = client.post_to_connection({
      data: "success",
      connection_id: connection_id
    })
    return { statusCode: 200, body: "success"}
  end

  # Return early if this is the initial "CONNECTED" message. Send authorization warning if needed.
  if message == CONNECTED_MESSAGE
    authorization_warning_response = AuthResponseHelper.get_warning_response(authorizer)
    # if there is a warning response, send the warning via the websocket before returning.
    if authorization_warning_response
      resp = client.post_to_connection({
        data: authorization_warning_response,
        connection_id: connection_id
      })
    end
    return { statusCode: 200, body: "success" }
  end

  sqs = Aws::SQS::Client.new(region: get_region(context))
  sqs.send_message(
    queue_url: get_sqs_url(event, context),
    message_body: message,
    message_deduplication_id: SecureRandom.uuid.to_str.gsub("-", ""),
    message_group_id: get_session_id(event),
  )

  { statusCode: 200, body: "success"}
end

def get_api_endpoint(event, context)
  request_context = event["requestContext"]
  "https://#{request_context['apiId']}.execute-api.#{get_region(context)}.amazonaws.com/#{request_context['stage']}"
end

# ARN is of the format arn:aws:lambda:{region}:{account_id}:function:{lambda_name}
def get_region(context)
  context.invoked_function_arn.split(':')[3]
end

# SQS queues can only be named with the following characters:
# alphanumeric characters, hyphens (-), and underscores (_)
# See https://docs.aws.amazon.com/sdk-for-ruby/v3/api/Aws/SQS/Client.html#create_queue-instance_method
# The connection ID always ends with an '='. We remove that here so we can use the connection ID as
# our session ID.
def get_session_id(event)
  event["requestContext"]["connectionId"].delete_suffix("=")
end

def get_sqs_url(event, context)
  region = get_region(context)
  # ARN is of the format arn:aws:lambda:{region}:{account_id}:function:{lambda_name}
  account_id = context.invoked_function_arn.split(':')[4]
  connection_id = get_session_id(event)
  "https://sqs.#{region}.amazonaws.com/#{account_id}/#{connection_id}.fifo"
end

def is_connectivity_test(authorizer)
  !!(authorizer && authorizer['connectivityTest'])
end

def sqs_operation_with_retries(sqs_operation, request_context, *operation_params)
  retries = 0
  sleep_time = INITIAL_RETRY_SLEEP_S
  begin
    method(sqs_operation).call(*operation_params)
  rescue Aws::SQS::Errors::RequestThrottled => e
    raise(e) if retries >= MAX_SQS_RETRIES
    retries += 1
    sleep(sleep_time)
    puts get_formatted_log("RETRY #{retries}, just slept for #{sleep_time} seconds", request_context, nil)
    # double sleep time for next retry
    sleep_time = sleep_time * 2
    retry
  end
end

def create_queue(sqs_client, queue_name)
  sqs_queue = sqs_client.create_queue(
    queue_name: queue_name,
    attributes: {"FifoQueue" => "true"}
  )
end

def delete_queue(sqs_client, event, context)
  # Handle if queue does not exist,
  # such as in case of connectivity test.
  # The NonExistentQueue error is not documented in the AWS SDK,
  # but was observed in errors accumulated from our connectivity tests.
  begin
    sqs_client.delete_queue(queue_url: get_sqs_url(event, context))
  rescue Aws::SQS::Errors::NonExistentQueue => e
    request_context = event['requestContext']
    authorizer = request_context['authorizer']

    # This exception is expected during connectivity tests,
    # so do not log in those cases.
    unless is_connectivity_test(authorizer)
      puts get_formatted_log("DISCONNECT ERROR: #{e.message}", request_context, nil)
    end
  end
end

def get_formatted_log(log_message, request_context, status_code)
  log_data = {
    connectionId: request_context['connectionId'],
    message: log_message
  }
  log_data[:status_code] = status_code if status_code
  log_data.to_json
end