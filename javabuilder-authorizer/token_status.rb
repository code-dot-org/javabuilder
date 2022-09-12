module TokenStatus
  ### Token statuses used in HTTP authorizer (first step in token validation)
  # Token was validated by HTTP authorizer
  VALID_HTTP = 'VALID_HTTP'.freeze
  # User has been blocked for violating hourly or daily throttle limits
  USER_BLOCKED = 'USER_BLOCKED'.freeze
  # All of a user's teachers (or the teacher themselves, if the user is a teacher)
  # has been blocked for violating hourly throttle limits
  CLASSROOM_BLOCKED = 'CLASSROOM_BLOCKED'.freeze
  # User has reached the hourly limit for Javabuilder requests.
  USER_OVER_HOURLY_LIMIT = 'USER_OVER_HOURLY_LIMIT'.freeze
  # User has reached the daily limit for Javabuilder requests.
  USER_OVER_DAILY_LIMIT = 'USER_OVER_DAILY_LIMIT'.freeze
  # All of a user's teachers (or the teacher themselves, if the user is a teacher)
  # has reached the hourly limit for Javabuilder requests for a classroom.
  TEACHERS_OVER_HOURLY_LIMIT = 'TEACHERS_OVER_HOURLY_LIMIT'.freeze
  # A user is near their throttling limit. Token is valid but a warning should be sent.
  NEAR_LIMIT = 'NEAR_LIMIT'.freeze

  ### Token statuses used in websocket authorizer (second step in token validation)
  # Token was validated by websocket authorizer
  VALID_WEBSOCKET = 'VALID_WEBSOCKET'.freeze
  # Token provided did not pass through HTTP authorizer first (required)
  UNKNOWN_ID = 'UNKNOWN_ID'.freeze
  # Token provided was not vetted by hTTP authorizer
  NOT_VETTED = 'NOT_VETTED'.freeze

  ### Token status used by both authorizers
  # Token provided to the authorizer has already been used
  TOKEN_USED = 'TOKEN_USED'.freeze

  ERROR_STATES = [USER_BLOCKED, CLASSROOM_BLOCKED, USER_OVER_DAILY_LIMIT, USER_OVER_HOURLY_LIMIT, TEACHERS_OVER_HOURLY_LIMIT, UNKNOWN_ID, NOT_VETTED, TOKEN_USED]
  WARNING_STATES = [NEAR_LIMIT]
  VALID_STATES = [VALID_HTTP, VALID_WEBSOCKET]

  TOKEN_STATUS_METRIC_NAMES = {
    USER_BLOCKED => 'UserBlocked',
    CLASSROOM_BLOCKED => 'ClassroomBlocked',
    UNKNOWN_ID => 'TokenUnknownId',
    NOT_VETTED => 'TokenNotVetted',
    TOKEN_USED => 'TokenUsed'
  }.freeze

  NEW_USER_BLOCKED = 'NewUserBlocked'.freeze
  NEW_CLASSROOM_BLOCKED = 'NewClassroomBlocked'.freeze
  CLASSROOM_HOURLY_REQUEST_COUNT = 'ClassroomHourlyRequestCount'.freeze
end
