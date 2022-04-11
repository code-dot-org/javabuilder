module TokenStatus
  ### Token statuses used in HTTP authorizer (first step in token validation)
  # Token was validated by HTTP authorizer
  VALID_HTTP = 'valid_http'.freeze
  # Token provided to HTTP authorizer has already been used
  ALREADY_EXISTS = 'already_exists'.freeze
  # User has been blocked for violating hourly or daily throttle limits
  USER_BLOCKED = 'user_blocked'.freeze
  # All of a user's teachers (or the teacher themselves, if the user is a teacher)
  # has been blocked for violating hourly throttle limits
  TEACHERS_BLOCKED = 'teachers_blocked'.freeze
  # User has reached the hourly limit for Javabuilder requests.
  USER_OVER_HOURLY_LIMIT = 'user_over_hourly_limit'.freeze
  # User has reached the daily limit for Javabuilder requests.
  USER_OVER_DAILY_LIMIT = 'user_over_daily_limit'.freeze
  # All of a user's teachers (or the teacher themselves, if the user is a teacher)
  # has reached the hourly limit for Javabuilder requests for a classroom.
  TEACHERS_OVER_HOURLY_LIMIT = 'teachers_over_hourly_limit'.freeze

  ### Token statuses used in websocket authorizer (second step in token validation)
  # Token was validated by websocket authorizer
  VALID_WEBSOCKET = 'valid_websocket'.freeze
  # Token provided did not pass through HTTP authorizer first (required)
  UNKNOWN_ID = 'unknown_id'.freeze
  # Token provided was not vetted by hTTP authorizer
  NOT_VETTED = 'not_vetted'.freeze
  # Token has already been used
  USED = 'used'.freeze
end
