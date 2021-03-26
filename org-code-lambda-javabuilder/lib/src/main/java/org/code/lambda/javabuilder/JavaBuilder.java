package org.code.lambda.javabuilder;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApiClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

import java.io.IOException;

public class JavaBuilder {
  private final OutputHandler outputHandler;
  private final InputPoller inputPoller;
  private final RuntimeIO runtimeIO;
  private final OutputPoller outputPoller;
  private final JavaRunner javaRunner;
  private final OutputSemaphore outputSemaphore;

  public JavaBuilder(String connectionId, String apiEndpoint, String queueUrl) {
    // Create output handler
    AmazonApiGatewayManagementApi api = AmazonApiGatewayManagementApiClientBuilder.standard().withEndpointConfiguration(
        new AwsClientBuilder.EndpointConfiguration(apiEndpoint,  "us-east-1")
    ).build();
    this.outputHandler = new OutputHandler(connectionId, api);

    this.outputHandler.sendDebuggingMessage("Done setting up output handler");

    // Overwrite system I/O
    try {
      this.runtimeIO = new RuntimeIO();
    } catch (IOException e) {
      this.outputHandler.sendMessage("There was an error running your code. Try again.");
      throw new RuntimeException("Error setting up console IO", e);
    }

    this.outputHandler.sendDebuggingMessage("Done setting up runtime IO handler");

    // Create code runner
    this.javaRunner = new JavaRunner();

    // Create input poller
    final AmazonSQS sqsClient = AmazonSQSClientBuilder.defaultClient();
    this.inputPoller = new InputPoller(sqsClient, queueUrl, this.runtimeIO, this.javaRunner);

    // Create output poller
    this.outputPoller = new OutputPoller(this.javaRunner);

    this.outputSemaphore = new OutputSemaphore();
  }

  public void runUserCode() {
    this.javaRunner.start();
    this.outputHandler.sendDebuggingMessage("Started Running Code");
    this.inputPoller.start();
    this.outputHandler.sendDebuggingMessage("Started Input Poller");
    this.outputPoller.start();
    this.outputHandler.sendDebuggingMessage("Started Output Poller");
    while(javaRunner.isAlive()) {
      try {
        Thread.sleep(400);
      } catch (InterruptedException e) {
        outputHandler.sendMessage("There was an error running to your program. Try running it again." + e.toString());
      }
    }

    this.outputHandler.sendDebuggingMessage("Waiting for output");
    while (OutputSemaphore.anyOutputInProgress()) {
      try {
        Thread.sleep(400);
      } catch (InterruptedException e) {
        outputHandler.sendMessage("There was an error running to your program. Try running it again." + e.toString());
      }
    }
    this.outputHandler.sendDebuggingMessage("Done");
  }
}
