#!/bin/bash

# Deploy JavaBuilder with SSL certificates for dev environment  
# Based on production buildspec.yml using existing wildcard certificate

set -e

PROFILE="codeorg-dev"
REGION="us-east-1"
STACK_NAME="javabuilder-dev"
TEMPLATE_PATH="../cicd/3-app/javabuilder"
APP_TEMPLATE="app-template.yml"
PACKAGED_TEMPLATE="packaged-app-template.yml"

# Set artifact bucket - use environment variable if available, otherwise use default
if [ -z "$ARTIFACT_STORE" ]; then
    ARTIFACT_STORE="javabuilder-dev-artifacts"
fi
# Check if artifact bucket exists, create if needed
echo "🔍 Checking if artifact bucket exists: $ARTIFACT_STORE"
if ! aws s3api head-bucket --bucket "$ARTIFACT_STORE" --profile "$PROFILE" --region "$REGION" 2>/dev/null; then
    echo "📦 Creating artifact bucket: $ARTIFACT_STORE"
    aws s3 mb "s3://$ARTIFACT_STORE" --profile "$PROFILE" --region "$REGION"
else
    echo "✅ Artifact bucket already exists: $ARTIFACT_STORE"
fi

# Ensure Java is in PATH  
export PATH="/opt/homebrew/opt/openjdk@11/bin:$PATH"

echo "🚀 Starting Javabuilder Dev Deployment (following production buildspec pattern)..."

# Build javabuilder-authorizer (following production buildspec)
echo "🔐 Building javabuilder-authorizer..."
cd ../javabuilder-authorizer
./build.sh

# Build org-code-javabuilder (following production buildspec)
echo "🔨 Building org-code-javabuilder..."
cd ../org-code-javabuilder  
./gradlew test
./build.sh

# Build api-gateway-routes (following production buildspec)
echo "🌐 Building api-gateway-routes..."
cd ../api-gateway-routes
rake test

# Return to deployment directory and copy artifacts for packaging
cd ../dev-deployment

# Copy built artifacts to deployment directory for CloudFormation packaging
echo "📋 Copying built artifacts to deployment directory..."
rsync -a ../api-gateway-routes/ ./api-gateway-routes/
rsync -a ../javabuilder-authorizer/ ./javabuilder-authorizer/
rsync -a ../org-code-javabuilder/ ./org-code-javabuilder/

# Process ERB template (following production buildspec)
echo "🔄 Processing ERB template..."
erb -T - "$TEMPLATE_PATH/template.yml.erb" > "$APP_TEMPLATE"
echo "✅ Generated CloudFormation template: $APP_TEMPLATE"

# Lint template (following production buildspec)
echo "🗺️ Linting CloudFormation template..."
if command -v cfn-lint >/dev/null 2>&1; then
    cfn-lint "$APP_TEMPLATE"
    echo "✅ Template linting passed"
else
    echo "⚠️ cfn-lint not found, skipping template validation"
fi

# Create environment config (following production buildspec)
echo "⚙️ Creating environment config..."
if [ -f "$TEMPLATE_PATH/config/create-environment-config.sh" ]; then
    "$TEMPLATE_PATH/config/create-environment-config.sh"
else
    echo "⚠️ Environment config script not found, skipping..."
fi

echo "✅ Using existing wildcard SSL certificate for dev environment..."



# Package template (following production buildspec pattern)
echo "📦 Packaging CloudFormation template..."
aws cloudformation package \
    --template-file "$APP_TEMPLATE" \
    --s3-bucket "$ARTIFACT_STORE" \
    --s3-prefix package \
    --output-template-file "$PACKAGED_TEMPLATE"

echo "✅ Template packaged successfully"

echo "🔍 Checking if application stack exists..."
if aws cloudformation describe-stacks --stack-name "$STACK_NAME" --profile "$PROFILE" >/dev/null 2>&1; then
    echo "🔄 Updating existing application stack: $STACK_NAME"
    ACTION="update"
else
    echo "🆕 Creating new application stack: $STACK_NAME"
    ACTION="create"
fi

# Deploy stack using CloudFormation with SSL certificates
echo "🚀 Deploying application stack with SSL certificates..."
aws cloudformation deploy \
    --stack-name "$STACK_NAME" \
    --template-file "$PACKAGED_TEMPLATE" \
    --s3-bucket "$ARTIFACT_STORE" \
    --capabilities CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND \
    --parameter-overrides \
        BaseDomainName=dev-code.org \
        BaseDomainNameHostedZonedID=Z2LCOI49SCXUGU \
        SubdomainName=javabuilder-dev \
        WildcardCertificateArn=arn:aws:acm:us-east-1:165336972514:certificate/bb245651-2ce8-4864-9975-c833af199154 \
        ProvisionedConcurrentExecutions=1 \
        ReservedConcurrentExecutions=3 \
        LimitPerHour=50 \
        LimitPerDay=150 \
        TeacherLimitPerHour=5000 \
        StageName=Prod \
        SilenceAlerts=true \
        HighConcurrentExecutionsTopic=CDO-Urgent \
        HighConcurrentExecutionsAlarmThreshold=400 \
    --profile "$PROFILE" \
    --region "$REGION"

echo "✅ Application deployment completed successfully!"

echo "📊 Stack Outputs:"
aws cloudformation describe-stacks \
    --stack-name "$STACK_NAME" \
    --profile "$PROFILE" \
    --region "$REGION" \
    --query 'Stacks[0].Outputs[*].[OutputKey,OutputValue,Description]' \
    --output table

echo "🎉 Deployment Summary:"
echo "   Stack Name: $STACK_NAME"
echo "   Region: $REGION"
echo "   SSL Certificates: ENABLED (using wildcard certificate)"
echo "   🔗 HTTPS endpoints ready for testing"

# Cleanup temp files and copied artifacts
rm -f "$APP_TEMPLATE" "$PACKAGED_TEMPLATE"
rm -rf api-gateway-routes javabuilder-authorizer org-code-javabuilder

echo "✅ Deployment complete!"

