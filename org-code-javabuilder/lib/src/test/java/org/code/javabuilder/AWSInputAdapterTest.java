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
  void addsNewlineToMessage() {
    messageSetUp(new String[] {"hello"});
    assertEquals(inputAdapter.getNextMessage(), "hello" + System.lineSeparator());
  }

  @Test
  void addsAllReceivedMessagesToQueue() {
    messageSetUp(new String[] {"", "world"});
    inputAdapter.getNextMessage();
    assertEquals(inputAdapter.getNextMessage(), "world" + System.lineSeparator());
  }

  @Test
  void deletesAllReadMessages() {
    messageSetUp(new String[] {"", ""});
    inputAdapter.getNextMessage();
    verify(sqsMock, times(2)).deleteMessage("url", null);
  }

  @Test
  void cachesMessages() {
    messageSetUp(new String[] {"", ""});
    inputAdapter.getNextMessage();
    inputAdapter.getNextMessage();
    verify(sqsMock, times(1)).receiveMessage(any(ReceiveMessageRequest.class));
  }
}
