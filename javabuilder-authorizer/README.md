# Javabuilder Authorizer

This lambda function is an 
[API Gateway Authorizer](https://docs.aws.amazon.com/apigateway/latest/developerguide/apigateway-websocket-api-lambda-auth.html).
It expects a JWT token to be sent in an Authorization header in the standard format `Authorization: Bearer <token>`.

To create a zip file to upload to the lambda, run [zip_for_aws](zip_for_aws.sh).

You must be running ruby version 2.5.0 for the upload to work properly. The lambda expects the gems to be in 
`vendor\bundle\ruby\2.5.0\*`. You may be able to manually rename the `2.5.0` folder to workaround installing a different
ruby version.

