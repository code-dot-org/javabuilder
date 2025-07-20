#!/bin/bash

# Cleanup failed CloudFormation stack
# This script safely deletes a stack that's in ROLLBACK_COMPLETE state

set -e

STACK_NAME="javabuilder-dev"
AWS_PROFILE="codeorg-dev"
AWS_REGION="us-east-1"

echo "🧹 Cleaning up failed CloudFormation stack: $STACK_NAME"

# Check if stack exists and get its status
STACK_STATUS=$(aws cloudformation describe-stacks \
    --stack-name "$STACK_NAME" \
    --profile "$AWS_PROFILE" \
    --region "$AWS_REGION" \
    --query 'Stacks[0].StackStatus' \
    --output text 2>/dev/null || echo "STACK_NOT_FOUND")

if [ "$STACK_STATUS" = "STACK_NOT_FOUND" ]; then
    echo "✅ Stack $STACK_NAME not found - nothing to clean up"
    exit 0
fi

echo "📋 Current stack status: $STACK_STATUS"

# Only delete if stack is in a failed state
if [[ "$STACK_STATUS" == "ROLLBACK_COMPLETE" || "$STACK_STATUS" == "CREATE_FAILED" || "$STACK_STATUS" == "DELETE_FAILED" ]]; then
    echo "🗑️  Deleting failed stack..."
    
    aws cloudformation delete-stack \
        --stack-name "$STACK_NAME" \
        --profile "$AWS_PROFILE" \
        --region "$AWS_REGION"
    
    echo "⏳ Waiting for stack deletion to complete..."
    aws cloudformation wait stack-delete-complete \
        --stack-name "$STACK_NAME" \
        --profile "$AWS_PROFILE" \
        --region "$AWS_REGION" \
        --cli-read-timeout 600 \
        --cli-connect-timeout 60
    
    echo "✅ Stack $STACK_NAME deleted successfully"
else
    echo "⚠️  Stack is in state: $STACK_STATUS"
    echo "   Stack can only be deleted if it's in ROLLBACK_COMPLETE, CREATE_FAILED, or DELETE_FAILED state"
    echo "   Current state does not require cleanup"
fi

echo "🎉 Cleanup complete!"
