# JavaBuilder Development Stack Deployment

This directory contains the deployment scripts and templates for deploying a JavaBuilder development stack to AWS.

## Quick Start

To deploy a development stack:

```bash
cd cicd/3-app
./deploy-development-stack.rb
```

The script will build all components, process the CloudFormation template, and deploy the stack to AWS using the default configuration.

## Prerequisites

Before running the deployment script, ensure you have:

- **AWS CLI** configured with appropriate credentials for the dev account
- **Java SDK** installed (OpenJDK 11 recommended)
- **Ruby 3.3+** installed
- **Bundler** installed for Ruby dependencies
- **cfn-lint** installed (optional, for template validation): `pip install cfn-lint`

## Script Options

The deployment script accepts several command-line options:

```bash
./deploy-development-stack.rb --help
```

Common options:
- `--profile PROFILE`: AWS CLI profile to use (default: codeorg-dev)
- `--stack_name NAME`: CloudFormation stack name (default: javabuilder-dev)
- `--artifact_bucket BUCKET`: S3 bucket for build artifacts (auto-created if needed)
- `--subdomain_name SUBDOMAIN`: Subdomain for the service (default: javabuilder-dev)

## What the Script Does

1. **Artifact Bucket Setup**: Creates or verifies the S3 bucket for deployment artifacts
2. **Component Building**: 
   - Builds `javabuilder-authorizer` using its build script
   - Builds `org-code-javabuilder` using Gradle (including tests)
   - Prepares `api-gateway-routes` (tests skipped due to dependency conflicts)
3. **Artifact Management**: Copies built components to `tmp/` directory for debugging
4. **Template Processing**: Processes the ERB template to generate CloudFormation YAML
5. **Template Validation**: Runs cfn-lint if available
6. **Template Packaging**: Uploads Lambda packages to S3 and updates template references
7. **Stack Deployment**: Deploys or updates the CloudFormation stack
8. **Output Display**: Shows stack outputs including service endpoints

## Build Artifacts

Build artifacts are preserved in the `tmp/` directory for debugging purposes:
- `tmp/api-gateway-routes/`: API Gateway routes component
- `tmp/javabuilder-authorizer/`: Lambda authorizer component  
- `tmp/org-code-javabuilder/`: Main JavaBuilder application
- `tmp/app-template-*.yml`: Processed CloudFormation template
- `tmp/packaged-app-template-*.yml`: Packaged template with S3 references

The `tmp/` directory is gitignored and safe to delete between deployments.

## SSL Configuration

The deployment uses the existing wildcard certificate for `*.dev-code.org` by default. The certificate ARN is hardcoded in the script but can be overridden via command-line options if needed.

## Stack Management

### Deploying a Stack
```bash
./deploy-development-stack.rb --stack_name my-javabuilder-test
```

### Deleting a Stack
Since development stack provisioning is rare, manual deletion from the AWS console is recommended:

1. Go to AWS CloudFormation console
2. Select your stack (e.g., `javabuilder-dev`)
3. Click "Delete" 
4. Confirm deletion

Alternatively, use AWS CLI:
```bash
aws cloudformation delete-stack --stack-name javabuilder-dev --profile codeorg-dev
```

## Differences from Production

- Uses wildcard certificate instead of generating new certificates
- Simplified parameter configuration suitable for development
- Build artifacts preserved locally for debugging
- Reduced concurrent execution limits appropriate for development usage

## Troubleshooting

### Java Build Issues
Ensure OpenJDK 11 is installed and in your PATH:
```bash
export PATH="/opt/homebrew/opt/openjdk@11/bin:$PATH"
```

### Ruby Version Issues
Ensure you're using Ruby 3.3+:
```bash
ruby --version
gem install bundler
```

### Template Validation Errors
Install cfn-lint for better error messages:
```bash
pip install cfn-lint
```

### S3 Bucket Access Issues
Verify your AWS credentials have permissions to:
- Create/access S3 buckets
- Deploy CloudFormation stacks
- Create Lambda functions and other AWS resources

## Legacy Scripts

The `dev-deployment/` directory contains legacy shell scripts that have been replaced by this Ruby-based approach. The new Ruby script follows the same patterns used by other Code.org projects and provides better error handling and artifact management.
