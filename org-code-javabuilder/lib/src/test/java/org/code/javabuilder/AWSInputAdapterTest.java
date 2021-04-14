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
    Message firstMessage = mock(Message.class);
    Message secondMessage = mock(Message.class);
    when(firstMessage.getBody()).thenReturn("hello");
    when(secondMessage.getBody()).thenReturn("world");
    List<Message> messages = new ArrayList<>();
    messages.add(firstMessage);
    messages.add(secondMessage);

    ReceiveMessageResult result = mock(ReceiveMessageResult.class);
    when(result.getMessages()).thenReturn(messages);
    sqsMock = mock(AmazonSQS.class);
    when(sqsMock.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(result);

    inputAdapter = new AWSInputAdapter(sqsMock, "url");
  }

  @Test
  void addsNewlineToMessage() {
    assertEquals(inputAdapter.getNextMessage(), "hello" + System.lineSeparator());
    verify(sqsMock, times(2)).deleteMessage("url", null);
  }

  @Test
  void addsAllReceivedMessagesToQueue() {
    inputAdapter.getNextMessage();
    assertEquals(inputAdapter.getNextMessage(), "world" + System.lineSeparator());
  }

  @Test
  void deletesAllReadMessages() {
    inputAdapter.getNextMessage();
    verify(sqsMock, times(2)).deleteMessage("url", null);
  }

  @Test
  void cachesMessages() {
    inputAdapter.getNextMessage();
    inputAdapter.getNextMessage();
    verify(sqsMock, times(1)).receiveMessage(any(ReceiveMessageRequest.class));
  }
}
