# Integration Tests

This folder contains integration tests that run against a full Javabuilder stack. These tests are meant to be run
as part of Javabuilder's CI/CD process. You can also run the tests locally in standalone mode.

## Running Tests Locally

You can run tests locally against a custom deploy of Javabuilder.

1. Ensure you are using AWS credentials for the production account, as the tests require retrieval of AWS Secrets.
2. Edit the values in [standalone.config](./standalone.config) to point to the desired deployed environment, `javabuilder-test` by default.
3. `./run-integration-tests.sh`

### Running Individual Tests

Use the `-g` option to target a single test or group of tests; for example: `./run-integration-tests.sh -g Theater`

The `-g` option runs any tests matching the provided string/pattern (see [Mocha docs](https://mochajs.org/#usage) for more information).

## Adding New Tests

Write new tests in the [./test/](./test/) directory. You can also use and update support code as needed.
Support code:

- [./test/lib/](./test/lib/) contains support code for writing tests. Relevant files include:
- - [JavabuilderConnectionHelper.js](./test/lib/JavabuilderConnectionHelper.js) helps create and manage a connection to a Javabuilder instance.
- - [sources.js](./test/lib/sources.js) contains source code used for tests. To use a new source for a test, add it to this file. An easy way to find source
    code is to run a Java Lab project on Code Studio and retrieve the sources.json file for that request from the associated Javabuilder S3 bucket. Copy the contents
    of the sources.json file into this file and export it as a new variable.
- [./test/helpers/](./test/helpers/) contains helper functions for setting up common test scenarios, such as verifying that a set of messages was sent by Javabuilder.

For a given mini-app, add new tests to the associated test file. If needed, add a new source object to [sources.js](./test/lib/sources.js)

Some things to keep in mind:

- As we are testing against a real API, network requests can take some time. You'll likely need to increase Mocha's test timeout, which by default is 2000ms.
  Cold starts can take as much as 15s, so 20s (20000ms) is a good place to start. To do so, add `.timeout(value)` to the end of your test function, e.g.

```
it("tests something", (done) => {
  ... test ...
}).timeout(millis);
```

- Another result of testing against a real WebSocket API is that test verifications will most likely be done asynchronously. In Mocha, we can use the `done()` callback
  to test asynchronously. If used, the test will not complete until the `done()` callback has been called. See [testHelpers.js](./test/helpers/testHelpers.js) for an example.
