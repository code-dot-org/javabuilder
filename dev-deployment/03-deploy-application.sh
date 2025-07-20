#!/bin/bash

# Step 3: Deploy Application Stack
# This script deploys the main Javabuilder application

set -e

# Configuration
AWS_PROFILE="codeorg-dev"
STACK_NAME="javabuilder-dev"
REGION="us-east-1"
BASE_STACK_NAME="javabuilder-base-infrastructure"
TEMPLATE_FILE="dev-app-template.yml"
PACKAGED_TEMPLATE_FILE="packaged-dev-app-template.yml"
PARAMS_FILE="dev-deployment-params.json"

echo "🚀 Starting Javabuilder Application Deployment..."

# Step 1: Check AWS CLI and credentials
echo "📋 Checking AWS CLI and credentials for profile: $AWS_PROFILE..."
if ! aws sts get-caller-identity --profile "$AWS_PROFILE" > /dev/null 2>&1; then
    echo "❌ AWS CLI not configured or no valid credentials found for profile: $AWS_PROFILE"
    exit 1
fi

echo "✅ AWS credentials verified for profile: $AWS_PROFILE"

# Step 2: Check base infrastructure
echo "🔍 Checking base infrastructure..."
if ! aws cloudformation describe-stacks --stack-name "$BASE_STACK_NAME" --profile "$AWS_PROFILE" > /dev/null 2>&1; then
    echo "❌ Base infrastructure stack not found!"
    echo "Please run ./01-deploy-base-infrastructure.sh first"
    exit 1
fi

echo "✅ Base infrastructure found"

# Step 3: Get artifact bucket from base infrastructure
echo "📦 Getting artifact bucket from base infrastructure..."
ARTIFACT_BUCKET_NAME=$(aws cloudformation describe-stacks \
    --stack-name "$BASE_STACK_NAME" \
    --query 'Stacks[0].Parameters[?ParameterKey==`ArtifactBucket`].ParameterValue' \
    --output text \
    --profile "$AWS_PROFILE")

if [ -z "$ARTIFACT_BUCKET_NAME" ]; then
    echo "❌ Could not find artifact bucket from base infrastructure"
    exit 1
fi

echo "✅ Using artifact bucket: $ARTIFACT_BUCKET_NAME"

# Step 4: Process ERB template
echo "🔄 Processing ERB template..."
erb -T - ../cicd/3-app/javabuilder/template.yml.erb > "$TEMPLATE_FILE"
echo "✅ Generated CloudFormation template: $TEMPLATE_FILE"

# Step 5: Run environment config script
echo "⚙️ Running environment configuration script..."
if [ -f "cicd/3-app/javabuilder/config/create-environment-config.sh" ]; then
    bash cicd/3-app/javabuilder/config/create-environment-config.sh
    echo "✅ Environment configuration completed"
else
    echo "⚠️  Environment configuration script not found, skipping..."
fi

# Step 6: Create minimal required artifacts if missing
echo "🔧 Creating minimal artifacts if missing..."

# Create minimal Java artifacts if they don't exist
if [ ! -f "../org-code-javabuilder/lib/build/distributions/lib.zip" ]; then
    echo "⚠️  Java artifacts missing - creating minimal placeholder..."
    mkdir -p ../org-code-javabuilder/lib/build/distributions
    echo "# Placeholder Java application" > ../org-code-javabuilder/lib/build/distributions/placeholder.java
    cd ../org-code-javabuilder/lib/build/distributions
    zip lib.zip placeholder.java
    cd - > /dev/null
    echo "✅ Created minimal lib.zip placeholder"
fi

# Create minimal font config if missing
if [ ! -f "../org-code-javabuilder/font_config.zip" ]; then
    echo "⚠️  Font config missing - creating minimal placeholder..."
    mkdir -p ../org-code-javabuilder/temp_font
    echo "# Minimal font configuration" > ../org-code-javabuilder/temp_font/fonts.conf
    cd ../org-code-javabuilder/temp_font
    zip -r ../font_config.zip .
    cd - > /dev/null
    rm -rf ../org-code-javabuilder/temp_font
    echo "✅ Created minimal font_config.zip"
fi

