#!/bin/bash

echo Deploying Javabuilder CICD Dependencies

# Create/Update the Javabuilder setup/dependencies stack. This is manually created and maintained, and requires elevated permissions. 

TEMPLATE_FILE=cicd/1-setup/setup.template.yml

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
    --stack-name javabuilder-cicd-deps \
    --template-file ${TEMPLATE_FILE} \
    --capabilities CAPABILITY_IAM \
    "$@"

  echo Complete!
else
  echo Exiting...
fi


