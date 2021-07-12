#!/bin/bash -xe

# Deploys API service CloudFormation stack.

TEMPLATE_BUCKET=${TEMPLATE_BUCKET?Required}
SUB_DOMAIN=${SUB_DOMAIN?Required}

# Default to dev-code.org domain name and the Route 53 Hosted Zone ID in the Dev AWS account for that base domain name.
BASE_DOMAIN=${BASE_DOMAIN-'dev-code.org'}
BASE_DOMAIN_HOSTED_ZONE_ID=${BASE_DOMAIN_HOSTED_ZONE_ID-'Z07248463JGJ44FME5BZ5'}

# Use sub domain name as the CloudFormation Stack name.
STACK=${SUB_DOMAIN}

TEMPLATE=template.yml
OUTPUT_TEMPLATE=$(mktemp)

# Build each Lambda (that needs to be compiled or has external package dependencies) so it can be uploaded to AWS Lambda.
./javabuilder-authorizer/build.sh
./org-code-javabuilder/build.sh

aws cloudformation package \
  --template-file ${TEMPLATE} \
  --s3-bucket ${TEMPLATE_BUCKET} \
  --output-template-file ${OUTPUT_TEMPLATE}

# TODO: Remove CAPABILITY_IAM temporarily added during testing.
# TODO: Remove parameter-overrides and document that they can be passed as shell parameters to this script.
aws cloudformation deploy \
  --template-file ${OUTPUT_TEMPLATE} \
  --parameter-overrides SubDomainName=$SUB_DOMAIN BaseDomainName=$BASE_DOMAIN BaseDomainNameHostedZonedID=$BASE_DOMAIN_HOSTED_ZONE_ID \
  --stack-name ${STACK} \
  --capabilities CAPABILITY_IAM \
  "$@"
