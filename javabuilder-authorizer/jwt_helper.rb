module JwtHelper
  # Verify the token with the appropriate public key (dependant on the
  # environment the request came from), and checks the token has not
  # expired and its issue time is not in the future.
  def decode_token(token, standardized_origin)
    return false unless token
    begin
      return JWT.decode(
        token,
        # Temporarily choose the key based on the client origin rather than the
        # resource until we have environment-specific Javabuilders set up.
        get_public_key(standardized_origin),
        true,
        verify_iat: true, # verify issued at time is valid
        algorithm: 'RS256'
      )
    rescue JWT::ExpiredSignature, JWT::InvalidIatError
      return false
    end
  end

  def generate_allow(resource, token_payload)
    user_id = token_payload['uid']
    issuer = token_payload['iss']
    principal_id = "#{issuer}/#{user_id}"
    generate_policy(principal_id, 'Allow', resource, token_payload)
  end

  def generate_deny(resource)
    generate_policy(nil, 'Deny', resource, nil)
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

  def get_standardized_origin(origin)
    origin.delete_prefix("https://").delete_prefix("http://").delete_suffix(":3000")
  end

  private

  # Load the JWT public key from the appropriate environment variable
  # based on the environment the request was sent from.
  #
  # route_arn is in format
  # arn:aws:execute-api:region:account-id:api-id/stage-name/$connect
  # We only care about stage--that tells us the environment (development, staging, etc.)
  # def get_public_key(route_arn)
  def get_public_key(standardized_origin)
    # Temporarily choose the key based on the client origin rather than the
    # route_arn until we have environment-specific Javabuilders set up.
    # tmp = route_arn.split(':')
    # api_gateway_arn = tmp[5].split('/')
    # stage_name = api_gateway_arn[1]
    stage_name = ""
    if standardized_origin == "localhost-studio.code.org"
      stage_name = "development"
    elsif standardized_origin == "staging-studio.code.org"
      stage_name = "staging"
    elsif standardized_origin == "levelbuilder-studio.code.org"
      stage_name = "levelbuilder"
    elsif standardized_origin == "test-studio.code.org"
      stage_name = "test"
    elsif standardized_origin == "studio.code.org"
      stage_name = "production"
    elsif standardized_origin.start_with?("adhoc-") && standardized_origin.end_with?("-studio.cdn-code.org")
      stage_name = "adhoc"
    end
    # End of temporary code

    public_key = ENV["rsa_pub_#{stage_name}"]
    # Environment variables can't contain newlines (if you copy over an environment variable with
    # newlines they are replaced with spaces) The public keys are saved in the configuration by
    # copying over the key with literal '\n' at the end of each line. Environment variable save
    # then escapes '\n' and adds a space. We need to replace '\\n<space>' with a real '\n' for
    # the key generation to work.
    public_key = public_key.gsub('\n ', "\n")
    OpenSSL::PKey::RSA.new(public_key)
  end
end
