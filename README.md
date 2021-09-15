# Javabuilder

This is the code that compiles and runs user code from the Java Lab app in code studio.
Javabuilder is a serverless AWS application with three main parts:
1. The WebSocket communication layer, handled by AWS API Gateway and AWS SQS
1. The code execution layer, handled by AWS Lambda
1. The data layer, handled by Code.org's Dashboard server

For an in-depth description of how these three parts work together, see the
[Code.org Javabuilder Service Infrastructure](https://docs.google.com/document/d/196aKj947BYZXZH3nGvzHprgWPOoy2Kw1x3sKgziUaSo/edit)
doc.

### Directory
* [api-gateway-routes](https://github.com/code-dot-org/javabuilder/tree/main/api-gateway-routes)
  contains the Lambda function that is executed by API Gateway when the connect,
  disconnect, and default routes are invoked.
* [javabuilder-authorizer](https://github.com/code-dot-org/javabuilder/tree/main/javabuilder-authorizer)
  contains the Lambda function that is executed by API Gateway to authorize the user when
  the connect route is invoked.
* [org-code-javabuilder](https://github.com/code-dot-org/javabuilder/tree/main/org-code-javabuilder)
  contains the Lambda function that builds and runs student code. It also contains the
  local developent version of Javabuilder.
* [javabuilder](https://github.com/code-dot-org/javabuilder) (the current directory)
  contains the script and Cloud Formation template for deploying Javabuilder to
  production.

## Developing Javabuilder
For developing core Javabuilder (the engine that builds & runs student code) you can skip
directly to the
[Java README](https://github.com/code-dot-org/javabuilder/blob/main/org-code-javabuilder/README.md).
For developing the API Gateway communication layer lambdas or working on the deployment
script, read further.

## Prerequisites
You need to install Ruby 2.7.4 to build and/or run the Ruby lambda functions 
(`javabuilder-authorizer` and `api-gateway-routes`). 
[rbenv](https://github.com/rbenv/rbenv) is a useful tool for installing Ruby and managing
multiple versions. Follow the instructions 
[here](https://github.com/rbenv/rbenv#installing-ruby-versions) to use rbenv to install a
new Ruby version. You may need to also install 
[ruby-build](https://github.com/rbenv/ruby-build#readme) to get the latest Ruby versions.
The `.ruby-version` file sets the local Ruby version for javabuilder to be 2.7.4.

## Deploying Javabuilder
For development purposes, you generally shouldn't need to deploy Javabuilder. See the 
org-code-javabuilder 
[README](https://github.com/code-dot-org/javabuilder/blob/main/org-code-javabuilder/README.md)
for local testing instructions. Some cases that do need a dev deployment of Javabuilder:
* Editing the API Gateway Route Lambdas.
* Editing the Javabuilder Authorizer Lambda.
* Editing the Javabuilder Cloud Formation template or deploy script.
* Editing the Lambda-specific portions of the build and run Lambda. These are all 
  prefixed with `AWS` or `Lambda` in the org-code-javabuilder directory.


To deploy Javabuilder to production or to a dev account, see the
[Deploying Javabuilder](https://docs.google.com/document/d/1mMQK6HhniLsz9lynzhUcm7Tcw_2WVLBxADe0WzqL6rM/edit#)
instruction doc.