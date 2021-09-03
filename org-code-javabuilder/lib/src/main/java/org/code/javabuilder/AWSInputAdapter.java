package org.code.javabuilder;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.code.protocol.InputAdapter;

/** Accesses Amazon SQS to get user input for the currently running program. */
public class AWSInputAdapter implements InputAdapter {
  private final AmazonSQS sqsClient;
  private final String queueUrl;
  private final Queue<String> messages;

  public AWSInputAdapter(AmazonSQS sqsClient, String queueUrl) {
    this.sqsClient = sqsClient;
    this.queueUrl = queueUrl;
    this.messages = new LinkedList<>();
  }

  /**
   * Attempts to access the first element in the fifo queue. If none exist, queries available input
   * from Amazon SQS and stores each input in the `messages` fifo queue. Messages are deleted after
   * they are retrieved. This is a blocking call.
   *
   * @return the first message in the fifo queue.
   */
  public String getNextMessage() {
    ReceiveMessageRequest request = new ReceiveMessageRequest();
    request.setQueueUrl(queueUrl);
    // Sets the request timeout to its maximum value of 20 seconds
    request.setWaitTimeSeconds(20);
    // Sets the the number of messages to retrieve to its maximum value of 10
    request.setMaxNumberOfMessages(10);
    while (messages.peek() == null) {
      List<Message> messages = sqsClient.receiveMessage(request).getMessages();
      for (Message message : messages) {
        this.messages.add(message.getBody());
        sqsClient.deleteMessage(queueUrl, message.getReceiptHandle());
      }
    }

    return messages.remove();
  }
}
