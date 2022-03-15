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

## CI/CD Implementation

There are three phases of the CI/CD configuration. They are best explained in reverse order from the way they flow in practice.

### 3 - App

All of the application resources are defined in a CloudFormation template. We could manually update the CloudFormation stack with an updated template every time we have a code change, but it is considerably less effort and less risky to automate this with a CI/CD pipeline. By using parameters, we can use the same code to deploy multiple environments.

**Note: The application template has not been incorporated into the CI/CD implementation yet. That is the next step. The plan is to migrate 'template.yml.erb' from the root, into this folder.**

We also keep some CodeBuild configuration here, as this code tends to be more coupled to current application code, than the CI/CD code in the next section.

* "app.template.yml" - AWS resources for the application
* "buildspec.yml" - Main build/verify instructions

### 2 - CI/CD

In order to trigger the application resources to be updated upon changes to the source code, we need CI/CD resources. This is accomplished by a CloudFormation template that defines a stack of resources, primarily including a CodeBuild project and a CodePipeline pipeline which update the [App Stack](#3---app). These CI/CD resources only need to be deployed once per deployable branch, `main` in our case (we might choose to create adhoc environments by launching a new CI/CD stack targeting that branch).

These resources are deployed manually when changes occur. We could make yet another CodePipeline resource in the Setup section, but not today.

* "deploy-cicd.sh" - Shell script to deploy this stack.
* "cicd.template.yml" - AWS resources for the CI/CD infrastructure

### 1 - Setup

Finally, all of the above need some Roles to exist in the AWS accounts before we can run things with appropriate permisions. These roles are exported and used elsewhere. Elevated permissions is likely required to update these. This only needs to be created once, as roles and other resources can be shared by all CI/CD stacks for this application.

* "deploy-setup.sh"
* "setup.template.yml" - AWS resources for the Setup infrastructure
