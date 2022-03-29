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
Use `rbenv` to install Ruby 2.7.2 to build and/or run the Ruby lambda functions
(`javabuilder-authorizer` and `api-gateway-routes`).
[rbenv](https://github.com/rbenv/rbenv) is required for installing Ruby and managing
multiple versions of Ruby on a single development environment. Follow the instructions
[here](https://github.com/rbenv/rbenv#installing-ruby-versions) to use rbenv to install a
new Ruby version. You may need to also install
[ruby-build](https://github.com/rbenv/ruby-build#readme) to get the latest Ruby versions.
The `.ruby-version` file sets the local Ruby version for javabuilder to be 2.7.2

## Deploying Production Javabuilder
To deploy Javabuilder to production, see the
[Deploying Javabuilder](https://docs.google.com/document/d/1mMQK6HhniLsz9lynzhUcm7Tcw_2WVLBxADe0WzqL6rM/edit#)
instruction doc.

## Developing Javabuilder

There are two main ways to develop and run Javabuilder:
- Using the **[WebSocketServer](https://github.com/code-dot-org/javabuilder/blob/main/org-code-javabuilder/lib/src/main/java/dev/javabuilder/WebSocketServer.java)**: 
  - this is a local replacement of API Gateway that runs the "core" Javabuilder (the engine that 
    builds & runs student code) directly. This suits most local development needs that don't touch 
    any AWS-specific classes, or code outside the [org-code-javabuilder](https://github.com/code-dot-org/javabuilder/tree/main/org-code-javabuilder) 
    directory. To set up the WebSocketServer, skip directly to the
    [org-code-javabuilder README](https://github.com/code-dot-org/javabuilder/blob/main/org-code-javabuilder/README.md).
- Deploying a **development instance** of Javabuilder: 
  - this allows you to develop against a custom deployment of the full Javabuilder AWS stack in a 
    prod-like environment. This is useful for developing AWS-specific code (anything prefixed with 
    `AWS` or `Lambda`), and anything outside the [org-code-javabuilder](https://github.com/code-dot-org/javabuilder/tree/main/org-code-javabuilder) 
    directory, such as the Lambda authorizers and deployment scripts. To deploy a development instance of 
    Javabuilder, read further.

### Deploying a Dev Instance

1. Make and commit your desired changes.
1. Deploy a development instance of Javabuilder, following the instructions here:
   [Deploying Javabuilder](https://docs.google.com/document/d/1mMQK6HhniLsz9lynzhUcm7Tcw_2WVLBxADe0WzqL6rM/edit#bookmark=id.6objek4aiiu5).
   The deployment should take around 10 minutes.

To connect your dev instance with Java Lab (Code Studio client) running on your local Dashboard server:   

1. In the code-dot-org repo, edit the `javabuilder_url` value in
   [cdo.rb](https://github.com/code-dot-org/code-dot-org/blob/665a45210d556b4c3d82d6ad2434617c8e2e5ea1/lib/cdo.rb#L127)
   to point to your local dev deployment (this will typically be wss://javabuilder-<your name\>.dev-code.org).
1. Edit the `javabuilder_upload_url` value in
   [cdo.rb](https://github.com/code-dot-org/code-dot-org/blob/665a45210d556b4c3d82d6ad2434617c8e2e5ea1/lib/cdo.rb#L137)
   to point to your local dev HTTP upload API (this will typically be https://javabuilder-<your name\>-http.dev-code.org/seedsources/sources.json).
1. Launch dashboard using the instructions here:
   https://github.com/code-dot-org/code-dot-org/blob/staging/SETUP.md#overview
1. Navigate to any Java Lab level, for example:
   http://localhost-studio.code.org:3000/projects/javalab/new
1. Click the "Run" button

To connect with an adhoc:

1. Make the same changes to cdo.rb listed above.
1. Additionally, edit the `upgrade_insecure_requests` list to include
   the deployed Javabuilder hostname.
   [example](https://github.com/code-dot-org/code-dot-org/commit/945fa3ad38be6d85cb7c7aaeda5b3bf2e0fde60c#diff-19cc5be92c36ff06b63767f0ff922d2b9b7b9b8bebe4eaf38e0f331a14b0b528R53)
1. Commit your changes and deploy the adhoc using the instructions in the
   [How to Provision an adhoc Environment](https://docs.google.com/document/d/1nWeQEmEQF1B2l93JTQPyeRpLEFzCzY5NdgJ8kgprcDk/edit)
   document.
1. Navigate to any Java Lab level, for example:
       http://<your-adhoc-name\>.cdn-code.org/projects/javalab/new
1. Click the "Run" button

_NOTE: Currently, Javabuilder still relies on Dashboard to fetch assets when needed (for example, in Theater
projects). If you are running Dashboard locally and have connected it to a dev instance of Javabuilder,
your dev instance will not able to access assets as it cannot make requests to your local machine. 
In this case, you should expect to see these static [image](https://github.com/code-dot-org/javabuilder/blob/main/org-code-javabuilder/lib/src/main/resources/sampleImageBeach.jpg) 
and [audio](https://github.com/code-dot-org/javabuilder/blob/main/org-code-javabuilder/lib/src/main/resources/beatbox.wav) 
files used in replacement of project assets._

### Advanced Usage

You can also develop with a deployed dev instance of Javabuilder without running Dashboard by using tools
such as Postman and wscat. However, this is a considerably more complicated setup and should only be used 
in situations where running Dashboard locally or with an adhoc is not possible. For more details, 
contact the [CSA team](https://github.com/orgs/code-dot-org/teams/csa).
