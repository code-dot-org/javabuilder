# Javabuilder App
### Local Development
To run Javabuilder in isolation, run the `LocalMain` class from an IDE (IntelliJ is recommended).
This will exercise the compile and run logic in isolation. Javabuilder will execute the
program in src/main/resources/MyClass.java. Input and output will be directed to the terminal.

### Developing with Dashboard
In order to run Java Lab (Code Studio client) with Javabuilder, use the WebSocketServer.
This is a local replacement of AWS API Gateway. 

Instructions (Note: this method is still in development)
1. Launch the WebSocketServer: `gradle appRun`
1. Launch dashboard using the instructions here: https://github.com/code-dot-org/code-dot-org/blob/staging/SETUP.md#overview 
1. Navigate to any Java Lab level, for example: http://localhost-studio.code.org:3000/projects/javalab/new
1. Click the "Run" button

Alternatively, you can test in "headless" mode
1. Install wscat: `npm install -g wscat`
1. Launch the WebSocketServer: `gradle appRun`
1. In a terminal, connect to the server with wscat: `wscat -c ws://localhost:8080/javabuilder`

### Developing with AWS (under construction)
In order to test against the AWS services, you'll need to integrate your IDE with AWS SAM.

1. Set up AWS SAM: https://docs.aws.amazon.com/toolkit-for-jetbrains/latest/userguide/setup-toolkit.html
1. Connect the first time: https://docs.aws.amazon.com/toolkit-for-jetbrains/latest/userguide/key-tasks.html#key-tasks-first-connect
1. This is still in development. Check in with the CSA team to get the latest details on running with AWS.
1. Open LambdaRequestHandler, set any configurations, and run the lambda.
