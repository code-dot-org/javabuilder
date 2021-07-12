#!/bin/bash -e

# Package template for CodePipeline's CloudFormation-Deploy action.

# Required environment variables:
SUB_DOMAIN=${SUB_DOMAIN?Required}
STAGING_SUB_DOMAIN=${STAGING_SUB_DOMAIN?Required}

# Default to dev-code.org domain name and the Route 53 Hosted Zone ID in the Dev AWS account for that base domain name.
BASE_DOMAIN=${BASE_DOMAIN-'dev-code.org'}
BASE_DOMAIN_HOSTED_ZONE_ID=${BASE_DOMAIN_HOSTED_ZONE_ID-'Z07248463JGJ44FME5BZ5'}

# CloudFormation stack names can't contain periods ("."), so replace them with hyphens ("-").
FULLY_QUALIFIED_DOMAIN_NAME="${SUB_DOMAIN}.${BASE_DOMAIN}"
STAGING_FULLY_QUALIFIED_DOMAIN_NAME="${STAGING_SUB_DOMAIN}.${BASE_DOMAIN}"

STACK=${FULLY_QUALIFIED_DOMAIN_NAME//./-}
STAGING_STACK=${STAGING_FULLY_QUALIFIED_DOMAIN_NAME//./-}

OUTPUT_TEMPLATE=${OUTPUT_TEMPLATE?Required}
TEMPLATE_CONFIG=${TEMPLATE_CONFIG?Required}
STAGING_TEMPLATE_CONFIG=${STAGING_TEMPLATE_CONFIG?Required}
TEMPLATE_BUCKET=${TEMPLATE_BUCKET?Required}

# Build each Lambda (that needs to be compiled or has external package dependencies) so it can be uploaded to AWS Lambda.
./javabuilder-authorizer/build.sh
./org-code-javabuilder/build.sh

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
