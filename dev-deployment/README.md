# Javabuilder Dev Environment Deployment Guide

Comprehensive guide for deploying and managing the JavaBuilder AWS Lambda environment for development.

## 🎯 Quick Start

For a complete no-SSL deployment (recommended for dev):
```bash
./deploy-javabuilder-dev-no-ssl-fixed.sh
```

For modular deployment:
```bash
./01-deploy-base-infrastructure.sh  # Deploy IAM roles first
./02-build-java-components.sh       # Build Java artifacts  
./03-deploy-application.sh          # Deploy application stack
```

## 📋 Current Environment Status

- **AWS Account**: 165336972514
- **Profile**: codeorg-dev
- **Region**: us-east-1
- **Stack Name**: javabuilder-dev
- **Bucket**: javabuilder-dev-artifacts-*

### ✅ Working Components
- ✅ AWS CLI configured with `codeorg-dev` profile
- ✅ S3 bucket creation for artifacts
- ✅ ERB template processing with SSL removal
- ✅ CloudFormation template packaging and deployment
- ✅ Java artifacts built and packaged correctly
- ✅ Lambda functions deployed and active

### 🔧 Key Components
- **javabuilder-authorizer**: Handles API Gateway authorization
- **api-gateway-routes**: API Gateway interaction logic
- **org-code-javabuilder**: Core Java logic built with Gradle
- **WebSocket API**: Real-time communication for build sessions
- **CloudFront**: Content delivery for build artifacts

## 🚀 Deployment Options

### Option 1: No-SSL Deployment (Recommended for Dev)
**Best for development environments without Route53 permissions**
```bash
./deploy-javabuilder-dev-no-ssl-fixed.sh
```
- Removes SSL certificates and custom domains
- Uses CloudFront default domain
- Faster deployment, fewer permissions needed

### Option 2: Full SSL Deployment
**For production-like environments with Route53 access**
```bash
./01-deploy-base-infrastructure.sh
./02-build-java-components.sh
./03-deploy-application.sh
```

### Option 3: Clean Slate Deployment
**If stack exists but needs complete refresh**
```bash
./cleanup-javabuilder-dev.sh      # Remove existing stack
./deploy-javabuilder-dev-no-ssl-fixed.sh  # Deploy fresh
```

## 🔧 Prerequisites

### Required Software
- **AWS CLI**: Configure with `codeorg-dev` profile
- **Ruby**: For ERB template processing
- **Java/Gradle**: For building org-code-javabuilder components

### Required Permissions
- CloudFormation stack management
- S3 bucket creation and object management
- Lambda function deployment
- IAM role creation (for base infrastructure)
- API Gateway management

### Pre-Deployment Check
```bash
./pre-deploy-check.sh  # Verify all prerequisites
```

## 📁 Required Artifacts

### Java Build Artifacts
- ✅ `org-code-javabuilder/lib/build/distributions/lib.zip`
- ✅ `org-code-javabuilder/font_config.zip` 
- ✅ `org-code-javabuilder/change_runtime_directory/` (directory)

### CloudFormation Templates
- `../cicd/3-app/javabuilder/template.yml.erb` (source)
- `process-template-no-ssl.rb` (SSL removal script)
- Generated templates: `template-no-ssl.yml`, `packaged-*.yml`

## 🔍 Deployment Process Details

### 1. Template Processing
- Processes ERB template with environment variables
- Removes SSL resources for no-SSL deployment
- Handles large template packaging via S3

### 2. Artifact Packaging
- Creates S3 bucket for deployment artifacts
- Packages Lambda code from local directories
- Uploads packaged template to S3

### 3. CloudFormation Deployment
- Uses `--template-url` for large templates
- Includes `CAPABILITY_AUTO_EXPAND` for SAM transforms
- Provides all required parameters via JSON file

### 4. Post-Deployment Verification
- Validates stack creation status
- Tests WebSocket API endpoint
- Verifies CloudFront distribution
- Confirms Lambda function deployment

## 🚨 Common Issues & Solutions

### Template Too Large
**Error**: Template body exceeds 51200 characters
**Solution**: Script automatically uploads to S3 and uses `--template-url`

### SSL Certificate Errors
**Error**: Certificate validation or Route53 permissions
**Solution**: Use no-SSL deployment script

### Missing IAM Roles
**Error**: Stack exports not found
**Solution**: Deploy base infrastructure first with `01-deploy-base-infrastructure.sh`

### Java Artifacts Missing
**Error**: CodeUri points to non-existent files
**Solution**: Run `02-build-java-components.sh` or ensure artifacts exist

### Stack in ROLLBACK_COMPLETE State
**Error**: Cannot update stack in failed state
**Solution**: Use `cleanup-javabuilder-dev.sh` to delete and recreate

## 🧪 Testing & Verification

### Health Check Script
```bash
./test-deployment-health.sh  # Verify deployment status
```

### Manual Verification
```bash
# Check stack status
aws cloudformation describe-stacks --stack-name javabuilder-dev --profile codeorg-dev

# Test WebSocket endpoint
aws apigatewayv2 get-apis --profile codeorg-dev

# Verify Lambda functions
aws lambda list-functions --profile codeorg-dev | grep -i javabuilder
```

## 🧹 Cleanup & Maintenance

### Clean Failed Deployments
```bash
./cleanup-failed-stack.sh     # Remove failed stacks
./cleanup-javabuilder-dev.sh  # Remove specific dev stack
```

### Artifact Management
- S3 buckets are created with unique suffixes
- Old artifacts remain in S3 (manual cleanup needed)
- CloudFormation stacks are idempotent (safe to redeploy)

## 📖 File Reference

### Main Scripts
- `deploy-javabuilder-dev-no-ssl-fixed.sh` - Complete no-SSL deployment
- `01-deploy-base-infrastructure.sh` - IAM roles and base resources
- `02-build-java-components.sh` - Build Java artifacts
- `03-deploy-application.sh` - Deploy application stack

### Configuration Files
- `dev-deployment-params.json` - CloudFormation parameters
- `dev.config.json` - Environment configuration
- `process-template-no-ssl.rb` - SSL removal script

### Generated Files
- `template-no-ssl.yml` - Processed template without SSL
- `packaged-*.yml` - CloudFormation packaged templates
- `runtime.zip` - Lambda runtime artifacts

For issues or questions, consult AWS CloudFormation logs or reach out to the DevOps team.

