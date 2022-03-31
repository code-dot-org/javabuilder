require_relative 'token_status'

class TokenValidator
  ONE_HOUR_SECONDS = 60 * 60
  ONE_DAY_SECONDS = 24 * 60 * 60
  TOKEN_RECORD_TTL_SECONDS = 120 # 2 minutes
  USER_REQUEST_RECORD_TTL_SECONDS = 24 * 60 * 60 # One day
  TEACHER_ASSOCIATED_REQUEST_TTL_SECONDS = 2 * 60 * 60 # 2 hours

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
      puts "TOKEN VALIDATION ERROR: #{TokenStatus::ALREADY_EXISTS} token_id: #{@token_id}"
      # return false
      return true
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

  # update vs craete
  def teachers_blocked?
    block = true
    @verified_teachers.split(',').each do |teacher_id|
      response = @client.get_item(
        table_name: ENV['blocked_users_table'],
        key: {user_id: blocked_users_section_owner_id(teacher_id)}
      )
      block = false unless response.item
    end

    block
  end

  def user_over_hourly_limit?
    # also check daily limit?
    response = @client.query(
      table_name: ENV['user_requests_table'],
      key_condition_expression: "user_id = :user_id AND issued_at > :one_hour_ago",
      expression_attribute_values: {
        ":user_id" => "#{@origin}##{@user_id}",
        ":one_hour_ago" => Time.now.to_i - ONE_HOUR_SECONDS
      }
    )

    # I think you'd never need to paginate, because you'd be throttled
    # I guess this might not be true in the 24 hour case where we're not throttling
    # iterate if response.last_evaluated_key?
    deny = response.count > ENV['limit_per_hour'].to_i
    if deny
      # logging is kind of gross, looks like
      # [{"ttl"=>0.1648766446e10, "user_id"=>"611", "issued_at"=>0.1648680046e10}, {...
      @client.put_item(
        table_name: ENV['blocked_users_table'],
        item: {
          user_id: blocked_users_user_id,
          request_log: response.items.to_s,
          reason: TokenStatus::USER_OVER_HOURLY_LIMIT
        }
      )
      # deny / return?
    end

    deny
  end

  def user_over_daily_limit?
    response = @client.query(
      table_name: ENV['user_requests_table'],
      key_condition_expression: "user_id = :user_id AND issued_at > :one_day_ago",
      expression_attribute_values: {
        ":user_id" => "#{@origin}##{@user_id}",
        ":one_day_ago" => Time.now.to_i - ONE_DAY_SECONDS
      }
    )

    # I think you'd never need to paginate, because you'd be throttled
    # I guess this might not be true in the 24 hour case where we're not throttling
    # iterate if response.last_evaluated_key?
    deny = response.count > ENV['limit_per_day'].to_i
    if deny
      # logging is kind of gross, looks like
      # [{"ttl"=>0.1648766446e10, "user_id"=>"611", "issued_at"=>0.1648680046e10}, {...
      @client.put_item(
        table_name: ENV['blocked_users_table'],
        item: {
          user_id: blocked_users_user_id,
          request_log: response.items.to_s,
          reason: TokenStatus::USER_OVER_DAILY_LIMIT
        }
      )
    end

    deny
  end

  # update instead of creat??
  def teachers_over_hourly_limit?
    deny = true
    @verified_teachers.split(',').each do |teacher_id|
      response = @client.query(
        table_name: ENV['teacher_associated_requests_table'],
        key_condition_expression: "section_owner_id = :teacher_id AND issued_at > :one_hour_ago",
        expression_attribute_values: {
          ":teacher_id" => "#{@origin}##{teacher_id}",
          ":one_hour_ago" => Time.now.to_i - ONE_HOUR_SECONDS
        }
      )
      # iterate if response.last_evaluated_key

      # check that response.count is less than limit
      # if its under limit for at least one class, we allow
      if response.count > 1
        @client.put_item(
          table_name: ENV['blocked_users_table'],
          item: {
            user_id: blocked_users_section_owner_id(teacher_id),
            request_log: response.items.to_s,
            reason: TokenStatus::TEACHERS_OVER_HOURLY_LIMIT
          }
        )
      else
        deny = false
      end
    end

    deny
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
      client.put_item(
        table_name: ENV['teacher_associated_requests_table'],
        item: {
          section_owner_id: "#{origin}##{teacher_id}",
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

  private

  def blocked_users_section_owner_id(teacher_id)
    "#{@origin}#sectionOwnerId##{teacher_id}"
  end

  def blocked_users_user_id
    "#{@origin}#userId##{@user_id}"
  end
end