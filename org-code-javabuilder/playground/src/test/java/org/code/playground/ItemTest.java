package org.code.playground;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.List;
import org.code.protocol.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class ItemTest {
  private PlaygroundMessageHandler playgroundMessageHandler;
  private ArgumentCaptor<PlaygroundMessage> messageCaptor;
  private MockedStatic<PlaygroundMessageHandler> messageHandlerMockedStatic;

  @BeforeEach
  public void setUp() {
    playgroundMessageHandler = mock(PlaygroundMessageHandler.class);
    messageCaptor = ArgumentCaptor.forClass(PlaygroundMessage.class);
    GlobalProtocolTestFactory.builder().create();

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
  public void settersSendChangeMessagesIfTheyAreOn() {
    Item item = new ItemHelper(0, 0, 15);
    item.turnOnChangeMessages();

    int newX = 5;
    int newY = 10;
    int newHeight = 100;
    item.setX(newX);
    item.setY(newY);
    item.setHeight(newHeight);

    verify(playgroundMessageHandler, times(3)).sendMessage(messageCaptor.capture());
    List<PlaygroundMessage> messages = messageCaptor.getAllValues();
    assertEquals(PlaygroundSignalKey.CHANGE_ITEM.toString(), messages.get(0).getValue());
    assertEquals(
        Integer.toString(newX), messages.get(0).getDetail().get(ClientMessageDetailKeys.X));
    assertEquals(
        Integer.toString(newY), messages.get(1).getDetail().get(ClientMessageDetailKeys.Y));
    assertEquals(
        Integer.toString(newHeight),
        messages.get(2).getDetail().get(ClientMessageDetailKeys.HEIGHT));
  }

  @Test
  public void settersDoNotSendChangeMessagesByDefault() {
    Item item = new ItemHelper(0, 0, 15);

    int newX = 5;
    int newY = 10;
    int newHeight = 100;
    item.setX(newX);
    item.setY(newY);
    item.setHeight(newHeight);

    verify(playgroundMessageHandler, times(0)).sendMessage(messageCaptor.capture());
    assertEquals(newHeight, item.getHeight());
    assertEquals(newX, item.getX());
    assertEquals(newY, item.getY());
  }
}
