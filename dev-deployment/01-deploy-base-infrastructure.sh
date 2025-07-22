#!/bin/bash

# Step 1: Deploy Base Infrastructure (IAM Roles)
# This script deploys the required IAM roles and base infrastructure

set -e

# Configuration
AWS_PROFILE="codeorg-dev"
REGION="us-east-1"
BASE_STACK_NAME="javabuilder-base-infrastructure"
IAM_TEMPLATE_FILE="../iam.yml"
ARTIFACT_BUCKET_NAME="javabuilder-dev-artifacts"

echo "🚀 Starting Javabuilder Base Infrastructure Deployment..."

# Step 1: Check AWS CLI and credentials
echo "📋 Checking AWS CLI and credentials for profile: $AWS_PROFILE..."
if ! aws sts get-caller-identity --profile "$AWS_PROFILE" > /dev/null 2>&1; then
    echo "❌ AWS CLI not configured or no valid credentials found for profile: $AWS_PROFILE"
    echo "Please ensure the profile is configured correctly"
    exit 1
fi

ACCOUNT_ID=$(aws sts get-caller-identity --profile "$AWS_PROFILE" --query Account --output text)
echo "✅ AWS credentials verified for profile: $AWS_PROFILE. Account ID: $ACCOUNT_ID"

# Step 2: Create S3 buckets for artifacts and templates
echo "📦 Creating S3 buckets for artifacts and templates..."

# Create artifact bucket
if aws s3api head-bucket --bucket "$ARTIFACT_BUCKET_NAME" --profile "$AWS_PROFILE" 2>/dev/null; then
    echo "✅ Artifact bucket $ARTIFACT_BUCKET_NAME already exists"
else
    aws s3api create-bucket --bucket "$ARTIFACT_BUCKET_NAME" --region "$REGION" --profile "$AWS_PROFILE"
    echo "✅ Created artifact bucket: $ARTIFACT_BUCKET_NAME"
fi

# Create template bucket (using same bucket for simplicity)
TEMPLATE_BUCKET_NAME="$ARTIFACT_BUCKET_NAME"
echo "✅ Using template bucket: $TEMPLATE_BUCKET_NAME"

# Step 3: Check if base infrastructure stack exists
echo "🔍 Checking if base infrastructure stack exists..."
BASE_STACK_EXISTS=$(aws cloudformation describe-stacks --stack-name "$BASE_STACK_NAME" --query 'Stacks[0].StackStatus' --output text --profile "$AWS_PROFILE" 2>/dev/null || echo "DOES_NOT_EXIST")

if [ "$BASE_STACK_EXISTS" = "DOES_NOT_EXIST" ]; then
    echo "🆕 Creating new base infrastructure stack: $BASE_STACK_NAME"
    OPERATION="create-stack"
    WAIT_CONDITION="stack-create-complete"
else
    echo "🔄 Updating existing base infrastructure stack: $BASE_STACK_NAME"
    OPERATION="update-stack"
    WAIT_CONDITION="stack-update-complete"
fi

# Step 4: Deploy base infrastructure stack
echo "🚀 Deploying base infrastructure stack..."
aws cloudformation "$OPERATION" \
    --stack-name "$BASE_STACK_NAME" \
    --template-body file://"$IAM_TEMPLATE_FILE" \
    --parameters \
        ParameterKey=ArtifactBucket,ParameterValue="$ARTIFACT_BUCKET_NAME" \
        ParameterKey=TemplateBucket,ParameterValue="$TEMPLATE_BUCKET_NAME" \
        ParameterKey=JavabuilderApiId,ParameterValue="*" \
    --capabilities CAPABILITY_IAM CAPABILITY_NAMED_IAM \
    --region "$REGION" \
    --tags Key=Environment,Value=development Key=Project,Value=javabuilder Key=Component,Value=base-infrastructure \
    --profile "$AWS_PROFILE"

echo "⏳ Waiting for base infrastructure deployment to complete..."
aws cloudformation wait "$WAIT_CONDITION" --stack-name "$BASE_STACK_NAME" --profile "$AWS_PROFILE"

# Step 5: Verify stack outputs
echo "📋 Base infrastructure deployment completed! Getting outputs..."
aws cloudformation describe-stacks \
    --stack-name "$BASE_STACK_NAME" \
    --query 'Stacks[0].Outputs' \
    --output table \
    --profile "$AWS_PROFILE"

echo "✅ Base infrastructure deployment completed successfully!"
echo "📝 Outputs:"
echo "   - Artifact Bucket: $ARTIFACT_BUCKET_NAME"
echo "   - Template Bucket: $TEMPLATE_BUCKET_NAME"
echo "   - Base Stack Name: $BASE_STACK_NAME"
echo ""
echo "🔄 Next steps:"
echo "   1. Run: ./02-build-java-components.sh"
echo "   2. Then: ./03-deploy-application.sh"
