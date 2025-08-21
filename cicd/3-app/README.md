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
- **S3 Artifact Bucket** created in your target AWS account (see setup instructions below)
- **Java SDK** installed (OpenJDK 11+ recommended)
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
- `--artifact_bucket BUCKET`: S3 bucket for build artifacts (must exist)
- `--subdomain_name SUBDOMAIN`: Subdomain for the service (default: javabuilder-dev)

## Setup

### Artifact Bucket Setup

**IMPORTANT:** Before deploying, you must create an S3 bucket in your target AWS account for storing deployment artifacts. The deployment script will not create this bucket automatically.

```bash
# Create a bucket (replace with your desired bucket name)
aws s3 mb s3://my-javabuilder-artifacts --profile codeorg-dev --region us-east-1

# Then use it in deployment
./deploy-development-stack.rb --artifact_bucket my-javabuilder-artifacts
```

If no `--artifact_bucket` is specified, the script will use `{stack_name}-artifacts` as the default bucket name. **This bucket must already exist before running the deployment.**

## What the Script Does

1. **Artifact Bucket Verification**: Verifies the required S3 bucket exists for deployment artifacts (fails if not found)
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
Ensure OpenJDK 11+ is installed and accessible in your PATH.

**Installation options:**
- **macOS with Homebrew:** `brew install openjdk@11`
- **Ubuntu/Debian:** `sudo apt-get install openjdk-11-jdk`
- **CentOS/RHEL:** `sudo yum install java-11-openjdk-devel`
- **Manual installation:** Download from [OpenJDK website](https://openjdk.org/)

**Verify installation:**
```bash
java -version
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
- Access the pre-created S3 artifact bucket
- Deploy CloudFormation stacks
- Create Lambda functions and other AWS resources

If you get an error that the artifact bucket doesn't exist, ensure you've created it according to the setup instructions above.

