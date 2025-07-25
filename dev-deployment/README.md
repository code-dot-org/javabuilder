# JavaBuilder Development Deployment

## Recommended Approach

For development stack deployment, use the Ruby script in `/cicd/3-app`:

```bash
cd ../cicd/3-app
./deploy-development-stack.rb
```

See [Development Deployment README](../cicd/3-app/README.md) for comprehensive documentation.

## Legacy Script

This directory contains a legacy shell script for development deployment:
- `deploy-javabuilder-dev-with-ssl.sh` - Legacy deployment script

**Prerequisites for legacy script:**
- Create S3 artifact bucket manually: `aws s3 mb s3://javabuilder-dev-artifacts --profile codeorg-dev`
- Or set `ARTIFACT_STORE` environment variable to existing bucket name

The new Ruby-based approach is recommended for better error handling, artifact management, and consistency with other Code.org projects.

## Architecture

Javabuilder uses a two-stack architecture:
1. **Base Infrastructure** (`javabuilder-iam`) - IAM roles and policies
2. **Application Stack** (`javabuilder-dev`) - Lambda functions, API Gateway, etc.

The deployment script handles both stacks automatically.

