package org.code.lambda.javabuilder;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import java.util.List;

// Details: https://examples.javacodegeeks.com/aws-sqs-polling-example-in-java/
// https://docs.aws.amazon.com/code-samples/latest/catalog/javav2-sqs-src-main-java-com-example-sqs-SendMessages.java.html
public class InputPoller {
  private static final String QUEUE_NAME = "JavaBuilderTestQueue.fifo";
  private static final String queueUrl = "https://sqs.us-east-1.amazonaws.com/165336972514/JavaBuilderTestQueue.fifo";
  private OutputHandler outputHandler = null;
  private SqsClient sqsClient = null;

  public InputPoller(){
    this.outputHandler = new OutputHandler();
    this.sqsClient = SqsClient.builder()
      .region(Region.US_EAST_1)
      .build();
  }

  public String poll() {
    int x = 0;
    while (x < 100) {
      x++;
      ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
        .queueUrl(queueUrl)
        .waitTimeSeconds(20)
        .maxNumberOfMessages(1)
        .build();
      List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();
//      sqsClient.deleteMessage(DeleteMessageRequest.builder().receiptHandle(messages).build());

      for (Message m : messages) {
        // TODO: This line isn't workign. Figure this out next.
        sqsClient.deleteMessage(DeleteMessageRequest.builder().receiptHandle(m.receiptHandle()).build());
        return outputHandler.processMessage(m.body());
      }
    }
    return "got nothing";
  }
}
