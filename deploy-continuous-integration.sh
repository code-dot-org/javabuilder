#!/bin/bash -xe

# Deploys the Continuous Integration service CloudFormation stack.

TEMPLATE_BUCKET=${TEMPLATE_BUCKET?Required}
STACK=${STACK-'javabuilder-continuous-integration'}

TEMPLATE=continuous-integration.yml
OUTPUT_TEMPLATE=$(mktemp)

aws cloudformation package \
  --template-file ${TEMPLATE} \
  --s3-bucket ${TEMPLATE_BUCKET} \
  --output-template-file ${OUTPUT_TEMPLATE}

aws cloudformation deploy \
  --template-file ${OUTPUT_TEMPLATE} \
  --stack-name ${STACK} \
  "$@"
  