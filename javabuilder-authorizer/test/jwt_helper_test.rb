require 'minitest/autorun'
require_relative '../jwt_helper'
include JwtHelper

class JwtHelperTest < Minitest::Test
   def test_generates_allow_policy
    user_id = "user_id123"
    issuer = "issuer123"
    token_payload = {}
    token_payload['uid'] = user_id
    token_payload['iss'] = issuer
    resource = "resource123"

    auth_response = JwtHelper.generate_allow(resource, token_payload)

    assert auth_response != nil
    assert_equal "#{issuer}/#{user_id}", auth_response['principalId']
    assert_equal token_payload, auth_response['context']

    policy_document = auth_response['policyDocument']
    statement = policy_document['Statement'][0]
    assert_equal 'Allow', statement['Effect']
    assert_equal resource, statement['Resource']
   end
end
