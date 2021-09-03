#!/bin/bash -e

# Package Javabuilder for CodePipeline's CloudFormation-Deploy action.

TEMPLATE_BUCKET=${TEMPLATE_BUCKET?Required}
SUB_DOMAIN=${SUB_DOMAIN?Required}
STAGING_SUB_DOMAIN=${STAGING_SUB_DOMAIN?Required}

get_hosted_zone() {
  aws route53 list-hosted-zones-by-name \
  --dns-name "$1" \
  --max-items 1 \
  --query HostedZones[0].Id \
  --output text \
  | sed 's|/hostedzone/||'
}

# Default to dev-code.org domain name and the Route 53 Hosted Zone ID in the Dev AWS account for that base domain name.
BASE_DOMAIN=${BASE_DOMAIN-'dev-code.org'}
# Default to lookup the hosted zone by name.
BASE_DOMAIN_HOSTED_ZONE_ID=${BASE_DOMAIN_HOSTED_ZONE_ID-$(get_hosted_zone "${BASE_DOMAIN}")}

# Use sub domain name as the CloudFormation Stack name.
STACK=${SUB_DOMAIN}
STAGING_STACK=${STAGING_SUB_DOMAIN}

# Provisioned concurrency for production stack.
PROVISIONED_CONCURRENT_EXECUTIONS=${PROVISIONED_CONCURRENT_EXECUTIONS-'1'}

OUTPUT_TEMPLATE=${OUTPUT_TEMPLATE?Required}
TEMPLATE_CONFIG=${TEMPLATE_CONFIG?Required}
STAGING_TEMPLATE_CONFIG=${STAGING_TEMPLATE_CONFIG?Required}

# Build each Lambda (that needs to be compiled or has external package dependencies) so it can be uploaded to AWS Lambda.
./javabuilder-authorizer/build.sh
./org-code-javabuilder/build.sh

aws cloudformation package \
  --template-file template.yml \
  --s3-bucket ${TEMPLATE_BUCKET} \
  --output-template-file ${OUTPUT_TEMPLATE} \

cat <<JSON > ${TEMPLATE_CONFIG}
{
  "Parameters": {
    "BaseDomainName": "${BASE_DOMAIN}",
    "BaseDomainNameHostedZonedID": "${BASE_DOMAIN_HOSTED_ZONE_ID}",
    "SubDomainName": "${SUB_DOMAIN}",
    "ProvisionedConcurrentExecutions": "1"
  },
  "Tags": {
    "environment": "production"
  }
}
JSON

cat <<JSON > ${STAGING_TEMPLATE_CONFIG}
{
  "Parameters": {
    "BaseDomainName": "${BASE_DOMAIN}",
    "BaseDomainNameHostedZonedID": "${BASE_DOMAIN_HOSTED_ZONE_ID}",
    "SubDomainName": "${STAGING_SUB_DOMAIN}",
    "ProvisionedConcurrentExecutions": "${PROVISIONED_CONCURRENT_EXECUTIONS}"
  },
  "Tags": {
    "environment": "staging"
  }
}
JSON

