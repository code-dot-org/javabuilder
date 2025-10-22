# Javabuilder Authorizer

This lambda function is an 
[API Gateway Authorizer](https://docs.aws.amazon.com/apigateway/latest/developerguide/apigateway-websocket-api-lambda-auth.html).
It expects a JWT token to be sent in an Authorization header in the standard format 
`Authorization: Bearer <token>`.

You must be running the same ruby version locally as the configured Lambda runtime for
the upload to work properly. The lambda expects the gems to be in `vendor\bundle\ruby\<version>\*`.
If you use rbenv, the `.ruby-version` file will ensure you have the correct version.

## Testing
To run unit tests, run `rake test` from this folder. For end to end testing, refer to the main [README](../README.MD).
