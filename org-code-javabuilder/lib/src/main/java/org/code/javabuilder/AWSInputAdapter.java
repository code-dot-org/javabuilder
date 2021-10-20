package org.code.javabuilder;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.code.protocol.InputAdapter;

/** Accesses Amazon SQS to get user input for the currently running program. */
public class AWSInputAdapter implements InputAdapter {
  private final AmazonSQS sqsClient;
  private final String queueUrl;
  private final String queueName;
  private final Queue<String> messages;
  private boolean hasActiveConnection;

  public AWSInputAdapter(AmazonSQS sqsClient, String queueUrl, String queueName) {
    this.sqsClient = sqsClient;
    this.queueUrl = queueUrl;
    this.messages = new LinkedList<>();
    this.hasActiveConnection = true;
    this.queueName = queueName;
  }

  /**
   * Attempts to access the first element in the fifo queue. If none exist, queries available input
   * from Amazon SQS and stores each input in the `messages` fifo queue. Messages are deleted after
   * they are retrieved. This is a blocking call.
   *
   * @return the first message in the fifo queue.
   */
  public String getNextMessage() {
    if (!this.hasActiveConnection) {
      return null;
    }
    ReceiveMessageRequest request = new ReceiveMessageRequest();
    request.setQueueUrl(queueUrl);
    // Sets the request timeout to its maximum value of 20 seconds
    request.setWaitTimeSeconds(20);
    // Sets the the number of messages to retrieve to its maximum value of 10
    request.setMaxNumberOfMessages(10);
    while (messages.peek() == null) {
      try {
        List<Message> messages = sqsClient.receiveMessage(request).getMessages();
        for (Message message : messages) {
          this.messages.add(message.getBody());
          sqsClient.deleteMessage(queueUrl, message.getReceiptHandle());
        }
      } catch (QueueDoesNotExistException e) {
        // if we tried to send a message and got queue does not exist, we have lost our connection
        this.hasActiveConnection = false;
        return null;
      }
    }

    return messages.remove();
  }

  /** Check if we still have an active connection to AWS. */
  @Override
  public boolean hasActiveConnection() {
    if (!this.hasActiveConnection) {
      return false;
    }
    try {
      // The simplest way to determine if we have an active connection is to make a queue
      // url request.
      GetQueueUrlRequest queueUrlRequest = new GetQueueUrlRequest(this.queueName);
      sqsClient.getQueueUrl(queueUrlRequest);
    } catch (QueueDoesNotExistException e) {
      this.hasActiveConnection = false;
    }
    return this.hasActiveConnection;
  }
}
