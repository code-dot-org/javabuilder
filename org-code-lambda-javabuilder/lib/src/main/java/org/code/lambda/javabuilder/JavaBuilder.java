package org.code.lambda.javabuilder;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApiClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.regions.Regions;

import java.io.IOException;
import java.util.Scanner;

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

    // Overwrite system I/O
    try {
      this.runtimeIO = new RuntimeIO();
    } catch (IOException e) {
      this.outputHandler.sendMessage("There was an error running your code. Try again.");
      throw new RuntimeException("Error setting up console IO", e);
    }

    // Create input poller
    final AmazonSQS sqsClient = AmazonSQSClientBuilder.defaultClient();
    this.inputPoller = new InputPoller(sqsClient, queueUrl, this.runtimeIO, this.outputHandler);

    // Create output poller
    this.outputPoller = new OutputPoller(this.outputHandler);

    // Create code runner
    this.javaRunner = new JavaRunner();

    this.outputSemaphore = new OutputSemaphore();

    this.inputPoller.start();
    this.outputPoller.start();
  }

  public void runUserCode() {
    this.javaRunner.start();
    while(javaRunner.isAlive()) {
      try {
        Thread.sleep(200);
      } catch (InterruptedException e) {
        outputHandler.sendMessage("There was an error running to your program. Try running it again." + e.toString());
      }
    }

    OutputSemaphore.signalProcessFinalOutput();
    while (OutputSemaphore.anyOutputInProgress()) {
      try {
        Thread.sleep(200);
      } catch (InterruptedException e) {
        outputHandler.sendMessage("There was an error running to your program. Try running it again." + e.toString());
      }
    }
  }
}
