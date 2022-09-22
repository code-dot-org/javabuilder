# Javabuilder

This is the code that compiles and runs user code from the Java Lab app in code studio.
Javabuilder is a serverless AWS application with three main parts:
1. The WebSocket communication layer, handled by AWS API Gateway and AWS SQS
1. The code execution layer, handled by AWS Lambda
1. The data layer, handled by Code.org's Dashboard server

The Javabuilder dev doc can be found at
[Code.org Javabuilder Service Infrastructure](https://docs.google.com/document/d/196aKj947BYZXZH3nGvzHprgWPOoy2Kw1x3sKgziUaSo/edit).

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
1. Push your local changes to your feature branch.
1. Deploy a development instance of Javabuilder, following the instructions here:
   [Deploying a Development environment](https://github.com/code-dot-org/javabuilder/tree/main/cicd#deploying-an-development-environment).

To connect your dev instance with Java Lab (Code Studio client) running on your local Dashboard server:   

1. In your code-dot-org workspace, add an entry to your `locals.yml` file with your dev instance stack name:
   ```
   local_javabuilder_stack_name: 'javabuilder-dev-<your-branch-name>'
   ```
1. Launch dashboard using the instructions here:
   https://github.com/code-dot-org/code-dot-org/blob/staging/SETUP.md#overview
1. Navigate to any Java Lab level, for example:
   http://localhost-studio.code.org:3000/projects/javalab/new
1. Click the "Run" button

To connect with an adhoc:

1. Deploy the adhoc using the instructions in the
   [How to Provision an adhoc Environment](https://docs.google.com/document/d/1nWeQEmEQF1B2l93JTQPyeRpLEFzCzY5NdgJ8kgprcDk/edit)
   document.
1. Add your dev instance stack name to the `locals.yml` on your adhoc machine like above.
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
