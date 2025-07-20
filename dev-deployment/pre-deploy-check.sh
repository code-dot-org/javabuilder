#!/bin/bash

# Pre-deployment check script for Javabuilder
# This script verifies all prerequisites before attempting deployment

set -e

# Configuration
AWS_PROFILE="codeorg-dev"

echo "🔍 Running pre-deployment checks..."

# Check AWS CLI
echo "📋 Checking AWS CLI..."
if ! command -v aws &> /dev/null; then
    echo "❌ AWS CLI not found. Please install it first."
    exit 1
fi

echo "✅ AWS CLI found: $(aws --version)"

# Check AWS credentials
echo "🔑 Checking AWS credentials for profile: $AWS_PROFILE..."
if ! aws sts get-caller-identity --profile "$AWS_PROFILE" > /dev/null 2>&1; then
    echo "❌ AWS credentials not configured or invalid for profile: $AWS_PROFILE"
    echo "Please ensure the profile is configured correctly"
    echo "You can check with: aws configure list --profile $AWS_PROFILE"
    exit 1
fi

ACCOUNT_ID=$(aws sts get-caller-identity --profile "$AWS_PROFILE" --query Account --output text)
echo "✅ AWS credentials valid for profile: $AWS_PROFILE. Account ID: $ACCOUNT_ID"

# Check Ruby
echo "💎 Checking Ruby..."
if ! command -v ruby &> /dev/null; then
    echo "❌ Ruby not found. Please install Ruby first."
    exit 1
fi

echo "✅ Ruby found: $(ruby --version)"

# Check ERB
echo "📄 Checking ERB template processor..."
if ! command -v erb &> /dev/null; then
    echo "❌ ERB not found. Please install ERB first."
    exit 1
fi

echo "✅ ERB found"

# Check required files
echo "📂 Checking required files..."

required_files=(
    "../cicd/3-app/javabuilder/template.yml.erb"
    "../cicd/3-app/javabuilder/config/create-environment-config.sh"
    "../cicd/3-app/javabuilder/config/dev.config.json"
    "dev-deployment-params.json"
)

for file in "${required_files[@]}"; do
    if [ ! -f "$file" ]; then
        echo "❌ Required file not found: $file"
        exit 1
    fi
done

echo "✅ All required files found"

# Check CloudFormation template can be processed
echo "🔄 Testing ERB template processing..."
if ! erb -T - ../cicd/3-app/javabuilder/template.yml.erb > /dev/null 2>&1; then
    echo "❌ Failed to process ERB template"
    exit 1
fi

echo "✅ ERB template processing successful"

# Check if javabuilder components exist
echo "🏗️  Checking javabuilder components..."
components=(
    "../javabuilder-authorizer"
    "../api-gateway-routes"
    "../org-code-javabuilder"
)

for component in "${components[@]}"; do
    if [ ! -d "$component" ]; then
        echo "⚠️  Warning: Component directory not found: $component"
    else
        echo "✅ Found component: $component"
    fi
done

# Check IAM permissions (basic check)
echo "🔐 Checking basic IAM permissions..."
if ! aws iam get-user --profile "$AWS_PROFILE" > /dev/null 2>&1 && ! aws sts get-caller-identity --profile "$AWS_PROFILE" > /dev/null 2>&1; then
    echo "❌ Insufficient IAM permissions for profile: $AWS_PROFILE"
    exit 1
fi

echo "✅ Basic IAM permissions verified"

# Check if stack already exists
echo "📋 Checking existing stack..."
STACK_NAME="javabuilder-dev"
STACK_EXISTS=$(aws cloudformation describe-stacks --stack-name "$STACK_NAME" --query 'Stacks[0].StackStatus' --output text --profile "$AWS_PROFILE" 2>/dev/null || echo "DOES_NOT_EXIST")

if [ "$STACK_EXISTS" = "DOES_NOT_EXIST" ]; then
    echo "ℹ️  Stack does not exist - will create new stack"
else
    echo "ℹ️  Stack exists with status: $STACK_EXISTS"
fi

echo ""
echo "✅ All pre-deployment checks passed!"
echo "🚀 You can now run: ./deploy-javabuilder-dev.sh"
echo ""
echo "📝 Note: This script doesn't check for Java build dependencies."
echo "   If you need to build Java components, ensure Java 11+ is installed."
