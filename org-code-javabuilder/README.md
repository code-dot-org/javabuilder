# Javabuilder build-and-run package
This portion of Javabuilder contains the code that compiles and runs student code as well
as the code for the `org.code` Java packages that are used in the Java Lab mini apps.

### Directory
* [lib](https://github.com/code-dot-org/javabuilder/tree/main/org-code-javabuilder/lib)
  contains the "backend" code that compiles, builds, and runs student code. This is not
  user-facing. It also handles the WebSocket layer that communicates between the client
  and the running program. Also contained here are implementations of the communication
  protocol and program initialization that are specific to local development and AWS
  development. Local implementations are in the `dev.javabuilder` package while AWS
  implementations are in the main `org.code.javabuilder` package and are prefixed with
  `AWS` or `Lambda`
* [media](https://github.com/code-dot-org/javabuilder/tree/main/org-code-javabuilder/media)
  contains a package that can be imported by the user to edit sounds, colors, fonts, and
  images.
* [neighborhood](https://github.com/code-dot-org/javabuilder/tree/main/org-code-javabuilder/neighborhood)
  contains a package that can be imported by the user to interact with the neighborhood
  mini-app, a maze-like app created for AP CSA.
* [protocol](https://github.com/code-dot-org/javabuilder/tree/main/org-code-javabuilder/protocol)
  contains the communication protocol that handles messages between Java Lab (client) and
  Javabuilder (server)
* [theater](https://github.com/code-dot-org/javabuilder/tree/main/org-code-javabuilder/theater)
  contains a package that can be imported by the user to interact with the theater 
  mini-app, an image and sound editing app created for AP CSA and used in standalone Java
  Lab projects.

## Local Development
### Setup your project
1. Install a Java IDE - We recommend IntelliJ Community edition.
    1. If given the option when installing, select Java 11 as the default
1. If Java 11 was not installed as part of the IDE, install 
   [OpenJDK 11](https://jdk.java.net/java-se-ri/11)
1. Download the repo and build. Building is a one-time step that seeds the git commit
   hooks.
```
git clone git@github.com:code-dot-org/javabuilder.git
cd javabuilder/org-code-javabuilder
./gradlew build
```
That's it! Now you can open the project in your IDE and start developing. Here are some
other useful commands:

**Launch the local Javabuilder WebSocket server**
```
./gradlew appRun
```
*Note: the Javabuilder server will be ready to use when you see
`Press any key to stop the server.` It will never reach `100% EXECUTING`.*

**Run all tests**
```
./gradlew test
```

**Run the linter and fix lint errors**
```
./gradlew goJF
```

### Developing with Dashboard (most common scenario)
In order to run Java Lab (Code Studio client) with Javabuilder, use the WebSocketServer.
This is a local replacement of AWS API Gateway. 

Instructions
1. Launch the WebSocketServer using the instructions above. (run `./gradlew appRun`)
1. Launch dashboard using the instructions here: 
   https://github.com/code-dot-org/code-dot-org/blob/staging/SETUP.md#overview 
1. Navigate to any Java Lab level, for example: 
   http://localhost-studio.code.org:3000/projects/javalab/new
1. Click the "Run" button

Alternatively, you can test in "headless" mode
1. Install wscat: `npm install -g wscat`
1. Launch the WebSocketServer: `./gradlew appRun`
1. In a terminal, connect to the server with wscat: 
   `wscat -c ws://localhost:8080/javabuilder`

Postman is a good wscat alternative. It is in beta, but has a gui and is more
feature-rich.

### Developing with AWS
For development purposes, you generally shouldn't need to deploy Javabuilder. Some cases
that do need a dev deployment of Javabuilder:
* Editing the API Gateway Route Lambdas.
* Editing the Javabuilder Authorizer Lambda.
* Editing the Javabuilder Cloud Formation template or deploy script.
* Editing the Lambda-specific portions of the build and run Lambda. These are all
  prefixed with `AWS` or `Lambda` in the org-code-javabuilder directory.

**Deploying Javabuilder**  
To deploy a development version of Javabuilder, see the
[Deploying Javabuilder](https://docs.google.com/document/d/1mMQK6HhniLsz9lynzhUcm7Tcw_2WVLBxADe0WzqL6rM/edit#)
instruction doc.

Once you have deployed Javabuilder, you can run a basic connection to it with
```
wscat -c wss://<host-name>/?Authorization=connectivityTest
```
**Developing with deployed Javabuilder**  
If you need to test against a level's Java code, use the following steps:
1. Log in to https://staging-studio.code.org 
1. Navigate to the level or project you'd like to test against
1. Open the network tab on your browser developer tools
1. Click "Run" in Javalab
1. Look for the network request to 
   https://staging-studio.code.org/javabuilder/access_token?projectUrl=...
   in the response, there will be a token. Copy the entire token, without quotes around
   it.
1. Open up Postman. Choose new-> WebSocket Request 
1. Put the url of your javabuilder instance in the server url section (such as 
   wss://javabuilder-myname.dev-code.org)
1. Under Params add the key `Authorization` with the value of the token you copied earlier
1. Under Headers add the key `Origin` with the value https://staging-studio.code.org
1. Click connect

You should now start seeing messages from Javabuilder! Your token will last for 15
minutes. If you make changes to the code, you do not need to re-generate your token.

**Developing with deployed Javabuilder and an adhoc environment**  
1. Deploy javabuilder with the instructions above
1. In the code-dot-org repo, edit the `javabuilder_url` value in 
   [cdo.rb](https://github.com/code-dot-org/code-dot-org/blob/3219e5866689117e086d9891effe0fb39b9ae3f0/lib/cdo.rb#L131)
   to point to your local dev deployment.
1. Deploy the adhoc using the instructions in the 
   [How to Provision an adhoc Environment](https://docs.google.com/document/d/1nWeQEmEQF1B2l93JTQPyeRpLEFzCzY5NdgJ8kgprcDk/edit)
   document.

### Developing in isolation
To run Javabuilder in isolation, run the `LocalMain` class from you Java IDE. This will
exercise the compile and run logic in isolation. Javabuilder will execute the program in
src/main/resources/main.json. You can update which file is used from 
`LocalProjectFileManager`. Input and output will be directed to the terminal. Any 
non-Java file used or created by the program (for example `grid.txt` in 
`main_painter.json`) will be created in the `org-code-javabuilder` folder when running
locally, so be aware you may want to do some cleanup after running code that relies on
text files (in our Lambda runtime the code all runs from a temporary folder and handles
this cleanup).
