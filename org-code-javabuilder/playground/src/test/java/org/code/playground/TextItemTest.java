package org.code.playground;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.List;
import org.code.media.Color;
import org.code.media.Font;
import org.code.media.FontStyle;
import org.code.protocol.*;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class TextItemTest {
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
  public void settersSendChangeMessages() {
    TextItem textItem = new TextItem("text", 0, 0, Color.BLUE, Font.SANS, FontStyle.BOLD, 10, 0);

    String newText = "new text";
    Color newColor = Color.GREEN;
    Font newFont = Font.SERIF;
    FontStyle newFontStyle = FontStyle.ITALIC;
    double newRotation = 15;

    textItem.setText(newText);
    textItem.setColor(newColor);
    textItem.setFont(newFont);
    textItem.setFontStyle(newFontStyle);
    textItem.setRotation(newRotation);

    verify(playgroundMessageHandler, times(5)).sendMessage(messageCaptor.capture());
    List<PlaygroundMessage> messages = messageCaptor.getAllValues();
    assertEquals(PlaygroundSignalKey.CHANGE_ITEM.toString(), messages.get(0).getValue());
    assertEquals(newText, messages.get(0).getDetail().get(ClientMessageDetailKeys.TEXT));
    JSONObject colorDetails = messages.get(1).getDetail();
    assertEquals(
        Integer.toString(newColor.getRed()), colorDetails.get(ClientMessageDetailKeys.COLOR_RED));
    assertEquals(
        Integer.toString(newColor.getBlue()), colorDetails.get(ClientMessageDetailKeys.COLOR_BLUE));
    assertEquals(
        Integer.toString(newColor.getGreen()),
        colorDetails.get(ClientMessageDetailKeys.COLOR_GREEN));
    assertEquals(newFont.toString(), messages.get(2).getDetail().get(ClientMessageDetailKeys.FONT));
    assertEquals(
        newFontStyle.toString(),
        messages.get(3).getDetail().get(ClientMessageDetailKeys.FONT_STYLE));
    assertEquals(
        Double.toString(newRotation),
        messages.get(4).getDetail().get(ClientMessageDetailKeys.ROTATION));
  }
}
