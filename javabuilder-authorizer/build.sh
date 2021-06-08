#!/bin/bash -xe

# cd to script directory (shh, it's magic: https://stackoverflow.com/questions/6393551/what-is-the-meaning-of-0-in-a-bash-script)
cd "${0%/*}"

# Install Ruby gem dependencies for Lambda Authorizer, so they can be packaged and deployed by the AWS cloudformation
# command line tools.
bundle install
