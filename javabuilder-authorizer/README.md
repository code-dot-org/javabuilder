# Javabuilder Authorizer

This lambda function is an 
[API Gateway Authorizer](https://docs.aws.amazon.com/apigateway/latest/developerguide/apigateway-websocket-api-lambda-auth.html).
It expects a JWT token to be sent in an Authorization header in the standard format `Authorization: Bearer <token>`.

You must be running ruby version 2.7.4 for the upload to work properly. The lambda expects the gems to be in 
`vendor\bundle\ruby\2.7.0\*`. You may be able to manually rename the `2.7.0` folder to workaround installing a different
ruby version.
