#!/bin/bash

# Deploy JavaBuilder without SSL certificates for dev environment
# This bypasses Route53 permission issues while maintaining functionality

set -e

PROFILE="codeorg-dev"
REGION="us-east-1"
STACK_NAME="javabuilder-dev"
BASE_STACK_NAME="javabuilder-base-infrastructure"
TEMPLATE_PATH="../cicd/3-app/javabuilder"
PROCESSED_TEMPLATE="dev-app-template.yml"
NO_SSL_TEMPLATE="dev-app-template-no-ssl.yml"
PACKAGED_TEMPLATE="packaged-dev-app-template-no-ssl.yml"

# Ensure Java is in PATH
export PATH="/opt/homebrew/opt/openjdk@11/bin:$PATH"

echo "🚀 Starting Javabuilder Application Deployment (No SSL)..."
echo "📋 Checking AWS CLI and credentials for profile: $PROFILE..."

# Verify AWS credentials
if ! aws sts get-caller-identity --profile "$PROFILE" >/dev/null 2>&1; then
    echo "❌ AWS credentials not configured for profile: $PROFILE"
    echo "Please run: aws configure --profile $PROFILE"
    exit 1
fi
echo "✅ AWS credentials verified for profile: $PROFILE"

echo "🔍 Checking base infrastructure..."
if ! aws cloudformation describe-stacks --stack-name "$BASE_STACK_NAME" --profile "$PROFILE" >/dev/null 2>&1; then
    echo "❌ Base infrastructure not found. Please run ./01-deploy-base-infrastructure.sh first"
    exit 1
fi
echo "✅ Base infrastructure found"

echo "📦 Getting artifact bucket from base infrastructure..."
# Look for artifact bucket created by the base deployment - get just the first one
ARTIFACT_BUCKET=$(aws s3api list-buckets --profile "$PROFILE" --query 'Buckets[?starts_with(Name, `javabuilder-dev-artifacts`)].Name' --output text | awk '{print $1}')

if [ -z "$ARTIFACT_BUCKET" ] || [ "$ARTIFACT_BUCKET" = "None" ]; then
    echo "❌ Could not find javabuilder artifact bucket. Make sure base infrastructure was deployed."
    exit 1
fi
echo "✅ Using artifact bucket: $ARTIFACT_BUCKET"

echo "🔄 Processing ERB template..."
cd "$TEMPLATE_PATH"
# Use erb command directly with proper trim mode
erb -T - template.yml.erb > "../../../dev-deployment/$PROCESSED_TEMPLATE"
cd - > /dev/null
echo "✅ Generated CloudFormation template: $PROCESSED_TEMPLATE"

echo "🚫 Removing SSL certificate resources from template..."

# Remove SSL certificate resources from the processed template
python3 << 'PYTHON_SCRIPT'
import re

# Read the processed template
with open('dev-app-template.yml', 'r') as f:
    content = f.read()

# Target SSL resources that need complete removal
ssl_resources = [
    'HttpCertificate',
    'Certificate',
    'ContentApiCertificate',
    'HttpDomainName', 
    'DomainName',
    'ContentDomain',
    'HttpDomain',
    'Domain',
    'HttpDomainNameApiMapping',
    'DomainNameApiMapping',
    'ContentBucketPolicy'
]

lines = content.split('\n')
new_lines = []
remove_mode = False
resource_indent = 0

i = 0
while i < len(lines):
    line = lines[i]
    stripped = line.strip()
    
    # Check if this is a SSL resource declaration
    is_ssl_resource = False
    for ssl_resource in ssl_resources:
        if stripped.startswith(f'{ssl_resource}:'):
            is_ssl_resource = True
            break
    
    if is_ssl_resource:
        # Skip this entire resource block
        resource_indent = len(line) - len(line.lstrip())
        # Skip lines until we find the next resource at the same indent level or less
        i += 1
        while i < len(lines):
            next_line = lines[i]
            if next_line.strip() == '':
                i += 1
                continue
            next_indent = len(next_line) - len(next_line.lstrip())
            if next_indent <= resource_indent:
                # We've reached the next resource or higher level
                break
            i += 1
        continue
    
    # Keep non-SSL lines
    new_lines.append(line)
    i += 1

