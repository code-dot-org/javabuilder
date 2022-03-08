#!/bin/bash

echo Deploying Javabuilder CICD Dependencies

# Create/Update the Javabuilder setup/dependencies stack. This is manually created and maintained, and requires elevated permissions. 

# 'Developer' role requires a specific service role for all CloudFormation operations.
if [[ $(aws sts get-caller-identity --query Arn --output text) =~ "475661607190:assumed-role/Developer/" ]]; then
  # Append the role-arn option to the positional parameters $@ passed to cloudformation deploy.
  set -- "$@" --role-arn "arn:aws:iam::$(aws sts get-caller-identity --query Account --output text):role/admin/CloudFormationService"
fi

TEMPLATE_FILE=cicd/1-setup/setup.template.yml

echo Validating cloudformation template...
aws cloudformation validate-template \
  --template-body file://${TEMPLATE_FILE} \
  | cat

echo Updating cloudformation stack...
aws cloudformation deploy \
  --stack-name darin-javabuilder-ci-deps \
  --template-file ${TEMPLATE_FILE} \
  --capabilities CAPABILITY_IAM \
  "$@"

echo Complete!