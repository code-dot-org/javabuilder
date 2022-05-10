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
RESERVED_CONCURRENT_EXECUTIONS=${RESERVED_CONCURRENT_EXECUTIONS-'3'}

# If alerts should be silenced on this instance. Dev instances will always be silenced.
SILENCE_ALERTS=${SILENCE_ALERTS-'false'}

# Default per-user limits to prevent javabuilder abuse.
LIMIT_PER_HOUR=${LIMIT_PER_HOUR-'50'}
LIMIT_PER_DAY=${LIMIT_PER_DAY-'150'}
# Default per-classroom hourly limit
TEACHER_LIMIT_PER_HOUR=${TEACHER_LIMIT_PER_HOUR-'1000'}

erb -T - template.yml.erb > template.yml
TEMPLATE=template.yml
OUTPUT_TEMPLATE=$(mktemp)

# Build each Lambda (that needs to be compiled or has external package dependencies) so it can be uploaded to AWS Lambda.
./javabuilder-authorizer/build.sh
./org-code-javabuilder/build.sh

aws cloudformation package \
  --template-file ${TEMPLATE} \
  --s3-bucket ${TEMPLATE_BUCKET} \
  --output-template-file ${OUTPUT_TEMPLATE}

# 'Developer' role requires a specific service role for all CloudFormation operations.
if [[ $(aws sts get-caller-identity --query Arn --output text) =~ "assumed-role/Developer/" ]]; then
  # Append the role-arn option to the positional parameters $@ passed to cloudformation deploy.
  set -- "$@" --role-arn "arn:aws:iam::$(aws sts get-caller-identity --query Account --output text):role/admin/CloudFormationService"
fi

aws cloudformation deploy \
  --s3-bucket ${TEMPLATE_BUCKET} \
  --template-file ${OUTPUT_TEMPLATE} \
  --parameter-overrides SubDomainName=$SUB_DOMAIN BaseDomainName=$BASE_DOMAIN BaseDomainNameHostedZonedID=$BASE_DOMAIN_HOSTED_ZONE_ID \
    ProvisionedConcurrentExecutions=$PROVISIONED_CONCURRENT_EXECUTIONS ReservedConcurrentExecutions=$RESERVED_CONCURRENT_EXECUTIONS \
    LimitPerHour=$LIMIT_PER_HOUR LimitPerDay=$LIMIT_PER_DAY TeacherLimitPerHour=$TEACHER_LIMIT_PER_HOUR SilenceAlerts=$SILENCE_ALERTS \
  --stack-name ${STACK} \
  "$@"
