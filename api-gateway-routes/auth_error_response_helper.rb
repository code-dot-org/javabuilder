module AuthErrorResponseHelper
  AUTHORIZER_KEY = 'AUTHORIZER'.freeze
  AUTHORIZATION_ERROR_KEY = "authorization_error".freeze
  AUTHORIZATION_ERROR_CODE_KEY = "authorization_error_code".freeze

  def get_response(authorizer_payload)
    return nil unless authorizer_payload[AUTHORIZATION_ERROR_KEY] && authorizer_payload[AUTHORIZATION_ERROR_CODE_KEY]

    body = {
      type: AUTHORIZER_KEY,
      value: authorizer_payload[AUTHORIZATION_ERROR_KEY]
    }
    { statusCode: authorizer_payload[AUTHORIZATION_ERROR_CODE_KEY], body: body.to_json }
  end
end
