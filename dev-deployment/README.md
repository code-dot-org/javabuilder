# Javabuilder Dev Environment Deployment Guide

Scripts for deploying and managing the JavaBuilder development environment.

## Available Scripts

### Deploy with SSL (Production-like)
```bash
./deploy-javabuilder-dev-with-ssl.sh
```
Deploys a complete JavaBuilder development stack with SSL certificates using the existing wildcard certificate. This follows the production buildspec pattern and includes:
- **Checks and deploys IAM base infrastructure** (if not already deployed)
- Builds all components (javabuilder-authorizer, org-code-javabuilder, api-gateway-routes)
- Processes ERB templates
- Deploys with SSL certificates and custom domain
- Uses existing wildcard certificate for dev-code.org

### Clean Up Development Environment
```bash
./cleanup-javabuilder-dev.sh
```
Removes the development stack and cleans up resources:
- Deletes the javabuilder-dev CloudFormation stack
- Lists any remaining S3 buckets that may need manual cleanup

## Prerequisites

### Required Software
- **AWS CLI**: Configure with `codeorg-dev` profile
- **Ruby**: For ERB template processing
- **Java/Gradle**: For building org-code-javabuilder components

### Required Permissions
- CloudFormation stack management
- S3 bucket creation and object management
- Lambda function deployment
- API Gateway management

## Architecture Overview

Javabuilder uses a two-stack deployment architecture:

### 1. Base Infrastructure Stack (`javabuilder-iam`)
**Purpose**: Contains shared IAM roles and policies required by all Javabuilder deployments.

**Contains**:
- `JavabuilderPutSourcesLambdaRole` - For uploading student code to S3
- `JavabuilderAuthorizerLambdaRole` - For API Gateway authorization
- `JavabuilderSessionManagerMessageRelayLambdaRole` - For WebSocket message handling
- `JavabuilderBuildAndRunLambdaRole` - For compiling and executing student code
- `JavabuilderAPIGatewayRole` - For API Gateway operations
- Associated policies for S3, DynamoDB, CloudWatch, and Lambda access

**Why Separate?**
- **Security**: IAM roles require elevated privileges to deploy
- **Reusability**: Multiple dev instances can share the same IAM roles
- **Stability**: IAM roles change less frequently than application code

### 2. Application Stack (`javabuilder-dev`)
**Purpose**: Contains the actual Javabuilder application resources.

**Contains**:
- Lambda functions (authorizer, API routes, build/run functions)
- API Gateway configurations (HTTP and WebSocket APIs)
- DynamoDB tables for throttling and health checks
- S3 buckets for content storage
- CloudFront distribution
- SSL certificates and Route53 records

**Dependencies**: Imports IAM roles from the base infrastructure stack using CloudFormation `ImportValue`.

## Environment Details

- **AWS Account**: 165336972514
- **Profile**: codeorg-dev
- **Region**: us-east-1
- **Base Stack**: javabuilder-iam (deployed automatically if missing)
- **App Stack**: javabuilder-dev
- **Domain**: Uses wildcard certificate for dev-code.org

For issues or questions, consult AWS CloudFormation logs or reach out to the DevOps team.

