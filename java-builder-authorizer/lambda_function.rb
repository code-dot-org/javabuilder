require 'aws-sdk-lambda'

require 'json'
require 'jwt'

def lambda_handler(event:, context:, callback: )
  puts event
  headers = event['headers']
  auth_header = headers['Authorization']
  is_valid = false

  if auth_header.start_with?('Bearer: ')
    jwt_token = auth_header.split[1]
    decoded_token = JWT.decode(jwt_token, dev_public_key, true, algorithm: 'RS256')
    callback('Unauthorized') unless decoded_token
    current_time = Time.now.to_i
    token_data = decoded_token[0]
    if token_data['iat'] < current_time && token_data['exp'] > current_time
      is_valid = true
    end
  end

  tmp = event['methodArn'].split(':')
  api_gateway_arn_tmp = tmp[5].split('/')
  aws_account_id = tmp[4]
  region = tmp[3]
  rest_api_id = api_gateway_arn_tmp[0]
  stage = api_gateway_arn_tmp[1]
  method = api_gateway_arn_tmp[2]
  resource = '/' # root resource
  resource += api_gateway_arn_tmp[3] if api_gateway_arn_tmp[3]

  if is_valid
    callback(null, generate_allow('me', event['methodArn']))
  else
    callback('Unauthorized')
  end
end

# Helper function to generate an IAM policy
def generate_policy(principal_id, effect, resource)
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
  auth_response
end

def generate_allow(principal_id, resource)
  generate_policy(principal_id, 'Allow', resource)
end

def generate_deny(principal_id, resource)
  generate_policy(principal_id, 'Deny', resource)
end

def dev_public_key
  key_file = File.open('./public_keys/javabuilder_rsa_dev_public.pem')
  public_key = key_file.read
  OpenSSL::PKey::RSA.new(public_key)
end