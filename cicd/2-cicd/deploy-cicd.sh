#!/bin/bash

echo Deploying Javabuilder CICD Pipeline

# Create/Update the Javabuilder build/deploy pipeline stack. This is manually created and maintained, but should not require elevated permissions. 

# 'Developer' role requires a specific service role for all CloudFormation operations.
if [[ $(aws sts get-caller-identity --query Arn --output text) =~ "assumed-role/Developer/" ]]; then
  # Append the role-arn option to the positional parameters $@ passed to cloudformation deploy.
  set -- "$@" --role-arn "arn:aws:iam::$(aws sts get-caller-identity --query Account --output text):role/admin/CloudFormationService"
fi

STACK_NAME=javabuilder-cicd
TEMPLATE_FILE=cicd/2-cicd/cicd.template.yml

echo Validating cloudformation template...
aws cloudformation validate-template \
  --template-body file://${TEMPLATE_FILE} \
  | cat

ACCOUNT=$(aws sts get-caller-identity --query "Account" --output text)

read -r -p "Would you like to deploy this template to AWS account $ACCOUNT? [y/N] " response
if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]
then
  echo Updating cloudformation stack...
  aws cloudformation deploy \
    --stack-name $STACK_NAME \
    --template-file $TEMPLATE_FILE \
    --parameter-overrides GitHubBranch=main GitHubBadgeEnabled=false \
    --capabilities CAPABILITY_IAM \
    "$@"

  echo Complete!
else
  echo Exiting...
fi
