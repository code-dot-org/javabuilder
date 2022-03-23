package org.code.playground;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.FileNotFoundException;
import java.util.List;
import org.code.media.Color;
import org.code.media.Font;
import org.code.media.FontStyle;
import org.code.protocol.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class BoardTest {

  private PlaygroundMessageHandler playgroundMessageHandler;
  private InputHandler inputHandler;
  private ContentManager contentManager;
  private ArgumentCaptor<PlaygroundMessage> messageCaptor;
  private Board unitUnderTest;

  @BeforeEach
  public void setUp() {
    playgroundMessageHandler = mock(PlaygroundMessageHandler.class);
    messageCaptor = ArgumentCaptor.forClass(PlaygroundMessage.class);
    contentManager = mock(ContentManager.class);
    inputHandler = mock(InputHandler.class);

    GlobalProtocolTestFactory.builder().withContentManager(contentManager).create();
    unitUnderTest = new Board(playgroundMessageHandler, inputHandler, contentManager);
  }

  @AfterEach
  public void tearDown() {
    GlobalProtocolTestFactory.tearDown();
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
    assertEquals(
        filename,
        messageCaptor.getAllValues().get(0).getDetail().get(ClientMessageDetailKeys.FILENAME));
  }

  @Test
  public void testPlaySoundThrowsExceptionIfFileNotFound() throws FileNotFoundException {
    final FileNotFoundException expected = new FileNotFoundException();
    doThrow(expected).when(contentManager).verifyAssetFilename(anyString());

    final FileNotFoundException actual =
        assertThrows(FileNotFoundException.class, () -> unitUnderTest.playSound("test_file.wav"));

    assertSame(expected, actual);
  }

  @Test
  public void testSetBackgroundImageSendsMessage() throws FileNotFoundException {
    final String backgroundFilename = "background.png";

    unitUnderTest.setBackgroundImage(backgroundFilename);

    verify(playgroundMessageHandler).sendMessage(messageCaptor.capture());
    final PlaygroundMessage message = messageCaptor.getValue();
    assertEquals(PlaygroundSignalKey.SET_BACKGROUND_IMAGE.toString(), message.getValue());
    assertTrue(message.getDetail().has(ClientMessageDetailKeys.FILENAME));
    assertEquals(
        backgroundFilename, message.getDetail().getString(ClientMessageDetailKeys.FILENAME));
  }

  @Test
  public void testSetBackgroundImageExceptionIfFileNotFound() throws FileNotFoundException {
    final FileNotFoundException expected = new FileNotFoundException();
    doThrow(expected).when(contentManager).verifyAssetFilename(anyString());

    final FileNotFoundException actual =
        assertThrows(
            FileNotFoundException.class, () -> unitUnderTest.setBackgroundImage("background.png"));

    assertSame(expected, actual);
  }

  @Test
  public void testStartSendsMessageAndWaitsForInput() throws PlaygroundException {
    // Need to make sure end() is called so start() terminates
    when(inputHandler.getNextMessageForType(InputMessageType.PLAYGROUND))
        .thenAnswer(
            invocation -> {
              unitUnderTest.end();
              return "id";
            });

    unitUnderTest.start();

    verify(playgroundMessageHandler, times(2)).sendMessage(messageCaptor.capture());
    // expect to send 1 batch when run starts, and 1 after input message received
    verify(playgroundMessageHandler, times(2)).sendBatchedMessages();
    assertEquals(
        PlaygroundSignalKey.RUN.toString(), messageCaptor.getAllValues().get(0).getValue());
    verify(inputHandler).getNextMessageForType(InputMessageType.PLAYGROUND);
  }

  @Test
  public void testStartThrowsExceptionIfCalledTwice() throws PlaygroundException {
    // Need to make sure end() is called so start() terminates
    when(inputHandler.getNextMessageForType(InputMessageType.PLAYGROUND))
        .thenAnswer(
            invocation -> {
              unitUnderTest.end();
              return "id";
            });

    unitUnderTest.start();
    final PlaygroundException e =
        assertThrows(PlaygroundException.class, () -> unitUnderTest.start());
    assertEquals(PlaygroundExceptionKeys.PLAYGROUND_RUNNING.toString(), e.getMessage());
  }

  @Test
  public void testStartSendsUpdateCompleteIfGameNotEnded() throws PlaygroundException {
    // Simulate three messages with end only being called on the last
    when(inputHandler.getNextMessageForType(InputMessageType.PLAYGROUND))
        .thenReturn("id")
        .thenReturn("id")
        .thenAnswer(
            invocation -> {
              unitUnderTest.end();
              return "id";
            });

    unitUnderTest.start();

    verify(playgroundMessageHandler, times(4)).sendMessage(messageCaptor.capture());

    // UPDATE_COMPLETE should only be sent after the first two messages, not the last
    assertEquals(
        PlaygroundSignalKey.UPDATE_COMPLETE.toString(),
        messageCaptor.getAllValues().get(1).getValue());
    assertEquals(
        PlaygroundSignalKey.UPDATE_COMPLETE.toString(),
        messageCaptor.getAllValues().get(2).getValue());
  }

  @Test
  public void testSendsBatchAfterEveryInputMessage() throws PlaygroundException {
    // Simulate three messages with end being called on the last
    when(inputHandler.getNextMessageForType(InputMessageType.PLAYGROUND))
        .thenReturn("id")
        .thenReturn("id")
        .thenAnswer(
            invocation -> {
              unitUnderTest.end();
              return "id";
            });

    unitUnderTest.start();

    // send 1 batch at start, and 1 after each input event for a total of 4. Should still send
    // a batch on end
    verify(playgroundMessageHandler, times(4)).sendBatchedMessages();
  }

  @Test
  public void testEndSendsExitMessage() throws PlaygroundException {
    // Ensure that end() is called while running to avoid exception
    when(inputHandler.getNextMessageForType(InputMessageType.PLAYGROUND))
        .thenAnswer(
            invocation -> {
              unitUnderTest.end();
              return "id";
            });
    unitUnderTest.start();

    verify(playgroundMessageHandler, times(2)).sendMessage(messageCaptor.capture());
    assertEquals(
        PlaygroundSignalKey.EXIT.toString(), messageCaptor.getAllValues().get(1).getValue());
  }

  @Test
  public void testEndWithSoundSendsPlaySoundAndExitMessages()
      throws PlaygroundException, FileNotFoundException {
    String filename = "test_file.wav";

    // Ensure that end() is called while running to avoid exception
    when(inputHandler.getNextMessageForType(InputMessageType.PLAYGROUND))
        .thenAnswer(
            invocation -> {
              unitUnderTest.end(filename);
              return "id";
            });
    unitUnderTest.start();

    verify(playgroundMessageHandler, times(3)).sendMessage(messageCaptor.capture());
    assertEquals(
        PlaygroundSignalKey.PLAY_SOUND.toString(), messageCaptor.getAllValues().get(1).getValue());
    assertEquals(
        filename,
        messageCaptor.getAllValues().get(1).getDetail().get(ClientMessageDetailKeys.FILENAME));
    assertEquals(
        PlaygroundSignalKey.EXIT.toString(), messageCaptor.getAllValues().get(2).getValue());
  }

  @Test
  public void testEndWithSoundThrowsExceptionIfFileNotFound()
      throws PlaygroundException, FileNotFoundException {
    final FileNotFoundException expected = new FileNotFoundException();
    doThrow(expected).when(contentManager).verifyAssetFilename(anyString());

    when(inputHandler.getNextMessageForType(InputMessageType.PLAYGROUND))
        .thenAnswer(
            invocation -> {
              final FileNotFoundException actual =
                  assertThrows(
                      FileNotFoundException.class, () -> unitUnderTest.end("test_file.wav"));
              assertSame(expected, actual);

              // Need to still call end() to ensure start() terminates
              unitUnderTest.end();
              return "id";
            });

    unitUnderTest.start();
  }

  @Test
  public void testEndThrowsExceptionIfNotRunning() {
    final PlaygroundException e =
        assertThrows(PlaygroundException.class, () -> unitUnderTest.end());
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
