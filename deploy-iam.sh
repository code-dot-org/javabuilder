#!/bin/bash -xe

# Deploys AWS IAM-permissions CloudFormation stack.
# Requires admin access to create/modify IAM roles.

STACK=${STACK-'javabuilder-iam'}

TEMPLATE=iam.yml
aws cloudformation deploy \
  --template-file ${TEMPLATE} \
  --capabilities CAPABILITY_IAM CAPABILITY_NAMED_IAM \
  --stack-name ${STACK} \
  --tags EnvType=infrastructure
  "$@"
