#!/bin/bash

# Deploy JavaBuilder without SSL certificates for dev environment  
# Based on production buildspec.yml but with SSL removal for dev

set -e

PROFILE="codeorg-dev"
REGION="us-east-1"
STACK_NAME="javabuilder-dev"
TEMPLATE_PATH="../cicd/3-app/javabuilder"
APP_TEMPLATE="app-template.yml"
NO_SSL_TEMPLATE="app-template-no-ssl.yml"
PACKAGED_TEMPLATE="packaged-app-template-no-ssl.yml"

# Set artifact bucket - use environment variable if available, otherwise use default
if [ -z "$ARTIFACT_STORE" ]; then
    ARTIFACT_STORE="javabuilder-dev-artifacts"
fi
echo "✅ Using artifact bucket: $ARTIFACT_STORE"

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

# Return to deployment directory
cd ../dev-deployment

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

echo "🚫 Removing SSL certificate resources from template..."

# Remove SSL certificate resources from the processed template
python3 << 'PYTHON_SCRIPT'
import re

# Read the processed template
with open('app-template.yml', 'r') as f:
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
with open('app-template-no-ssl.yml', 'w') as f:
    f.write(content_fixed)

print("SSL resources completely removed and CloudFront certificate fixed")
PYTHON_SCRIPT



# Package template (following production buildspec pattern)
echo "📦 Packaging CloudFormation template..."
aws cloudformation package \
    --template-file "$NO_SSL_TEMPLATE" \
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

# Deploy stack using CloudFormation (dev-specific deployment logic)
echo "🚀 Deploying application stack without SSL certificates..."
aws cloudformation deploy \
    --stack-name "$STACK_NAME" \
    --template-file "$PACKAGED_TEMPLATE" \
    --capabilities CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND \
    --parameter-overrides \
        BaseDomainName=dev-code.org \
        BaseDomainNameHostedZonedID=Z2LCOI49SCXUGU \
        SubdomainName=javabuilder-dev \
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
echo "   SSL Certificates: DISABLED (for dev environment)"
echo "   🔗 Note: Use HTTP endpoints for testing"

# Cleanup temp files
rm -f "$APP_TEMPLATE" "$NO_SSL_TEMPLATE" "$PACKAGED_TEMPLATE"

echo "✅ Deployment complete!"

