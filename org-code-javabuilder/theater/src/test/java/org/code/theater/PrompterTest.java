package org.code.theater;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.code.protocol.GlobalProtocol;
import org.code.protocol.OutputAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class PrompterTest {
  private OutputAdapter outputAdapter;
  private MockedStatic<GlobalProtocol> globalProtocol;
  private Prompter unitUnderTest;

  @BeforeEach
  public void setUp() {
    outputAdapter = mock(OutputAdapter.class);

    globalProtocol = mockStatic(GlobalProtocol.class);
    final GlobalProtocol globalProtocolInstance = mock(GlobalProtocol.class);
    globalProtocol.when(GlobalProtocol::getInstance).thenReturn(globalProtocolInstance);
    when(globalProtocolInstance.getOutputAdapter()).thenReturn(outputAdapter);

    unitUnderTest = new Prompter();
  }

  @AfterEach
  public void tearDown() {
    globalProtocol.close();
  }

  @Test
  public void testGetImageSendsMessage() {
    String prompt = "Upload an image please!";
    ArgumentCaptor<TheaterMessage> message = ArgumentCaptor.forClass(TheaterMessage.class);

    unitUnderTest.getImage(prompt);

    verify(outputAdapter, times(1)).sendMessage(message.capture());
    assertEquals(TheaterSignalKey.GET_IMAGE.toString(), message.getValue().getValue());
    assertEquals(prompt, message.getValue().getDetail().get("prompt"));
  }
}
