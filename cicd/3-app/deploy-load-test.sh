#!/bin/bash

set -e

echo Deploying Javabuilder Load Test Task

# Create/Update the Javabuilder load test stack. This script will be replaced with a cicd pipeline. 

# 'Developer' role requires a specific service role for all CloudFormation operations.
if [[ $(aws sts get-caller-identity --query Arn --output text) =~ "475661607190:assumed-role/Developer/" ]]; then
  # Append the role-arn option to the positional parameters $@ passed to cloudformation deploy.
  set -- "$@" --role-arn "arn:aws:iam::$(aws sts get-caller-identity --query Account --output text):role/admin/CloudFormationService"
fi

STACK_NAME=javabuilder-load-test
TEMPLATE_FILE=load-test.template.yml
LOAD_TEST_IMAGE=475661607190.dkr.ecr.us-east-1.amazonaws.com/javabuilder-main:molly-test

echo Validating cloudformation template...
aws cloudformation validate-template \
  --template-body file://${TEMPLATE_FILE}

echo Updating cloudformation stack...
aws cloudformation deploy \
  --stack-name $STACK_NAME \
  --template-file $TEMPLATE_FILE \
  --parameter-overrides LoadTestImage=$LOAD_TEST_IMAGE \
  --capabilities CAPABILITY_IAM \
  "$@"

echo Complete!
