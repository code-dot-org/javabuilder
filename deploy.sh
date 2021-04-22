#!/bin/bash -xe

# Deploys API service CloudFormation stack.

S3_BUCKET=${S3_BUCKET?Required}

STACK=${STACK-'javabuilder'}
TEMPLATE=template.yml
OUTPUT_TEMPLATE=$(mktemp)

aws cloudformation package \
  --template-file ${TEMPLATE} \
  --s3-bucket ${S3_BUCKET} \
  --output-template-file ${OUTPUT_TEMPLATE}

aws cloudformation deploy \
  --template-file ${OUTPUT_TEMPLATE} \
  --stack-name ${STACK} \
  "$@"
