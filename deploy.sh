#!/bin/bash -xe

# Deploys API service CloudFormation stack.

S3_BUCKET=${S3_BUCKET?Required}
SUB_DOMAIN=${SUB_DOMAIN?Required}

# Default to dev-code.org domain name and the Route 53 Hosted Zone ID in the Dev AWS account for that base domain name.
BASE_DOMAIN=${BASE_DOMAIN-'dev-code.org'}
BASE_DOMAIN_HOSTED_ZONE_ID=${BASE_DOMAIN_HOSTED_ZONE_ID-'Z07248463JGJ44FME5BZ5'}

# CloudFormation stack names can't contain periods ("."), so replace them with hyphens ("-").
FULLY_QUALIFIED_DOMAIN_NAME="${SUB_DOMAIN}.${BASE_DOMAIN}"
STACK=${FULLY_QUALIFIED_DOMAIN_NAME//./-}

TEMPLATE=template.yml
OUTPUT_TEMPLATE=$(mktemp)

# Build each Lambda (that needs to be compiled or has external package dependencies) so it can be uploaded to AWS Lambda.
./javabuilder-authorizer/build.sh
./org-code-javabuilder/build.sh

aws cloudformation package \
  --template-file ${TEMPLATE} \
  --s3-bucket ${S3_BUCKET} \
  --output-template-file ${OUTPUT_TEMPLATE}

# TODO: Remove CAPABILITY_IAM temporarily added during testing.
aws cloudformation deploy \
  --template-file ${OUTPUT_TEMPLATE} \
  --parameter-overrides SubDomainName=$SUB_DOMAIN BaseDomainName=$BASE_DOMAIN BaseDomainNameHostedZonedID=$BASE_DOMAIN_HOSTED_ZONE_ID \
  --stack-name ${STACK} \
  --capabilities CAPABILITY_IAM \
  "$@"
