package org.code.javabuilder;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class AWSInputAdapter implements InputAdapter {
  private final AmazonSQS sqsClient;
  private final String queueUrl;
  private final Queue<String> messages;

  public AWSInputAdapter(AmazonSQS sqsClient, String queueUrl) {
    this.sqsClient = sqsClient;
    this.queueUrl = queueUrl;
    this.messages = new LinkedList<>();
  }

  public String getNextMessage() {
    ReceiveMessageRequest request = new ReceiveMessageRequest();
    request.setQueueUrl(queueUrl);
    request.setWaitTimeSeconds(20);
    request.setMaxNumberOfMessages(10);
    while (messages.peek() == null) {
      List<Message> messages = sqsClient.receiveMessage(request).getMessages();
      for (Message message : messages) {
        this.messages.add(message.getBody() + System.lineSeparator());
        sqsClient.deleteMessage(queueUrl, message.getReceiptHandle());
      }
    }

    return messages.remove();
  }
}
