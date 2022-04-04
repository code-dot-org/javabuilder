# Load Testing
This folder contains scripts and helpers for load testing.

## One time set up
- [Install Docker](https://docs.docker.com/get-docker/)

## How to run
### Run Locally
- Ensure docker is running. This usually entails opening Docker Desktop.
- Generate a random password for your session. Copy this password to [configuration.js](scripts/configuration.js#L50)
  and [jwt_helper.rb](../javabuilder-authorizer/jwt_helper.rb#L3). Also update [jwt_helper.rb](../javabuilder-authorizer/jwt_helper.rb#L2) with `IS_LOAD_TEST = true`. Do not commit or merge this password. If you do, immediately change your deploy of Javabuilder to no longer use it.
- Deploy an instance of Javabuilder with your changes.
- From this folder, run `docker build -t load-testing .` (You can use any name you would like in place of `load-testing`)
- Run `docker run load-testing`

### Run on AWS
- See instructions in [this document](https://docs.google.com/document/d/1GIiiMYCLbPE9QAqTRzHyslBzjFNvV1V1q8tdGwpKiNY).
