#!/bin/bash

# JavaBuilder Dev Environment Health Check
# This script verifies all components are working after deployment

set -e

PROFILE="codeorg-dev"
REGION="us-east-1"
STACK_NAME="javabuilder-dev"

echo "🏥 JavaBuilder Dev Environment Health Check"
echo "==========================================="

# 1. Check CloudFormation Stack Status
echo "📊 Checking CloudFormation Stack Status..."
STACK_STATUS=$(aws cloudformation describe-stacks --stack-name "$STACK_NAME" --profile "$PROFILE" --region "$REGION" --query 'Stacks[0].StackStatus' --output text)
echo "   Stack Status: $STACK_STATUS"

if [ "$STACK_STATUS" != "CREATE_COMPLETE" ] && [ "$STACK_STATUS" != "UPDATE_COMPLETE" ]; then
    echo "❌ Stack is not in a healthy state!"
    exit 1
fi

# 2. Check Lambda Functions
echo ""
echo "🔧 Checking Lambda Functions..."
LAMBDA_FUNCTIONS=$(aws lambda list-functions --profile "$PROFILE" --region "$REGION" --query 'Functions[?contains(FunctionName, `javabuilder-dev`)].FunctionName' --output text)

for FUNCTION in $LAMBDA_FUNCTIONS; do
    STATE=$(aws lambda get-function --function-name "$FUNCTION" --profile "$PROFILE" --region "$REGION" --query 'Configuration.State' --output text 2>/dev/null || echo "ERROR")
    if [ "$STATE" = "Active" ] || [ "$STATE" = "None" ]; then
        echo "   ✅ $FUNCTION: $STATE"
    else
        echo "   ❌ $FUNCTION: $STATE"
    fi
done

# 3. Test API Gateway Endpoints
echo ""
echo "🌐 Testing API Gateway Endpoints..."

# Test HTTP API
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" https://javabuilder-dev-http.dev-code.org)
if [ "$HTTP_STATUS" = "404" ]; then
    echo "   ✅ HTTP API: Responding (404 expected for root path)"
else
    echo "   ⚠️  HTTP API: Status $HTTP_STATUS"
fi

# Test Content CDN
CONTENT_STATUS=$(curl -s -o /dev/null -w "%{http_code}" https://javabuilder-dev-content.dev-code.org)
if [ "$CONTENT_STATUS" = "403" ]; then
    echo "   ✅ Content CDN: Responding (403 expected for empty bucket)"
else
    echo "   ⚠️  Content CDN: Status $CONTENT_STATUS"
fi

# Test WebSocket (basic connection test)
WEBSOCKET_STATUS=$(curl -s -o /dev/null -w "%{http_code}" https://javabuilder-dev.dev-code.org)
if [ "$WEBSOCKET_STATUS" = "426" ] || [ "$WEBSOCKET_STATUS" = "404" ] || [ "$WEBSOCKET_STATUS" = "400" ]; then
    echo "   ✅ WebSocket API: Responding (non-200 expected for HTTP test)"
else
    echo "   ⚠️  WebSocket API: Status $WEBSOCKET_STATUS"
fi

# 4. Check DynamoDB Tables
echo ""
echo "🗃️  Checking DynamoDB Tables..."
TABLES=$(aws dynamodb list-tables --profile "$PROFILE" --region "$REGION" --query 'TableNames[?contains(@, `javabuilder-dev`)]' --output text)

for TABLE in $TABLES; do
    STATUS=$(aws dynamodb describe-table --table-name "$TABLE" --profile "$PROFILE" --region "$REGION" --query 'Table.TableStatus' --output text 2>/dev/null || echo "ERROR")
    if [ "$STATUS" = "ACTIVE" ]; then
        echo "   ✅ $TABLE: $STATUS"
    else
        echo "   ❌ $TABLE: $STATUS"
    fi
done

# 5. Check S3 Buckets
echo ""
echo "🪣 Checking S3 Buckets..."
BUCKETS=$(aws s3api list-buckets --profile "$PROFILE" --region "$REGION" --query 'Buckets[?contains(Name, `javabuilder-dev`)].Name' --output text)

for BUCKET in $BUCKETS; do
    if aws s3api head-bucket --bucket "$BUCKET" --profile "$PROFILE" --region "$REGION" 2>/dev/null; then
        echo "   ✅ $BUCKET: Available"
    else
        echo "   ❌ $BUCKET: Error"
    fi
done

# 6. Summary and Next Steps
echo ""
echo "📋 Health Check Summary"
echo "======================="
echo "   Stack Status: $STACK_STATUS"
echo "   Lambda Functions: $(echo $LAMBDA_FUNCTIONS | wc -w) found"
echo "   DynamoDB Tables: $(echo $TABLES | wc -w) found"
echo "   S3 Buckets: $(echo $BUCKETS | wc -w) found"
echo ""
echo "🎯 Next Steps to Test Your JavaBuilder:"
echo "   1. Use the WebSocket endpoint: wss://javabuilder-dev.dev-code.org"
echo "   2. HTTP upload endpoint: https://javabuilder-dev-http.dev-code.org"
echo "   3. Content delivery: https://javabuilder-dev-content.dev-code.org"
echo ""
echo "💡 Integration Testing:"
echo "   - Use Code.org Studio development environment"
echo "   - Connect JavaLab to your dev endpoints"
echo "   - Test with simple Java programs first"
echo ""
echo "✅ Health check completed!"
