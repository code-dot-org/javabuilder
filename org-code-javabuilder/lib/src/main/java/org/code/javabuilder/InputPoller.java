package org.code.javabuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;

import java.io.IOException;
import java.util.List;

// Details: https://examples.javacodegeeks.com/aws-sqs-polling-example-in-java/
// https://docs.aws.amazon.com/code-samples/latest/catalog/javav2-sqs-src-main-java-com-example-sqs-SendMessages.java.html
public class InputPoller extends Thread {
  private final InputAdapter inputAdapter;
  private final RuntimeIO runtimeIO;
  private final JavaRunner javaRunner;
  private final OutputAdapter outputAdapter;

  public InputPoller(InputAdapter inputAdapter, RuntimeIO runtimeIO, JavaRunner javaRunner, OutputAdapter outputAdapter) {
    this.inputAdapter = inputAdapter;
    this.runtimeIO = runtimeIO;
    this.javaRunner = javaRunner;
    this.outputAdapter = outputAdapter;
  }

  public void run() {
    while (javaRunner.isAlive()) {
      String message = inputAdapter.getNextMessage();

      try {
        runtimeIO.passInputToProgram(message);
      } catch (IOException e) {
        outputAdapter.sendMessage("There was an error passing input to your program." + e.getStackTrace());
      }
      try {
        Thread.sleep(400);
      } catch (InterruptedException e) {
        outputAdapter.sendMessage("There was an error passing input to your program. Try running it again." + e.toString());
      }
    }
  }
}
