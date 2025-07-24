# Javabuilder Dev Environment Deployment Guide

Scripts for deploying and managing the JavaBuilder development environment.

## Available Scripts

### Deploy with SSL (Production-like)
```bash
./deploy-javabuilder-dev-with-ssl.sh
```
Deploys a complete JavaBuilder development stack with SSL certificates using the existing wildcard certificate. This follows the production buildspec pattern and includes:
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
- Provides instructions for cleaning up base infrastructure if needed

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

## Environment Details

- **AWS Account**: 165336972514
- **Profile**: codeorg-dev
- **Region**: us-east-1
- **Stack Name**: javabuilder-dev
- **Domain**: Uses wildcard certificate for dev-code.org

For issues or questions, consult AWS CloudFormation logs or reach out to the DevOps team.

