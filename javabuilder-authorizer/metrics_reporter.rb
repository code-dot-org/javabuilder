require 'aws-sdk-cloudwatch'
require_relative 'token_status'

class MetricsReporter
  include TokenStatus

  def initialize(context)
    @context = context
    @client = Aws::CloudWatch::Client.new(region: region)
  end

  def log_token_error(status, error_message)
    puts error_message
    metric_name = TOKEN_STATUS_METRIC_NAMES[status]
    log(metric_name)
  end

  def log(metric_name)
    metric_data = {
      metric_name: metric_name,
      dimensions: [
        {
          name: "functionName",
          value: function_name
        }
      ],
      unit: "Count",
      value: 1
    }

    @client.put_metric_data({namespace: "Javabuilder", metric_data: [metric_data]})
  end

  def log_count_with_value(metric_name, value)
    metric_data = {
      metric_name: metric_name,
      dimensions: [
        {
          name: "functionName",
          value: function_name
        }
      ],
      unit: "Count",
      value: value
    }

    @client.put_metric_data({namespace: "Javabuilder", metric_data: [metric_data]})
  end


  private

  # ARN is of the format arn:aws:lambda:{region}:{account_id}:function:{lambda_name}
  def region
    @context.invoked_function_arn.split(':')[3]
  end

  def function_name
    @context.function_name
  end
end
