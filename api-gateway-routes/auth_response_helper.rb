module AuthResponseHelper
  AUTHORIZER_KEY = 'AUTHORIZER'.freeze
  AUTHORIZATION_ERROR_KEY = "authorization_error".freeze
  AUTHORIZATION_ERROR_CODE_KEY = "authorization_error_code".freeze
  AUTHORIZATION_WARNING_KEY = "authorization_warning".freeze
  AUTHORIZATION_WARNING_DETAIL_KEY = "authorization_warning_detail".freeze

  def get_error_response(authorizer_payload)
    return nil unless authorizer_payload[AUTHORIZATION_ERROR_KEY] && authorizer_payload[AUTHORIZATION_ERROR_CODE_KEY]

    body = {
      type: AUTHORIZER_KEY,
      value: authorizer_payload[AUTHORIZATION_ERROR_KEY]
    }
    { statusCode: authorizer_payload[AUTHORIZATION_ERROR_CODE_KEY], body: body.to_json }
  end

  def get_warning_response(authorizer_payload)
    return nil unless authorizer_payload[AUTHORIZATION_WARNING_KEY] && authorizer_payload[AUTHORIZATION_WARNING_DETAIL_KEY]
    
    return {
      type: AUTHORIZER_KEY,
      value: authorizer_payload[AUTHORIZATION_WARNING_KEY],
      detail: JSON.parse(authorizer_payload[AUTHORIZATION_WARNING_DETAIL_KEY])
  }.to_json
  end
end
