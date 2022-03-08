# Javabuilder CI/CD

Objectives:

* General
  * Provide fast feedback on Pull Requests of any build/verify failures
  * Run all integration/acceptance tests in a non-prod environment
  * Provide a non-prod development environment that local resources can interact with
  * Easily create an ad-hoc environment for a given branch, which updates with changes
  * Eliminate or minimize manual AWS Console configuration
  * Use policies and roles with least-privilege
* **CodeBuild** For every PR created that merges into `main`
  * Build the project, ensure it compiles/transpiles/validates
  * Run all tests
  * Update the PR with the build/test status
  * Provide easily accessible logs and artifacts from the build/test job
* **CodePipeline** For every commit merged to `main`
  * Run tests and build artifacts necessary for deployment
  * Deploy the application to a Test environment
    * As prod-like as possible
  * Run integration and acceptance tests in the Test environment
  * Make test results and artifacts easily accessible to developers
  * If Test environment passes all tests, then deploy to Prod environment
  * Deploy the application to a Dev environment

## Implementation

### cicd/1-setup

The CI/CD resources require certain IAM Roles, S3 Buckets, and other resources to exist. These resources are more global and could be used by multiple builds and pipelines. The <cicd/1-setup/setup.template.yml> cloudformation template creates and exports shared resources. It is deployed manually whenever there are changes via the <cicd/1-setup/deploy-setup.sh> script.

### cicd/2-cicd

This directory contains CodeBuild and CodePipeline resources for building, verifying, and deploying the application. Templates here are parameterized to scope resources to a **target branch**, likely `main`. Any changes to files in this directory will need to be manually applied via the <cicd/2-cicd/deploy-pipeline.sh> script.

### cicd/3-app

This directory contains resources necessary for running the application itself. Templates are parameterized to allow multiple environments to be deployed with the same code. The build/verify scripts are also stored here. Any changes to files in this directory will be enacted upon PR or merge by the CodeBuild and CodePipeline resources defined in "cicd/2-cicd"
