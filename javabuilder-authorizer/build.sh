#!/bin/bash -xe

# cd to script directory (shh, it's magic: https://stackoverflow.com/questions/6393551/what-is-the-meaning-of-0-in-a-bash-script)
cd "${0%/*}"

# Build/install gem dependencies so they can be packaged and uploaded to AWS Lambda by CloudFormation.
bundle install