# Now fix CloudFront distribution and content references
content_fixed = '\n'.join(new_lines)

# Remove Aliases from CloudFront since we don't have custom domains
content_fixed = re.sub(
    r'\s+Aliases: \[!Sub "\$\{SubdomainName\}-content\.\$\{BaseDomainName\}"\]\s*\n',
    '',
    content_fixed,
    flags=re.MULTILINE
)

# Fix ViewerCertificate for CloudFront to use default certificate - handle multiple patterns
# First, try to match the original SSL certificate configuration
content_fixed = re.sub(
    r'ViewerCertificate:\s*\n\s*AcmCertificateArn: !Ref ContentApiCertificate\s*\n\s*MinimumProtocolVersion: [\w\.]+\s*\n\s*SslSupportMethod: [\w-]+',
    'ViewerCertificate:\n          CloudFrontDefaultCertificate: true',
    content_fixed,
    flags=re.MULTILINE | re.DOTALL
)

# Also handle cases where SSL resources were already removed but ViewerCertificate still has issues
content_fixed = re.sub(
    r'ViewerCertificate:\s*\n\s*AcmCertificateArn: !Ref AWS::NoValue\s*\n\s*MinimumProtocolVersion: [\w\.]+\s*\n\s*SslSupportMethod: [\w-]+',
    'ViewerCertificate:\n          CloudFrontDefaultCertificate: true',
    content_fixed,
    flags=re.MULTILINE | re.DOTALL
)

# Handle any remaining ViewerCertificate with SSL parameters
content_fixed = re.sub(
    r'ViewerCertificate:\s*\n[^\n]*AcmCertificateArn:[^\n]*\n[^\n]*MinimumProtocolVersion:[^\n]*\n[^\n]*SslSupportMethod:[^\n]*',
    'ViewerCertificate:\n          CloudFrontDefaultCertificate: true',
    content_fixed,
    flags=re.MULTILINE | re.DOTALL
)

# Also handle malformed ViewerCertificate lines
content_fixed = re.sub(
    r'Enabled: true\s+ViewerCertificate:',
    'Enabled: true\n        ViewerCertificate:',
    content_fixed
)

# Fix S3 Origins to include DomainName - handle both DomainName and RegionalDomainName
content_fixed = re.sub(
    r'Origins:\s*\n\s*- Id: ContentBucket\s*\n\s*DomainName: !GetAtt ContentBucket\.DomainName\s*\n\s*S3OriginConfig: \{\}',
    'Origins:\n          - Id: ContentBucket\n            DomainName: !GetAtt ContentBucket.RegionalDomainName\n            S3OriginConfig: {}',
    content_fixed,
    flags=re.MULTILINE | re.DOTALL
)

# Also handle case where no DomainName is specified
content_fixed = re.sub(
    r'Origins:\s*\n\s*- Id: ContentBucket\s*\n\s*S3OriginConfig: \{\}',
    'Origins:\n          - Id: ContentBucket\n            DomainName: !GetAtt ContentBucket.RegionalDomainName\n            S3OriginConfig: {}',
    content_fixed,
    flags=re.MULTILINE | re.DOTALL
)

# Replace ContentDomain references with CloudFront domain since we removed ContentDomain
content_fixed = re.sub(
    r'!Sub "https://\$\{ContentDomain\}"',
    '!Sub "https://${ContentCDN.DomainName}"',
    content_fixed
)

# Also handle HTTP references without protocol specified
content_fixed = re.sub(
    r'\$\{ContentDomain\}',
    '${ContentCDN.DomainName}',
    content_fixed
)

# Fix any remaining references to SSL certificates
content_fixed = re.sub(
    r'CertificateArn: !Ref [\w]+Certificate',
    'CertificateArn: !Ref AWS::NoValue',
    content_fixed
)

