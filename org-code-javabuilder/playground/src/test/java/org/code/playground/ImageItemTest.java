package org.code.playground;

import static org.junit.jupiter.api.Assertions.*;
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
  private ContentManager contentManager;

  @BeforeEach
  public void setUp() {
    playgroundMessageHandler = mock(PlaygroundMessageHandler.class);
    messageCaptor = ArgumentCaptor.forClass(PlaygroundMessage.class);
    GlobalProtocolTestFactory.builder().create();
    messageHandlerMockedStatic = mockStatic(PlaygroundMessageHandler.class);
    messageHandlerMockedStatic
        .when(PlaygroundMessageHandler::getInstance)
        .thenReturn(playgroundMessageHandler);
    contentManager = mock(ContentManager.class);
  }

  @AfterEach
  public void tearDown() {
    messageHandlerMockedStatic.close();
  }

  @Test
  public void settersSendChangeMessagesIfTheyAreOn() throws FileNotFoundException {
    ImageItem imageItem = new ImageItem("test", 0, 0, 10, 10, contentManager);
    imageItem.turnOnChangeMessages();

    String newFilename = "new_filename";
    int newWidth = 100;
    imageItem.setFilename(newFilename);
    imageItem.setWidth(newWidth);

    verify(playgroundMessageHandler, times(2)).sendMessage(messageCaptor.capture());
    List<PlaygroundMessage> messages = messageCaptor.getAllValues();
    assertEquals(PlaygroundSignalKey.CHANGE_ITEM.toString(), messages.get(0).getValue());
    assertEquals(newFilename, messages.get(0).getDetail().get(ClientMessageDetailKeys.FILENAME));
    assertEquals(
        Integer.toString(newWidth), messages.get(1).getDetail().get(ClientMessageDetailKeys.WIDTH));
  }

  @Test
  public void settersDoNotSendChangeMessagesByDefault() throws FileNotFoundException {
    ImageItem imageItem = new ImageItem("test", 0, 0, 10, 10, contentManager);
    String newFilename = "new_filename";
    int newWidth = 100;

    // change messages are off by default, no messages should be sent but values should change
    imageItem.setFilename(newFilename);
    imageItem.setWidth(newWidth);

    verify(playgroundMessageHandler, times(0)).sendMessage(messageCaptor.capture());
    assertEquals(newWidth, imageItem.getWidth());
    assertEquals(newFilename, imageItem.getFilename());
  }

  @Test
  public void testConstructorThrowsExceptionIfFileNotFound() throws FileNotFoundException {
    final FileNotFoundException expected = new FileNotFoundException();
    doThrow(expected).when(contentManager).verifyAssetFilename(anyString());

    final FileNotFoundException actual =
        assertThrows(
            FileNotFoundException.class, () -> new ImageItem("test", 0, 0, 10, 10, contentManager));

    assertSame(expected, actual);
  }

  @Test
  public void testSetFilenameThrowsExceptionIfFileNotFound() throws FileNotFoundException {
    ImageItem imageItem = new ImageItem("test", 0, 0, 10, 10, contentManager);

    final FileNotFoundException expected = new FileNotFoundException();
    doThrow(expected).when(contentManager).verifyAssetFilename(anyString());

    final FileNotFoundException actual =
        assertThrows(FileNotFoundException.class, () -> imageItem.setFilename("otherFile"));

    assertSame(expected, actual);
  }
}