# Create minimal runtime directory if missing
if [ ! -d "../org-code-javabuilder/change_runtime_directory" ]; then
    # Remove file if it exists and create directory
    if [ -f "../org-code-javabuilder/change_runtime_directory" ]; then
        rm ../org-code-javabuilder/change_runtime_directory
    fi
    echo "⚠️  Runtime directory missing - creating minimal placeholder..."
    mkdir -p ../org-code-javabuilder/change_runtime_directory
    echo "#!/bin/bash" > ../org-code-javabuilder/change_runtime_directory/change_runtime_directory
    echo "# Minimal runtime directory change script" >> ../org-code-javabuilder/change_runtime_directory/change_runtime_directory
    chmod +x ../org-code-javabuilder/change_runtime_directory/change_runtime_directory
    echo "✅ Created minimal change_runtime_directory"
else
    echo "✅ Runtime directory already exists"
fi

# Step 7: Package the CloudFormation template
echo "📦 Packaging CloudFormation template..."
aws cloudformation package \
    --template-file "$TEMPLATE_FILE" \
    --s3-bucket "$ARTIFACT_BUCKET_NAME" \
    --s3-prefix "package" \
    --output-template-file "$PACKAGED_TEMPLATE_FILE" \
    --profile "$AWS_PROFILE"
echo "✅ Template packaged successfully"

# Step 8: Check if stack exists
echo "🔍 Checking if application stack exists..."
STACK_EXISTS=$(aws cloudformation describe-stacks --stack-name "$STACK_NAME" --query 'Stacks[0].StackStatus' --output text --profile "$AWS_PROFILE" 2>/dev/null || echo "DOES_NOT_EXIST")

if [ "$STACK_EXISTS" = "DOES_NOT_EXIST" ]; then
    echo "🆕 Creating new application stack: $STACK_NAME"
    OPERATION="create-stack"
    WAIT_CONDITION="stack-create-complete"
else
    echo "🔄 Updating existing application stack: $STACK_NAME"
    OPERATION="update-stack"
    WAIT_CONDITION="stack-update-complete"
fi

# Step 9: Upload template to S3 (to avoid size limit)
echo "📎 Uploading template to S3..."
TEMPLATE_S3_KEY="templates/$(date +%s)-$PACKAGED_TEMPLATE_FILE"
aws s3 cp "$PACKAGED_TEMPLATE_FILE" "s3://$ARTIFACT_BUCKET_NAME/$TEMPLATE_S3_KEY" --profile "$AWS_PROFILE"
TEMPLATE_URL="https://$ARTIFACT_BUCKET_NAME.s3.amazonaws.com/$TEMPLATE_S3_KEY"
echo "✅ Template uploaded to: $TEMPLATE_URL"

# Step 10: Deploy the application stack
echo "🚀 Deploying application stack..."
aws cloudformation "$OPERATION" \
    --stack-name "$STACK_NAME" \
    --template-url "$TEMPLATE_URL" \
    --parameters file://"$PARAMS_FILE" \
    --capabilities CAPABILITY_IAM CAPABILITY_AUTO_EXPAND \
    --region "$REGION" \
    --tags Key=Environment,Value=development Key=Project,Value=javabuilder Key=Component,Value=application \
    --profile "$AWS_PROFILE"

echo "⏳ Waiting for application deployment to complete..."
aws cloudformation wait "$WAIT_CONDITION" --stack-name "$STACK_NAME" --profile "$AWS_PROFILE"

# Step 11: Get stack outputs
echo "📋 Application deployment completed! Getting outputs..."
aws cloudformation describe-stacks \
    --stack-name "$STACK_NAME" \
    --query 'Stacks[0].Outputs' \
    --output table \
    --profile "$AWS_PROFILE"

echo "✅ Deployment completed successfully!"
echo "📝 Next steps:"
echo "   1. Verify the Lambda functions are working"
echo "   2. Test the API Gateway endpoints"
echo "   3. Monitor CloudWatch logs for any issues"
echo "   4. Update Route 53 DNS if needed"
echo ""
echo "⚠️  Note: Placeholder Java artifacts were used."
echo "   For full functionality, install Java and run ./02-build-java-components.sh"
echo "   Then redeploy with ./03-deploy-application.sh"
