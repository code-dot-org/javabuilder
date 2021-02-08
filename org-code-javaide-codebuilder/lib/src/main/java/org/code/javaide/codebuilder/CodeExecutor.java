package org.code.javaide.codebuilder;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

/**
 * This is the AWS Lambda handler
 * Details: https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html
 * It compiles and runs the input Java code and returns the output of that code's execution
 */
public class CodeExecutor implements RequestHandler<Map<String,String>, String>{
  Gson gson = new GsonBuilder().setPrettyPrinting().create();

  /**
  * @param userProject A mapping of filenames in a user's project to the code in those files
  * @param __          Unused. The environment context where the handler is executed
  * @return            The output from the user's code execution
  */
  @Override
  public String handleRequest(Map<String,String> userProject, Context __)
  {
    // TODO: CSA-48 Handle more than one file
    if(userProject.size() > 1) {
      return "Error: Can only process one file at a time.";
    }

    File compileRunScript = null;
    String tempDir = System.getProperty("java.io.tmpdir");
    for(Map.Entry<String, String> projectFile : userProject.entrySet()) {
      String className = FilenameUtils.removeExtension(projectFile.getKey());
      String userCode = projectFile.getValue();
      try {
        // Build user's java file
        File userFile = new File(Paths.get(tempDir, projectFile.getKey()).toString());
        FileWriter writer = new FileWriter(userFile);
        writer.write(userCode);
        writer.close();

        // Create the compile & run script
        compileRunScript = File.createTempFile("script", null);
        FileWriter scriptWriter = new FileWriter(compileRunScript);
        scriptWriter.write("javac " + userFile.getAbsolutePath());
        scriptWriter.write(System.getProperty( "line.separator" ));
        scriptWriter.write("java -cp " + tempDir + " " + className);
        scriptWriter.close();
      } catch (IOException e) {
        e.printStackTrace();
        // TODO: CSA-42 Improve error handling
        return "An error occurred creating files.";
      }
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
      return "An error occurred running the program.";
    } catch (IOException e) {
      e.printStackTrace();
      return "An error occurred running the program.";
    }

    StringBuilder programOutput = new StringBuilder();
    try {
      // Get output from the user's program
      BufferedReader reader =
          new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line = null;
      while ( (line = reader.readLine()) != null) {
         programOutput.append(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
      return "An error occurred reading the program output.";
    }

    return programOutput.toString();
  }
}
