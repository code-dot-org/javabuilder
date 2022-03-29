module TokenStatus
  # Token statuses used in HTTP authorizer
  VALID_HTTP = 'valid_http'.freeze
  ALREADY_EXISTS = 'already_exists'.freeze

  # Token statuses used in websocket authorizer
  VALID_WEBSOCKET = 'valid_websocket'.freeze
  UNKNOWN_ID = 'unknown_id'.freeze
  NOT_VETTED = 'not_vetted'.freeze
  USED = 'used'.freeze
end