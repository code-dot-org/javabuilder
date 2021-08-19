#!/bin/bash -xe

# Create/Update a Javabuilder CloudFormation stack.

TEMPLATE_BUCKET=${TEMPLATE_BUCKET?Required}
SUB_DOMAIN=${SUB_DOMAIN?Required}

get_hosted_zone() {
  aws route53 list-hosted-zones-by-name \
  --dns-name "$1" \
  --max-items 1 \
  --query HostedZones[0].Id \
  --output text \
  | sed 's|/hostedzone/||'
}

# Default to dev-code.org domain name.
BASE_DOMAIN=${BASE_DOMAIN-'dev-code.org'}
# Default to lookup the hosted zone by name.
BASE_DOMAIN_HOSTED_ZONE_ID=${BASE_DOMAIN_HOSTED_ZONE_ID-$(get_hosted_zone "${BASE_DOMAIN}")}

# Use sub domain name as the CloudFormation Stack name.
STACK=${SUB_DOMAIN}

PROVISIONED_CONCURRENT_EXECUTIONS=${PROVISIONED_CONCURRENT_EXECUTIONS-'1'}

TEMPLATE=template.yml
OUTPUT_TEMPLATE=$(mktemp)

# Build each Lambda (that needs to be compiled or has external package dependencies) so it can be uploaded to AWS Lambda.
./javabuilder-authorizer/build.sh
./org-code-javabuilder/build.sh

aws cloudformation package \
  --template-file ${TEMPLATE} \
  --s3-bucket ${TEMPLATE_BUCKET} \
  --output-template-file ${OUTPUT_TEMPLATE}

aws cloudformation deploy \
  --template-file ${OUTPUT_TEMPLATE} \
  --parameter-overrides SubDomainName=$SUB_DOMAIN BaseDomainName=$BASE_DOMAIN BaseDomainNameHostedZonedID=$BASE_DOMAIN_HOSTED_ZONE_ID ProvisionedConcurrentExecutions=$PROVISIONED_CONCURRENT_EXECUTIONS \
  --stack-name ${STACK} \
  "$@"
