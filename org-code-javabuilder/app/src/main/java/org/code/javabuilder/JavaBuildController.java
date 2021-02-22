package org.code.javabuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.security.Principal;
import org.apache.commons.io.FilenameUtils;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

/**
 * Accepts requests to the /execute channel to compile and run user code. Directs output from the
 * user's program to the user on the /topic/output channel.
 */
@Controller
public class JavaBuildController {

  private final CompileRunService compileRunService;

  JavaBuildController(CompileRunService compileRunService) {
    this.compileRunService = compileRunService;
  }

  /** Executes the user code and sends the output of that code across the established websocket. */
  @MessageMapping(Destinations.EXECUTE_CODE)
  @SendToUser(Destinations.PTP_PREFIX + Destinations.OUTPUT_CHANNEL)
  public UserProgramOutput execute(UserProgram userProgram, Principal principal) throws Exception {
    // TODO: CSA-48 Handle more than one file
    // if (userProject.size() > 1) {
    //   return "Error: Can only process one file at a time.";
    // }

    File compileRunScript = null;
    String tempDir = System.getProperty("java.io.tmpdir");

    String filename = userProgram.getFileName();
    String className = FilenameUtils.removeExtension(filename);
    String userCode = userProgram.getCode();
    try {
      // Build user's java file
      File userFile = new File(Paths.get(tempDir, filename).toString());
      FileWriter writer = new FileWriter(userFile);
      writer.write(userCode);
      writer.close();

      // Create the compile & run script
      compileRunScript = File.createTempFile("script", null);
      FileWriter scriptWriter = new FileWriter(compileRunScript);
      scriptWriter.write("javac " + userFile.getAbsolutePath());
      scriptWriter.write(System.getProperty("line.separator"));
      scriptWriter.write("java -cp " + tempDir + " " + className);
      scriptWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
      // TODO: CSA-42 Improve error handling
      return new UserProgramOutput("An error occurred creating files.");
    }

    ProcessBuilder pb = new ProcessBuilder().command("bash", compileRunScript.toString());

    Process process = null;
    try {
      // Execute the user's code
      // TODO: CSA-42 Handle infinite loops, malicious code, etc.
      process = pb.start();
      process.waitFor();
    } catch (InterruptedException e) {
      e.printStackTrace();
      return new UserProgramOutput("An error occurred running the program.");
    } catch (IOException e) {
      e.printStackTrace();
      return new UserProgramOutput("An error occurred running the program.");
    }

    StringBuilder programOutput = new StringBuilder();
    try {
      // Get output from the user's program
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line = null;
      while ((line = reader.readLine()) != null) {
        programOutput.append(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
      return new UserProgramOutput("An error occurred reading the program output.");
    }

    // return new UserProgramOutput("> " + programOutput.toString());
    compileRunService.sendMessages(principal.getName(), programOutput.toString());
    // Haha this line gets sent second.
    return new UserProgramOutput("> Compiling and running your code...");
  }
}
