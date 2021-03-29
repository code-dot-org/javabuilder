package org.code.javabuilder;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class AWSInputAdapter implements InputAdapter {
  private AmazonSQS sqsClient;
  private String queueUrl;
  private Queue<String> messages;

  public AWSInputAdapter(AmazonSQS sqsClient, String queueUrl) {
    this.sqsClient = sqsClient;
    this.queueUrl = queueUrl;
    this.messages = new LinkedList<>();
  }

  public String getNextMessage() {
    while (messages.peek() == null) {
      List<Message> messages = sqsClient.receiveMessage(queueUrl).getMessages();
      for (Message message : messages) {
        this.messages.add(message.getBody());
        sqsClient.deleteMessage(queueUrl, message.getReceiptHandle());
      }
    }

    return messages.remove();
  }
}
