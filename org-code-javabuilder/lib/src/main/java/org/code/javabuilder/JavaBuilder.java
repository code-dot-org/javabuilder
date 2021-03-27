package org.code.javabuilder;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApiClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import java.io.IOException;

public class JavaBuilder {
  private final OutputHandler outputHandler;
  private final InputPoller inputPoller;
  private final OutputPoller outputPoller;
  private final JavaRunner javaRunner;
  private final OutputSemaphore outputSemaphore;

  public JavaBuilder(String connectionId, String apiEndpoint, String queueUrl) {
    // Create output handler
    AmazonApiGatewayManagementApi api =
        AmazonApiGatewayManagementApiClientBuilder.standard()
            .withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(apiEndpoint, "us-east-1"))
            .build();
    this.outputHandler = new OutputHandler(connectionId, api);
    this.outputSemaphore = new OutputSemaphore();

    // Overwrite system I/O
    RuntimeIO runtimeIO;
    try {
      runtimeIO = new RuntimeIO(this.outputSemaphore);
    } catch (IOException e) {
      this.outputHandler.sendMessage("There was an error running your code. Try again.");
      throw new RuntimeException("Error setting up console IO", e);
    }

    // Create code runner
    this.javaRunner = new JavaRunner(this.outputSemaphore);

    // Create input poller
    AmazonSQS sqsClient = AmazonSQSClientBuilder.defaultClient();
    this.inputPoller =
        new InputPoller(sqsClient, queueUrl, runtimeIO, this.javaRunner, this.outputHandler);

    // Create output poller
    this.outputPoller =
        new OutputPoller(this.javaRunner, this.outputHandler, runtimeIO, this.outputSemaphore);
  }

  public void runUserCode() {
    this.javaRunner.start();
    this.inputPoller.start();
    this.outputPoller.start();
    while (javaRunner.isAlive()) {
      try {
        Thread.sleep(400);
      } catch (InterruptedException e) {
        outputHandler.sendMessage(
            "There was an error running to your program. Try running it again." + e.toString());
      }
    }

    while (outputSemaphore.anyOutputInProgress()) {
      try {
        Thread.sleep(400);
      } catch (InterruptedException e) {
        outputHandler.sendMessage(
            "There was an error running to your program. Try running it again." + e.toString());
      }
    }
  }
}
