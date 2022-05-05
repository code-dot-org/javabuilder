require_relative 'token_status'

class TokenValidator
  include TokenStatus

  ONE_HOUR_SECONDS = 60 * 60
  ONE_DAY_SECONDS = 24 * 60 * 60
  TOKEN_RECORD_TTL_SECONDS = 120 # Two minutes
  USER_REQUEST_RECORD_TTL_SECONDS = 25 * ONE_HOUR_SECONDS
  TEACHER_ASSOCIATED_REQUEST_TTL_SECONDS = 25 * ONE_HOUR_SECONDS
  # TO DO: move this into env variable
  # https://codedotorg.atlassian.net/browse/JAVA-536
  TEACHER_HOURLY_LIMIT = 1000
  NEAR_LIMIT_BUFFER = 10

  def initialize(payload, origin, region)
    @token_id = payload['sid']
    @user_id = payload['uid']
    @verified_teachers = payload['verified_teachers']

    @origin = origin
    @client = Aws::DynamoDB::Client.new(region: region)
  end

  def validate
    return error(TOKEN_USED) unless log_token
    return error(USER_BLOCKED) if user_blocked?
    return error(TEACHERS_BLOCKED) if teachers_blocked?
    hourly_usage_response = user_usage(ONE_HOUR_SECONDS)
    return error(USER_OVER_HOURLY_LIMIT) if user_over_hourly_limit?(hourly_usage_response)
    return error(USER_OVER_DAILY_LIMIT) if user_over_daily_limit?
    return error(TEACHERS_OVER_HOURLY_LIMIT) if teachers_over_hourly_limit?
    near_limit_detail = user_near_hourly_limit?(hourly_usage_response.count)

    log_requests
    mark_token_as_vetted
    set_token_warning(NEAR_LIMIT, near_limit_detail) if near_limit_detail
    VALID_HTTP
  end

  private

  def log_token
    begin
      ttl = Time.now.to_i + TOKEN_RECORD_TTL_SECONDS
      @client.put_item(
        table_name: ENV['token_status_table'],
        item: {
          token_id: @token_id,
          ttl: ttl
        },
        condition_expression: 'attribute_not_exists(token_id)'
      )
    rescue Aws::DynamoDB::Errors::ConditionalCheckFailedException
      return false
    end

    true
  end

  def user_blocked?
    response = @client.get_item(
      table_name: ENV['blocked_users_table'],
      key: {user_id: blocked_users_user_id}
    )

    !!response.item
  end

  def teachers_blocked?
    blocked = true
    @verified_teachers.split(',').each do |teacher_id|
      response = @client.get_item(
        table_name: ENV['blocked_users_table'],
        key: {user_id: blocked_users_section_owner_id(teacher_id)}
      )

      # As long as at least one teacher is not blocked,
      # we allow the request through.
      unless response.item
        blocked = false
        break
      end
    end

    blocked
  end

  def user_over_hourly_limit?(hourly_usage_response)
    user_over_limit?(
      hourly_usage_response,
      ENV['limit_per_hour'].to_i,
      USER_OVER_HOURLY_LIMIT
    )
  end

  def user_near_hourly_limit?(hourly_usage_count)
    user_near_limit?(hourly_usage_count, ENV['limit_per_hour'].to_i)
  end

  def user_over_daily_limit?
    usage_response = user_usage(ONE_DAY_SECONDS)
    user_over_limit?(
      usage_response,
      ENV['limit_per_day'].to_i,
      USER_OVER_DAILY_LIMIT
    )
  end

  def teachers_over_hourly_limit?
    over_limit = true

    @verified_teachers.split(',').each do |teacher_id|
      response = @client.query(
        table_name: ENV['teacher_associated_requests_table'],
        key_condition_expression: "section_owner_id = :teacher_id AND issued_at > :one_hour_ago",
        expression_attribute_values: {
          ":teacher_id" => "#{@origin}##{teacher_id}",
          ":one_hour_ago" => Time.now.to_i - ONE_HOUR_SECONDS
        }
      )
      # See user_over_limit? method for notes on pagination
      if response.last_evaluated_key
        puts "teacher_associated_requests query has paginated responses. user_id #{@user_id} teacher_id #{teacher_id}"
      end

      if response.count > TEACHER_HOURLY_LIMIT
        begin
          @client.put_item(
            table_name: ENV['blocked_users_table'],
            item: {
              user_id: blocked_users_section_owner_id(teacher_id),
              request_log: response.items.to_s,
              reason: TEACHERS_OVER_HOURLY_LIMIT
            },
            condition_expression: 'attribute_not_exists(user_id)'
          )
        rescue Aws::DynamoDB::Errors::ConditionalCheckFailedException
          # Do nothing if this teacher has already been added to blocked table
          # (possible if throttling limit was reached by a user on another lambda simultaneously)
        end
      else
        # As long as at least one teacher is under the throttling limit,
        # we allow the request through.
        over_limit = false
        break
      end
    end

    over_limit
  end

  def log_requests
    @client.put_item(
      table_name: ENV['user_requests_table'],
      item: {
        user_id: "#{@origin}##{@user_id}",
        issued_at: Time.now.to_i,
        ttl: Time.now.to_i + USER_REQUEST_RECORD_TTL_SECONDS
      }
    )

    @verified_teachers.split(',').each do |teacher_id|
      @client.put_item(
        table_name: ENV['teacher_associated_requests_table'],
        item: {
          section_owner_id: "#{@origin}##{teacher_id}",
          issued_at: Time.now.to_i,
          ttl: Time.now.to_i + TEACHER_ASSOCIATED_REQUEST_TTL_SECONDS
        }
      )
    end
  end

  def mark_token_as_vetted
    @client.update_item(
      table_name: ENV['token_status_table'],
      key: {token_id: @token_id},
      update_expression: 'SET vetted = :v',
      expression_attribute_values: {':v': true}
    )
  end

  def set_token_warning(key, detail)
    @client.update_item(
      table_name: ENV['token_status_table'],
      key: {token_id: @token_id},
      update_expression: 'SET warning = :w',
      expression_attribute_values: {':w': {key: key, detail: detail}}
    )
  end

  # TO DO: return actual error status instead of valid HTTP
  # when we actually want to throttle. For now, only return error
  # status if a token has already been used.
  def error(status)
    puts "TOKEN VALIDATION ERROR: #{status} user_id: #{@user_id} verified_teachers: #{@verified_teachers} token_id: #{@token_id}"
    return status if status == TOKEN_USED
    # status
    VALID_HTTP
  end

  def user_usage(time_range_seconds)
    response = @client.query(
      table_name: ENV['user_requests_table'],
      key_condition_expression: "user_id = :user_id AND issued_at > :past_time",
      expression_attribute_values: {
        ":user_id" => "#{@origin}##{@user_id}",
        ":past_time" => Time.now.to_i - time_range_seconds
      }
    )
    # DynamoDB query will only read through 1 MB of data before paginating.
    # Our records should be relatively small (roughly 100 bytes, based on AWS docs),
    # so I think we'd need 10K records to hit paginated responses.
    # Once we're actually throttling, I don't think we'd get paginated responses
    # because you'd be throttled before reaching that limit.
    # Logging in the short term if this happens, so we can get a sense if it occurs.
    # TO DO: remove this, or handle paginated responses
    # https://docs.aws.amazon.com/sdk-for-ruby/v3/api/Aws/DynamoDB/Client.html#query-instance_method
    # https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/CapacityUnitCalculations.html
    if response.last_evaluated_key
      puts "user_requests query has paginated responses. user_id #{@user_id}"
    end
    response
  end

  def user_near_limit?(count, limit)
    if count <= limit && count >= (limit - NEAR_LIMIT_BUFFER)
      return {remaining: limit - count}
    else
      return false
    end
  end

  def user_over_limit?(query_response, limit, logging_message)
    over_limit = query_response.count > limit
    if over_limit
      # logging could be improved,
      # [{"ttl"=>0.1648766446e10, "user_id"=>"611", "issued_at"=>0.1648680046e10}, {...
      begin
        @client.put_item(
          table_name: ENV['blocked_users_table'],
          item: {
            user_id: blocked_users_user_id,
            request_log: query_response.items.to_s,
            reason: logging_message
          },
          condition_expression: 'attribute_not_exists(user_id)'
        )
      rescue Aws::DynamoDB::Errors::ConditionalCheckFailedException
        # Do nothing if this user has already been added to blocked table
        # (possible if throttling limit was reached by the same user executing code on another lambda simultaneously)
      end
    end

    over_limit
  end

  def blocked_users_section_owner_id(teacher_id)
    "#{@origin}#sectionOwnerId##{teacher_id}"
  end

  def blocked_users_user_id
    "#{@origin}#userId##{@user_id}"
  end
end
