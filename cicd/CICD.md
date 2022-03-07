# Javabuilder CI/CD

## Setup

The CI/CD resources require certain IAM Roles, S3 Buckets, and other resources to exist. These resources are more global and could be used by multiple builds and pipelines.

Implementation

* The [setup] cloudformation template creates and exports shared resources. It is deployed manually whenever there are changes

## Continuous Integration

Goals:

* For every PR created that merges into `main`
  * Build the project, ensure it compiles/transpiles/validates
  * Run all tests
  * Update the PR with the build/test status
  * Provide easily accessible logs and artifacts from the build/test job

Implementation:

* The [pipeline](cicd/2-cicd/pipeline.template.yml) cloudformation template creates a CodeBuild project
* When there are updates to the pipeline template, it must be manually deployed.
* The CodeBuild Project is triggered by any new PR or updates to a PR that targets the `main` branch.

## Continuous Delivery & Deployment

Goals

* Upon merge to main
  * Trigger a build and test, only continuing if green
  * Deploy the project to one or more non-prod environments
  * Deploy the project to the production environment
* Ad-hoc in a feature branch
  * Manually create an ad-hoc environment, that updates upon commits to that branch

Questions

* How important is it to have a manual release to production? Can we have a CD setup where merges to `main` will always go to production unless automated steps stop it?
  * you could accomplish this by disabling state transitions perhaps, then only once the transition is enabled, will artifacts from one stage to to target stage.

## Updating CI/CD Configuration

There are two options for updating the CI/CD configuration.

1. Manually update the cloudformation stack with CI/CD resources
   1. If you commit your CICD changes to `main` before you execute the update, then you'll have an App Pipeline running with old CICD config
2. Use another Pipeline to update the cloudformation stack
   1. Triggering this pipeline on a code change means you will triger both your App Pipeline and your CICD Pipeline at the same time, and the latter might try to modify the first, getting into a race condition. Usually, the building and testing in the App Pipeline means the CICD Pipeline will win the race, but not if what you've modified is the configuration for that build and test phase.
      1. A solution for monorepos, which has a similar problem: https://awscloudfeed.com/whats-new/devops/integrate-github-monorepo-with-aws-codepipeline-to-run-project-specific-ci-cd-pipelines

The problems here really only exist when a single merge has both CICD and APP changes. If you don't mix those, then you're clear. Perhaps a validation step could check this?