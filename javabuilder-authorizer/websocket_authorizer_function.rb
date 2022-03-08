require 'aws-sdk-lambda'
require 'jwt'

# The lambda handler takes an event with the query string parameter
# 'Authorization=token',  where the token is a JWT token generated
# by dashboard. The handler verifies the token with the appropriate
# public key (dependant on the environment the request came from),
# and checks the token has not expired and its issue time is not in the future.
def lambda_handler(event:, context:)
  origin = event['headers']['Origin']
  jwt_token = event['queryStringParameters']['Authorization']
  # Return early if this is the user connectivity test
  if jwt_token == 'connectivityTest'
    return generate_allow('connectivityTest', event['methodArn'], {connectivityTest: true})
  end
  method_arn = event['methodArn']

  if jwt_token
    begin
      decoded_token = JWT.decode(
        jwt_token,
        # Temporarily choose the key based on the client origin rather than the
        # method_arn until we have environment-specific Javabuilders set up.
        # get_public_key(method_arn),
        get_public_key(origin),
        true,
        verify_iat: true, # verify issued at time is valid
        algorithm: 'RS256'
      )
    rescue JWT::ExpiredSignature, JWT::InvalidIatError
      return generate_deny(nil, method_arn)
    end

    return generate_deny(nil, method_arn) unless decoded_token

    token_payload = decoded_token[0]
    user_id = token_payload['uid']
    issuer = token_payload['iss']
    principal_id = "#{issuer}/#{user_id}"

    generate_allow(principal_id, method_arn, token_payload)
  else
    generate_deny(nil, method_arn)
  end
end

# Helper function to generate an IAM policy.
def generate_policy(principal_id, effect, resource, token_payload)
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
  auth_response['context'] = token_payload if token_payload
  auth_response
end

def generate_allow(principal_id, resource, token_payload)
  generate_policy(principal_id, 'Allow', resource, token_payload)
end

def generate_deny(principal_id, resource)
  generate_policy(principal_id, 'Deny', resource, nil)
end

# Load the JWT public key from the appropriate environment variable
# based on the environment the request was sent from.
#
# method_arn is in format
# arn:aws:execute-api:region:account-id:api-id/stage-name/$connect
# We only care about stage--that tells us the environment (development, staging, etc.)
# def get_public_key(method_arn)
def get_public_key(origin)
  # Temporarily choose the key based on the client origin rather than the
  # method_arn until we have environment-specific Javabuilders set up.
  # tmp = method_arn.split(':')
  # api_gateway_arn = tmp[5].split('/')
  # stage_name = api_gateway_arn[1]

  stage_name = ""
  if origin.include? "localhost"
    stage_name = "development"
  elsif origin.include? "staging"
    stage_name = "staging"
  elsif origin.include? "levelbuilder"
    stage_name = "levelbuilder"
  elsif origin.include? "test"
    stage_name = "test"
  elsif origin.include? "adhoc"
    stage_name = "adhoc"
  else
    stage_name = "production"
  end
  # End of temporary code

  public_key = ENV["rsa_pub_#{stage_name}"]
  # Environment variables can't contain newlines
  # (if you copy over an environment variable with
  # newlines they are replaced with spaces)
  # The public keys are saved in the configuration
  # by copying over the key with literal '\n' at the end of each line.
  # Environment variable save then escapes '\n' and adds a space.
  # We need to replace '\\n<space>' with a real '\n'
  # for the key generation to work.
  public_key = public_key.gsub('\n ', "\n")
  OpenSSL::PKey::RSA.new(public_key)
end
