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

### Prerequisites
You need to install Ruby 2.7.4 to build and/or run the Ruby lambda functions
(`javabuilder-authorizer` and `api-gateway-routes`).
[rbenv](https://github.com/rbenv/rbenv) is a useful tool for installing Ruby and managing
multiple versions. Follow the instructions
[here](https://github.com/rbenv/rbenv#installing-ruby-versions) to use rbenv to install a
new Ruby version. You may need to also install
[ruby-build](https://github.com/rbenv/ruby-build#readme) to get the latest Ruby versions.
The `.ruby-version` file sets the local Ruby version for javabuilder to be 2.7.4.

## Deploying Production Javabuilder
To deploy Javabuilder to production, see the
[Deploying Javabuilder](https://docs.google.com/document/d/1mMQK6HhniLsz9lynzhUcm7Tcw_2WVLBxADe0WzqL6rM/edit#)
instruction doc.

## Developing Javabuilder
For developing "core" Javabuilder (the engine that builds & runs student code) you can
skip directly to the
[org-code-javabuilder README](https://github.com/code-dot-org/javabuilder/blob/main/org-code-javabuilder/README.md).
For developing the API Gateway communication layer Lambdas or working on the deployment
script, read further.

### Dev Deploy of Javabuilder
For development purposes, you generally shouldn't need a dev deploy of Javabuilder. Some
cases that do need a dev deploy of Javabuilder:
* Editing the API Gateway Route Lambdas.
* Editing the Javabuilder Authorizer Lambda.
* Editing the Javabuilder Cloud Formation template or deploy script.
* Editing the AWS Lambda or AWS SQS-specific portions of the build and run Lambda. These
  are all prefixed with `AWS` or `Lambda` in the org-code-javabuilder directory.
  
To deploy Javabuilder to a dev account, see the
[Deploying Javabuilder](https://docs.google.com/document/d/1mMQK6HhniLsz9lynzhUcm7Tcw_2WVLBxADe0WzqL6rM/edit#)
instruction doc.

Once you have deployed Javabuilder, you can run a basic connection to it with
```
wscat -c wss://<host-name>/?Authorization=connectivityTest
```
**Developing with deployed Javabuilder**  
If you need to test against a level's Java code, use the following steps:
1. Log in to https://staging-studio.code.org
1. Navigate to the level or project you'd like to test against
1. Open the network tab on your browser developer tools
1. Click "Run" in Javalab
1. Look for the network request to
   https://staging-studio.code.org/javabuilder/access_token?projectUrl=...
   in the response, there will be a token. Copy the entire token, without quotes around
   it.
1. Open up Postman. Choose new-> WebSocket Request
1. Put the url of your javabuilder instance in the server url section (such as
   wss://javabuilder-myname.dev-code.org)
1. Under Params add the key `Authorization` with the value of the token you copied earlier
1. Under Headers add the key `Origin` with the value https://staging-studio.code.org
1. Click connect

You should now start seeing messages from Javabuilder! Your token will last for 15
minutes. If you make changes to the code, you do not need to re-generate your token.

**Developing with deployed Javabuilder and an adhoc environment**
1. Deploy Javabuilder with the instructions above
1. In the code-dot-org repo, edit the `javabuilder_url` value in
   [cdo.rb](https://github.com/code-dot-org/code-dot-org/blob/3219e5866689117e086d9891effe0fb39b9ae3f0/lib/cdo.rb#L131)
   to point to your local dev deployment.
1. In the code-dot-org repo, also edit the `upgrade_insecure_requests` list to include
   the deployed Javabuilder hostname. 
   [example](https://github.com/code-dot-org/code-dot-org/commit/945fa3ad38be6d85cb7c7aaeda5b3bf2e0fde60c#diff-19cc5be92c36ff06b63767f0ff922d2b9b7b9b8bebe4eaf38e0f331a14b0b528R53)
1. Deploy the adhoc using the instructions in the
   [How to Provision an adhoc Environment](https://docs.google.com/document/d/1nWeQEmEQF1B2l93JTQPyeRpLEFzCzY5NdgJ8kgprcDk/edit)
   document.
