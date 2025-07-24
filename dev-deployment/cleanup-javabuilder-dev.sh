#!/bin/bash

# Cleanup JavaBuilder Dev Environment
set -e

PROFILE="codeorg-dev"
APP_STACK="javabuilder-dev"

echo "🗑️  Starting JavaBuilder Dev Environment Cleanup..."

echo "📋 Checking if application stack exists..."
if aws cloudformation describe-stacks --stack-name "$APP_STACK" --profile "$PROFILE" >/dev/null 2>&1; then
    echo "🔄 Deleting application stack: $APP_STACK"
    aws cloudformation delete-stack --stack-name "$APP_STACK" --profile "$PROFILE"
    
    echo "⏳ Waiting for application stack deletion to complete..."
    aws cloudformation wait stack-delete-complete --stack-name "$APP_STACK" --profile "$PROFILE"
    echo "✅ Application stack deleted successfully!"
else
    echo "ℹ️  Application stack $APP_STACK not found"
fi

echo "🧹 Checking for leftover S3 buckets..."
echo "S3 buckets that may need manual cleanup:"
aws s3 ls --profile "$PROFILE" | grep javabuilder || echo "No JavaBuilder S3 buckets found"

echo "✅ Application stack cleanup complete!"
echo ""
echo "💡 Note: The IAM base infrastructure stack (javabuilder-iam) was not removed."
echo "   This stack contains shared IAM roles and can be used by multiple deployments."
echo "   To remove it manually (only if no other deployments need it):"
echo "   aws cloudformation delete-stack --stack-name javabuilder-iam --profile $PROFILE"
