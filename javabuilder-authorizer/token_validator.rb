require_relative 'token_status'

class TokenValidator
  ONE_HOUR_SECONDS = 60 * 60
  ONE_DAY_SECONDS = 24 * 60 * 60
  TOKEN_RECORD_TTL_SECONDS = 120 # 2 minutes
  USER_REQUEST_RECORD_TTL_SECONDS = ONE_DAY_SECONDS
  # TO DO: update this value to two hours once we've evaluated a classroom-level
  # throttling threshold.
  TEACHER_ASSOCIATED_REQUEST_TTL_SECONDS = 14 * ONE_DAY_SECONDS

  def initialize(payload, origin, region)
    @token_id = payload['sid']
    @user_id = payload['uid']
    @verified_teachers = payload['verified_teachers']

    @origin = origin
    @client = Aws::DynamoDB::Client.new(region: region)
  end

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
      @status = TokenStatus::ALREADY_EXISTS
      return false
    end

    true
  end

  def user_blocked?
    response = @client.get_item(
      table_name: ENV['blocked_users_table'],
      key: {user_id: blocked_users_user_id}
    )

    blocked = !!response.item
    @status = TokenStatus::USER_BLOCKED if blocked
    blocked
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

    @status = TokenStatus::TEACHERS_BLOCKED if blocked
    blocked
  end

  def user_over_hourly_limit?
    user_over_limit?(
      ONE_HOUR_SECONDS,
      ENV['limit_per_hour'].to_i,
      TokenStatus::USER_OVER_HOURLY_LIMIT
    )
  end

  def user_over_daily_limit?
    user_over_limit?(
      ONE_DAY_SECONDS,
      ENV['limit_per_day'].to_i,
      TokenStatus::USER_OVER_DAILY_LIMIT
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

      # TO DO: set actual limit value
      # Setting arbitrary super high value temporarily.
      if response.count > 1_000_000
        begin
          @client.put_item(
            table_name: ENV['blocked_users_table'],
            item: {
              user_id: blocked_users_section_owner_id(teacher_id),
              request_log: response.items.to_s,
              reason: TokenStatus::TEACHERS_OVER_HOURLY_LIMIT
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

    @status = TokenStatus::TEACHERS_OVER_HOURLY_LIMIT if over_limit
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
    @status = TokenStatus::VALID_HTTP
  end

  # TO DO: return actual error status instead of valid HTTP
  # when we actually want to throttle.
  def error_message
    "TOKEN VALIDATION ERROR: #{@status} user_id: #{@user_id} verified_teachers: #{@verified_teachers} token_id: #{@token_id}"
    TokenStatus::VALID_HTTP
  end

  private

  def user_over_limit?(time_range_seconds, limit, logging_message)
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

    over_limit = response.count > limit
    if over_limit
      # logging could be improved,
      # [{"ttl"=>0.1648766446e10, "user_id"=>"611", "issued_at"=>0.1648680046e10}, {...
      begin
        @client.put_item(
          table_name: ENV['blocked_users_table'],
          item: {
            user_id: blocked_users_user_id,
            request_log: response.items.to_s,
            reason: logging_message
          },
          condition_expression: 'attribute_not_exists(user_id)'
        )
      rescue Aws::DynamoDB::Errors::ConditionalCheckFailedException
        # Do nothing if this user has already been added to blocked table
        # (possible if throttling limit was reached by the same user executing code on another lambda simultaneously)
      end
    end

    @status = logging_message if over_limit
    over_limit
  end

  def blocked_users_section_owner_id(teacher_id)
    "#{@origin}#sectionOwnerId##{teacher_id}"
  end

  def blocked_users_user_id
    "#{@origin}#userId##{@user_id}"
  end
end
