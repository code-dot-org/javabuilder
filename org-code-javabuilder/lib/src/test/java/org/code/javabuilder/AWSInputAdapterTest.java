package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AWSInputAdapterTest {
  private AWSInputAdapter inputAdapter;
  private AmazonSQS sqsMock;

  @BeforeEach
  public void setUp() {
    sqsMock = mock(AmazonSQS.class);
    inputAdapter = new AWSInputAdapter(sqsMock, "url");
  }

  /**
   * Adds messages to the SQS Queue Mock to be retrieved by the input Adapter
   *
   * @param messages an array of messages in the queue
   */
  private void messageSetUp(String[] messages) {
    List<Message> messageList = new ArrayList<>();
    for (String s : messages) {
      Message message = mock(Message.class);
      when(message.getBody()).thenReturn(s);
      messageList.add(message);
    }

    ReceiveMessageResult result = mock(ReceiveMessageResult.class);
    when(result.getMessages()).thenReturn(messageList);
    when(sqsMock.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(result);
  }

  @Test
  void getsNextMessage() {
    messageSetUp(new String[] {"hello"});
    assertEquals(inputAdapter.getNextMessage(), "hello");
  }

  @Test
  void addsAllReceivedMessagesToQueue() {
    messageSetUp(new String[] {"", "world"});
    inputAdapter.getNextMessage();
    assertEquals(inputAdapter.getNextMessage(), "world");
  }

  /**
   * When getNextMessage is called on an empty inputAdapter, it fetches and caches all available
   * messages from the SQS Queue (up to the AWS maximum of 10). To ensure these messages are not
   * retrieved again, we need to delete them from the SQS Queue. This test checks that we are
   * cleaning up the queue after retrieving messages
   */
  @Test
  void deletesAllReadMessages() {
    messageSetUp(new String[] {"", ""});
    inputAdapter.getNextMessage();
    verify(sqsMock, times(2)).deleteMessage("url", null);
  }

  /**
   * Retrieving messages from the SQS Queue is an expensive operation. When we have no more messages
   * available in the inputAdapter, we retrieve multiple messages from the Queue at once. They are
   * cached and subsequent calls to getNextMessage do not retrieve additional messages until the
   * local queue is empty.
   */
  @Test
  void cachesMessages() {
    messageSetUp(new String[] {"", ""});
    inputAdapter.getNextMessage();
    inputAdapter.getNextMessage();
    verify(sqsMock, times(1)).receiveMessage(any(ReceiveMessageRequest.class));
  }
}
