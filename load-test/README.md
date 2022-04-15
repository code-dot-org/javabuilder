# Load Testing
This folder contains scripts and helpers for load testing Javabuilder. Load tests are instrumented via [k6](https://k6.io/docs/).

## One time set up
- [Install Docker](https://docs.docker.com/get-docker/)

## How to run
### Run Locally
- Ensure docker is running. This usually entails opening Docker Desktop.
- Generate a random password for your session. Copy this password to [configuration.js](scripts/configuration.js#L50)
  and [jwt_helper.rb](../javabuilder-authorizer/jwt_helper.rb#L3). Also update [jwt_helper.rb](../javabuilder-authorizer/jwt_helper.rb#L2) 
  with `IS_LOAD_TEST = true`. Do not commit or merge this password. If you do, immediately change your deploy of Javabuilder to no longer use it.
- Deploy an instance of Javabuilder with your changes.
- From this folder, run `docker build -t load-testing .` (You can use any name you would like in place of `load-testing`)
- Run `docker run load-testing`

### Run on AWS
- See instructions in [this document](https://docs.google.com/document/d/1GIiiMYCLbPE9QAqTRzHyslBzjFNvV1V1q8tdGwpKiNY).


## Customizing a Load Test
### Load Configuration Options
In [loadTest.js](scripts/loadTest.js#L19) you can modify 2 basic parameters for the load. 

The first is the user goal. This is the number of expected total Javabuilder users 
you would like to simulate. An important thing to note here is this is not the number 
of Virtual Users we will simulate during the test. The number of Virtual Users is the 
user goal divided by 30, as that is the highest load we saw historically based on number of users.

The second is the high load time. This is the time in minutes the test will run at its highest load.
This time does not include a 2 minute manual ramp-up stage.

There is also a slightly more complex toggle, [`SHOULD_SLEEP`](scripts/loadTest.js#L28). If this is set to
true, requests will be spaced out at a specific interval, to simulate more realistic traffic. If it is set
to false, requests will never sleep, which will ensure we have concurrent requests equal to the number of
Virtual Users. If you want to simulate the highest possible load, use `false` here. If you do use `false`,
you should not run the load test for very long, as you will get a lot of data (and send a lot of requests)
from 5 minutes of high load.

### Configure Student Code
If you would like to add new source code that the load test will upload to Javabuilder, 
add the JSON string to [sources.js](scripts/sources.js). An easy way to generate this data is
to run Java Lab normally with your desired source code, then copy over the JSON from the content bucket.
Then update [loadTest.js](scripts/loadTest.js#L25) to use your new sources object.

### Add a Metric
We have many [k6 metrics](https://k6.io/docs/using-k6/metrics/) we calculate during a load test. If you would
like to create a new one, initialize it with the other [metrics](loadTest.js#L30), and track it wherever makes sense.
The metric will automatically show up in the test summary so long as the value is not zero.

### Change the script file
If you want to create a fully new load test script, you can substitute the new script name in the [Dockerfile](Dockerfile#L2).

### Further Details
If you need to do further customization, the [k6 documention](https://k6.io/docs/) and 
[API Guide](https://k6.io/docs/javascript-api/) detail the capabilities available to you.