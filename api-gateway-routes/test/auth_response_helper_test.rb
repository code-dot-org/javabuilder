require 'json'
require 'minitest/autorun'
require_relative '../auth_response_helper'
include AuthResponseHelper

class AuthResponseHelperTest < Minitest::Test
   def test_creates_correct_warning_payload
      warning_message = 'NEAR_LIMIT'
      request_payload = {}
      request_payload['authorization_warning'] = warning_message
      request_payload['authorization_warning_detail'] = {
        'remaining': 5
      }.to_json
      response = AuthResponseHelper.get_warning_response(request_payload)
      assert response != nil
      object_response = JSON.parse(response)
      assert object_response['type'] == AuthResponseHelper::AUTHORIZER_KEY
      assert object_response['value'] == warning_message
   end
end
