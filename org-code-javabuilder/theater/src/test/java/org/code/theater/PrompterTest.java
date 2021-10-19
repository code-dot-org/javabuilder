package org.code.theater;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.code.protocol.ClientMessageDetailKeys;
import org.code.protocol.OutputAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class PrompterTest {
  private OutputAdapter outputAdapter;
  private Prompter unitUnderTest;

  @BeforeEach
  public void setUp() {
    outputAdapter = mock(OutputAdapter.class);
    unitUnderTest = new Prompter(outputAdapter);
  }

  @Test
  public void testGetImageSendsMessage() {
    String prompt = "Upload an image please!";
    ArgumentCaptor<TheaterMessage> message = ArgumentCaptor.forClass(TheaterMessage.class);

    unitUnderTest.getImage(prompt);

    verify(outputAdapter, times(1)).sendMessage(message.capture());
    assertEquals(TheaterSignalKey.GET_IMAGE.toString(), message.getValue().getValue());
    assertEquals(prompt, message.getValue().getDetail().get(ClientMessageDetailKeys.PROMPT));
  }
}
