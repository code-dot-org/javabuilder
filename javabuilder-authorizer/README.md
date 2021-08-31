# Javabuilder Authorizer

This lambda function is an 
[API Gateway Authorizer](https://docs.aws.amazon.com/apigateway/latest/developerguide/apigateway-websocket-api-lambda-auth.html).
It expects a JWT token to be sent in an Authorization header in the standard format `Authorization: Bearer <token>`.

To create a zip file to upload to the lambda, run [zip_for_aws](zip_for_aws.sh).

You must be running ruby version 2.7.4 for the upload to work properly. The lambda expects the gems to be in 
`vendor\bundle\ruby\2.7.0\*`. You may be able to manually rename the `2.7.0` folder to workaround installing a different
ruby version.

# Prerequisites
You need to install Ruby 2.7.4 to build and/or run this function. [rbenv](https://github.com/rbenv/rbenv) is a useful tool for installing Ruby versions
and managing multiple versions. Follow the instructions [here](https://github.com/rbenv/rbenv#installing-ruby-versions) to
use rbenv to install a new Ruby version. You may need to also install [ruby-build](https://github.com/rbenv/ruby-build#readme) to get the latest Ruby
versions. If you want to just set Ruby 2.7.4 as the version for working within javabuilder run `rbenv local 2.7.4` in the root Javabuilder folder.