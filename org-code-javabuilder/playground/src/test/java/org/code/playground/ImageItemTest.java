package org.code.playground;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.io.FileNotFoundException;
import java.util.List;
import org.code.protocol.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class ImageItemTest {
  private PlaygroundMessageHandler playgroundMessageHandler;
  private ArgumentCaptor<PlaygroundMessage> messageCaptor;
  private MockedStatic<PlaygroundMessageHandler> messageHandlerMockedStatic;

  @BeforeEach
  public void setUp() {
    playgroundMessageHandler = mock(PlaygroundMessageHandler.class);
    messageCaptor = ArgumentCaptor.forClass(PlaygroundMessage.class);
    GlobalProtocol.create(
        mock(OutputAdapter.class),
        mock(InputAdapter.class),
        "",
        "",
        "",
        mock(JavabuilderFileWriter.class));
    messageHandlerMockedStatic = mockStatic(PlaygroundMessageHandler.class);
    messageHandlerMockedStatic
        .when(PlaygroundMessageHandler::getInstance)
        .thenReturn(playgroundMessageHandler);
  }

  @AfterEach
  public void tearDown() {
    messageHandlerMockedStatic.close();
  }

  @Test
  public void settersSendChangeMessages() throws FileNotFoundException {
    ImageItem imageItem = new ImageItem("test", 0, 0, 10, 10);
    imageItem.setFilename("new_filename");
    imageItem.setWidth(100);

    verify(playgroundMessageHandler, times(2)).sendMessage(messageCaptor.capture());
    List<PlaygroundMessage> messages = messageCaptor.getAllValues();
    assertEquals(PlaygroundSignalKey.CHANGE_ITEM.toString(), messages.get(0).getValue());
    assertEquals("new_filename", messages.get(0).getDetail().get("filename"));
    assertEquals("100", messages.get(1).getDetail().get("width"));
  }
}