# Write the cleaned template
with open('dev-app-template-no-ssl.yml', 'w') as f:
    f.write(content_fixed)

print("SSL resources completely removed and CloudFront certificate fixed")
PYTHON_SCRIPT



echo "📦 Packaging CloudFormation template..."
# Copy the template to root directory for packaging since CodeUri paths are relative to root
cp "$NO_SSL_TEMPLATE" "../temp-template.yml"
cd ..
aws cloudformation package \
    --template-file "temp-template.yml" \
    --s3-bucket "$ARTIFACT_BUCKET" \
    --output-template-file "dev-deployment/$PACKAGED_TEMPLATE" \
    --profile "$PROFILE" \
    --region "$REGION"
rm temp-template.yml
cd dev-deployment

echo "✅ Template packaged successfully"

echo "🔍 Checking if application stack exists..."
if aws cloudformation describe-stacks --stack-name "$STACK_NAME" --profile "$PROFILE" >/dev/null 2>&1; then
    echo "🔄 Updating existing application stack: $STACK_NAME"
    ACTION="update"
else
    echo "🆕 Creating new application stack: $STACK_NAME"
    ACTION="create"
fi

echo "📎 Uploading template to S3..."
TIMESTAMP=$(date +%s)
TEMPLATE_KEY="templates/$TIMESTAMP-packaged-dev-app-template-no-ssl.yml"
aws s3 cp "$PACKAGED_TEMPLATE" "s3://$ARTIFACT_BUCKET/$TEMPLATE_KEY" --profile "$PROFILE"
TEMPLATE_URL="https://$ARTIFACT_BUCKET.s3.$REGION.amazonaws.com/$TEMPLATE_KEY"
echo "✅ Template uploaded to: $TEMPLATE_URL"

echo "🚀 Deploying application stack without SSL certificates..."
aws cloudformation ${ACTION}-stack \
    --stack-name "$STACK_NAME" \
    --template-url "$TEMPLATE_URL" \
    --capabilities CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND \
    --parameters \
        ParameterKey=BaseDomainName,ParameterValue=dev-code.org \
        ParameterKey=BaseDomainNameHostedZonedID,ParameterValue=Z2LCOI49SCXUGU \
        ParameterKey=SubdomainName,ParameterValue=javabuilder-dev \
        ParameterKey=ProvisionedConcurrentExecutions,ParameterValue=1 \
        ParameterKey=ReservedConcurrentExecutions,ParameterValue=3 \
        ParameterKey=LimitPerHour,ParameterValue=50 \
        ParameterKey=LimitPerDay,ParameterValue=150 \
        ParameterKey=TeacherLimitPerHour,ParameterValue=5000 \
        ParameterKey=StageName,ParameterValue=Prod \
        ParameterKey=SilenceAlerts,ParameterValue=true \
        ParameterKey=HighConcurrentExecutionsTopic,ParameterValue=CDO-Urgent \
        ParameterKey=HighConcurrentExecutionsAlarmThreshold,ParameterValue=400 \
    --profile "$PROFILE" \
    --region "$REGION"

echo "⏳ Waiting for application deployment to complete..."
aws cloudformation wait stack-${ACTION}-complete --stack-name "$STACK_NAME" --profile "$PROFILE" --region "$REGION"

if [ $? -eq 0 ]; then
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
    echo "   SSL Certificates: DISABLED (for dev environment)"
    echo "   🔗 Note: Use HTTP endpoints for testing"
else
    echo "❌ Deployment failed. Check CloudFormation events for details:"
    echo "   aws cloudformation describe-stack-events --stack-name $STACK_NAME --profile $PROFILE"
    exit 1
fi

# Cleanup temp files
rm -f "$PROCESSED_TEMPLATE" "$NO_SSL_TEMPLATE" "$PACKAGED_TEMPLATE"

echo "✅ Deployment complete!"

