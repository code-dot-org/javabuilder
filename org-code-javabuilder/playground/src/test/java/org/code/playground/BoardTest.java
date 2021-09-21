package org.code.playground;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.io.FileNotFoundException;
import java.util.List;
import org.code.media.Color;
import org.code.media.Font;
import org.code.media.FontStyle;
import org.code.protocol.InputHandler;
import org.code.protocol.InputMessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class BoardTest {

  private PlaygroundMessageHandler playgroundMessageHandler;
  private InputHandler inputHandler;
  private ArgumentCaptor<PlaygroundMessage> messageCaptor;
  private Board unitUnderTest;

  @BeforeEach
  public void setUp() {
    playgroundMessageHandler = mock(PlaygroundMessageHandler.class);
    messageCaptor = ArgumentCaptor.forClass(PlaygroundMessage.class);
    inputHandler = mock(InputHandler.class);
    unitUnderTest = new Board(playgroundMessageHandler, inputHandler);
  }

  @Test
  public void testGetWidthReturnsDefaultWidth() {
    assertEquals(400, unitUnderTest.getWidth());
  }

  @Test
  public void testGetHeightReturnsDefaultHeight() {
    assertEquals(400, unitUnderTest.getHeight());
  }

  @Test
  public void testPlaySoundSendsMessage() throws FileNotFoundException {
    String filename = "test_file.wav";

    unitUnderTest.playSound(filename);
    verify(playgroundMessageHandler).sendMessage(messageCaptor.capture());
    assertEquals(
        PlaygroundSignalKey.PLAY_SOUND.toString(), messageCaptor.getAllValues().get(0).getValue());
    assertEquals(filename, messageCaptor.getAllValues().get(0).getDetail().get("filename"));
  }

  @Test
  public void testRunSendsMessageAndWaitsForInput() throws PlaygroundException {
    // Need to make sure exit() is called so run() terminates
    when(inputHandler.getNextMessageForType(InputMessageType.PLAYGROUND))
        .thenAnswer(
            invocation -> {
              unitUnderTest.exit();
              return "id";
            });

    unitUnderTest.run();

    verify(playgroundMessageHandler, times(2)).sendMessage(messageCaptor.capture());
    assertEquals(
        PlaygroundSignalKey.RUN.toString(), messageCaptor.getAllValues().get(0).getValue());
    verify(inputHandler).getNextMessageForType(InputMessageType.PLAYGROUND);
  }

  @Test
  public void testRunThrowsExceptionIfCalledTwice() throws PlaygroundException {
    // Need to make sure exit() is called so run() terminates
    when(inputHandler.getNextMessageForType(InputMessageType.PLAYGROUND))
        .thenAnswer(
            invocation -> {
              unitUnderTest.exit();
              return "id";
            });

    unitUnderTest.run();
    final PlaygroundException e =
        assertThrows(PlaygroundException.class, () -> unitUnderTest.run());
    assertEquals(PlaygroundExceptionKeys.PLAYGROUND_RUNNING.toString(), e.getMessage());
  }

  @Test
  public void testExitSendsExitMessage() throws PlaygroundException {
    // Ensure that exit() is called while running to avoid exception
    when(inputHandler.getNextMessageForType(InputMessageType.PLAYGROUND))
        .thenAnswer(
            invocation -> {
              unitUnderTest.exit();
              return "id";
            });
    unitUnderTest.run();

    verify(playgroundMessageHandler, times(2)).sendMessage(messageCaptor.capture());
    assertEquals(
        PlaygroundSignalKey.EXIT.toString(), messageCaptor.getAllValues().get(1).getValue());
  }

  @Test
  public void testExitWithSoundSendsPlaySoundAndExitMessages()
      throws PlaygroundException, FileNotFoundException {
    String filename = "test_file.wav";

    // Ensure that exit() is called while running to avoid exception
    when(inputHandler.getNextMessageForType(InputMessageType.PLAYGROUND))
        .thenAnswer(
            invocation -> {
              unitUnderTest.exit(filename);
              return "id";
            });
    unitUnderTest.run();

    verify(playgroundMessageHandler, times(3)).sendMessage(messageCaptor.capture());
    assertEquals(
        PlaygroundSignalKey.PLAY_SOUND.toString(), messageCaptor.getAllValues().get(1).getValue());
    assertEquals(filename, messageCaptor.getAllValues().get(1).getDetail().get("filename"));
    assertEquals(
        PlaygroundSignalKey.EXIT.toString(), messageCaptor.getAllValues().get(2).getValue());
  }

  @Test
  public void testExitThrowsExceptionIfNotRunning() {
    final PlaygroundException e =
        assertThrows(PlaygroundException.class, () -> unitUnderTest.exit());
    assertEquals(PlaygroundExceptionKeys.PLAYGROUND_NOT_RUNNING.toString(), e.getMessage());
  }

  @Test
  public void testAddClickableImageSendsMessage() throws FileNotFoundException {
    ClickableImage testImage = new ClickableImageHelper("test", 0, 0, 10, 10);
    unitUnderTest.addClickableImage(testImage);

    verify(playgroundMessageHandler, times(1)).sendMessage(messageCaptor.capture());
    PlaygroundMessage message = messageCaptor.getAllValues().get(0);
    assertEquals(PlaygroundSignalKey.ADD_CLICKABLE_ITEM.toString(), message.getValue());
  }

  @Test
  public void testAddClickableImageDoesNotAddDuplicate() throws FileNotFoundException {
    ClickableImage testImage = new ClickableImageHelper("test", 0, 0, 10, 10);
    unitUnderTest.addClickableImage(testImage);
    unitUnderTest.addClickableImage(testImage);

    verify(playgroundMessageHandler, times(1)).sendMessage(messageCaptor.capture());
    List<PlaygroundMessage> messages = messageCaptor.getAllValues();
    assertEquals(PlaygroundSignalKey.ADD_CLICKABLE_ITEM.toString(), messages.get(0).getValue());
  }

  @Test
  public void testAddItemIncrementsIndex() throws FileNotFoundException {
    ClickableImage testClickableImage = new ClickableImageHelper("test", 0, 0, 10, 10);
    ImageItem testImage = new ImageItem("test", 0, 0, 10, 15);
    unitUnderTest.addClickableImage(testClickableImage);
    unitUnderTest.addImageItem(testImage);

    verify(playgroundMessageHandler, times(2)).sendMessage(messageCaptor.capture());
    List<PlaygroundMessage> messages = messageCaptor.getAllValues();
    assertEquals("0", messages.get(0).getDetail().get("index"));
    assertEquals("1", messages.get(1).getDetail().get("index"));
  }

  @Test
  public void testRemoveItemDoesNotRemoveDuplicate() {
    TextItem testTextItem =
        new TextItem("text", 0, 0, Color.AQUA, Font.SANS, FontStyle.BOLD, 10, 0);
    unitUnderTest.addTextItem(testTextItem);
    unitUnderTest.removeItem(testTextItem);
    unitUnderTest.removeItem(testTextItem);

    // should have sent one add and one remove message
    verify(playgroundMessageHandler, times(2)).sendMessage(messageCaptor.capture());
    List<PlaygroundMessage> messages = messageCaptor.getAllValues();
    assertEquals(PlaygroundSignalKey.ADD_TEXT_ITEM.toString(), messages.get(0).getValue());
    assertEquals(PlaygroundSignalKey.REMOVE_ITEM.toString(), messages.get(1).getValue());
  }

  @Test
  public void testCanRemoveAllItemTypes() throws FileNotFoundException {
    ClickableImage testClickableImage = new ClickableImageHelper("test", 0, 0, 10, 10);
    ImageItem testImage = new ImageItem("test", 0, 0, 10, 15);
    TextItem testTextItem =
        new TextItem("text", 0, 0, Color.AQUA, Font.SANS, FontStyle.BOLD, 10, 0);
    unitUnderTest.addClickableImage(testClickableImage);
    unitUnderTest.addTextItem(testTextItem);
    unitUnderTest.addImageItem(testImage);
    unitUnderTest.removeClickableImage(testClickableImage);
    unitUnderTest.removeItem(testTextItem);
    unitUnderTest.removeItem(testImage);

    // should have sent 3 add and 3 remove messages
    verify(playgroundMessageHandler, times(6)).sendMessage(messageCaptor.capture());
  }
}
