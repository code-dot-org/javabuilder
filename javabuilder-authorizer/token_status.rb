module TokenStatus
  ### Token statuses used in HTTP authorizer (first step in token validation)
  # Token was validated by HTTP authorizer
  VALID_HTTP = 'valid_http'.freeze
  # Token provided to HTTP authorizer has already been used
  ALREADY_EXISTS = 'already_exists'.freeze

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
