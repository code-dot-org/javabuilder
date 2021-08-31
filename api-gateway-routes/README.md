# Javabuilder API Gateway Routes

This lambda function defines the actions that happen when the Javabuilder API Gateway triggers the connect, disconnect, and default actions.

To create a zip file to upload to the lambda, run [zip_for_aws](zip_for_aws.sh).

# Prerequisites
You need to install Ruby 2.7.4 to build and/or run this function. [rbenv](https://github.com/rbenv/rbenv) is a useful tool for installing Ruby versions
and managing multiple versions. Follow the instructions [here](https://github.com/rbenv/rbenv#installing-ruby-versions) to
use rbenv to install a new Ruby version. You may need to also install [ruby-build](https://github.com/rbenv/ruby-build#readme) to get the latest Ruby
versions. If you want to just set Ruby 2.7.4 as the version for working within javabuilder run `rbenv local 2.7.4` in the root Javabuilder folder.