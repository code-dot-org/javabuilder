# Load Testing
This folder contains scripts and helpers for load testing.

## One time set up
- [Install Docker](https://docs.docker.com/get-docker/)

## How to run
### Local Setup
Note: this will not work once throttling in enabled.
- Ensure docker is running. This usually entails opening Docker Desktop.
- On localhost code studio, [increase the token timeout](https://github.com/code-dot-org/code-dot-org/blob/dc88a86c265b85beda000e9657cb0d06cebd2171/dashboard/app/controllers/javabuilder_sessions_controller.rb#L81) 
  to the length of time you want to run your test.
  Then run a Java Lab project and copy over the authorization token to the `authToken` parameter in `hello-world-load-test.js`.
- From this folder, run `docker build -t load-testing .`
- Run `docker run load-testing`