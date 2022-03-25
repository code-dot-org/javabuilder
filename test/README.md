# Load Testing
This folder contains scripts and helpers for load testing.

## One time set up
- [Install Docker](https://docs.docker.com/get-docker/)

## How to run
- Ensure docker is running. This usually entails opening Docker Desktop.
- From a local code studio, run a Java Lab project and copy over the authorization token.
- From this folder, run `docker build -t load-testing .`
- Run `docker run load-testing`