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
* [playground](https://github.com/code-dot-org/javabuilder/tree/main/org-code-javabuilder/playground)
  is a prototype built to explore the idea of click-interactivity in Javabuilder and AP
  CSA. It contains a package that can be imported by the user to interact with the playground
  mini-app. Playground is primarily used for board game style programs.
* [studentlib](https://github.com/code-dot-org/javabuilder/tree/main/org-code-javabuilder/studentlib)
  houses all external dependencies required to run and compile student code that are not already
  included by other mini-app packages.

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
That's it! Now you can open the project in your IDE and start developing. If you are
using IntelliJ, import the project as a gradle project: File -> New -> Project from
Existing Sources...

It is not required, but you can also install gradle. If you do, you can use gradle
directly rather than from the gradlew file. Instead of running `./gradlew build`, for
example, you would run `gradle build`.

Here are some other useful commands:

**Launch the local Javabuilder WebSocket server**
```
./gradlew appRun
```
*Note: the Javabuilder server will be ready to use after a few seconds when you see
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

_Note: non-Java files used or created by the program (for example `grid.txt` in the
Neighborhood) will be added to the root of the repo. In production, the runtime directory
is changed to the tmp directory, so cleanup of these files is not necessary there._

### Developing with AWS
For development purposes, you generally shouldn't need a dev deploy of Javabuilder. See
the 
[repo-level README](https://github.com/code-dot-org/javabuilder/blob/main/README.md#dev-deploy-of-javabuilder)
for instructions on when a dev deploy of Javabuilder is required and how to carry out
such a deployment.

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
