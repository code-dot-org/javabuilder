require 'aws-sdk-lambda'

require 'json'
require 'jwt'

def lambda_handler(event:, context:)
  puts event
  puts context
  headers = event['headers']
  auth_header = headers['Authorization']
  method_arn = event['methodArn']

  if auth_header.start_with?('Bearer: ')
    jwt_token = auth_header.split[1]
    begin
      decoded_token = JWT.decode(jwt_token, get_public_key(method_arn), true, algorithm: 'RS256')
    rescue JWT::ExpiredSignature
      return generate_deny(nil, method_arn)
    end

    return generate_deny(nil, method_arn) unless decoded_token

    generate_allow('me', method_arn, decoded_token[0])
  else
    generate_deny(nil, method_arn)
  end
end

# Helper function to generate an IAM policy
def generate_policy(principal_id, effect, resource, token_data)
  # Required output:
  auth_response = {}
  auth_response['principalId'] = principal_id
  if effect && resource
    policy_document = {}
    policy_document['Version'] = '2012-10-17' # default version
    policy_document['Statement'] = []
    statement_one = {}
    statement_one['Action'] = 'execute-api:Invoke' # default action
    statement_one['Effect'] = effect
    statement_one['Resource'] = resource
    policy_document['Statement'][0] = statement_one
    auth_response['policyDocument'] = policy_document
  end
  auth_response['context'] = token_data if token_data
  auth_response
end

def generate_allow(principal_id, resource, token_data)
  generate_policy(principal_id, 'Allow', resource, token_data)
end

def generate_deny(principal_id, resource)
  generate_policy(principal_id, 'Deny', resource, nil)
end

def get_public_key(method_arn)
  # method_arn is in format
  # arn:aws:execute-api:region:account-id:api-id/stage-name/$connect
  # We only care about stage--that tells us which key to get (development, staging, etc.)
  tmp = method_arn.split(':')
  api_gateway_arn = tmp[5].split('/')
  stage_id = api_gateway_arn[1]

  key_file = File.open("./public_keys/javabuilder_rsa_#{stage_id}_pub.pem")
  public_key = key_file.read
  OpenSSL::PKey::RSA.new(public_key)
end
