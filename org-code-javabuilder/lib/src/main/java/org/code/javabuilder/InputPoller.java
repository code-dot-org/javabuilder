package org.code.javabuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;

import java.io.IOException;
import java.util.List;

// Details: https://examples.javacodegeeks.com/aws-sqs-polling-example-in-java/
// https://docs.aws.amazon.com/code-samples/latest/catalog/javav2-sqs-src-main-java-com-example-sqs-SendMessages.java.html
public class InputPoller extends Thread {
  private final String queueUrl;
  private final AmazonSQS sqsClient;
  private final RuntimeIO runtimeIO;
  private final JavaRunner javaRunner;
  private final OutputHandler outputHandler;

  public InputPoller(AmazonSQS sqsClient, String queueUrl, RuntimeIO runtimeIO, JavaRunner javaRunner, OutputHandler outputHandler) {
    this.queueUrl = queueUrl;
    this.sqsClient = sqsClient;
    this.runtimeIO = runtimeIO;
    this.javaRunner = javaRunner;
    this.outputHandler = outputHandler;
  }

  public void run() {
    while (javaRunner.isAlive()) {
      List<Message> messages = sqsClient.receiveMessage(queueUrl).getMessages();

      for (Message message : messages) {
        try {
          runtimeIO.passInputToProgram(message.getBody());
        } catch (IOException e) {
          outputHandler.sendMessage("There was an error passing input to your program." + e.getStackTrace());
        }
        sqsClient.deleteMessage(queueUrl, message.getReceiptHandle());
      }
      try {
        Thread.sleep(400);
      } catch (InterruptedException e) {
        outputHandler.sendMessage("There was an error passing input to your program. Try running it again." + e.toString());
      }
    }
  }
}
