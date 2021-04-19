#!/bin/bash -e

# Package template for CodePipeline's CloudFormation-Deploy action.

# Required environment variables:
STACK=${STACK?Required}
STAGING_STACK=${STAGING_STACK?Required}
OUTPUT_TEMPLATE=${OUTPUT_TEMPLATE?Required}
TEMPLATE_CONFIG=${TEMPLATE_CONFIG?Required}
STAGING_TEMPLATE_CONFIG=${STAGING_TEMPLATE_CONFIG?Required}
TEMPLATE_BUCKET=${TEMPLATE_BUCKET?Required}

aws cloudformation package \
  --template-file template.yml \
  --s3-bucket ${TEMPLATE_BUCKET} \
  --output-template-file ${OUTPUT_TEMPLATE} \

# Query existing parameter values for reuse in CodePipeline configuration.
JQ_FILTER='{Parameters: .Stacks[].Parameters | map({(.ParameterKey): .ParameterValue}) | add}'

aws cloudformation describe-stacks --stack-name ${STACK} | \
  jq "${JQ_FILTER}" \
    > ${TEMPLATE_CONFIG}

aws cloudformation describe-stacks --stack-name ${STAGING_STACK} | \
  jq "${JQ_FILTER}" \
    > ${STAGING_TEMPLATE_CONFIG}
