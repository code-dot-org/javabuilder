package org.code.playground;

import static org.junit.jupiter.api.Assertions.*;
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
        mock(JavabuilderFileManager.class),
        mock(LifecycleNotifier.class));
    CachedResources.create();
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
    TextItem textItem = new TextItem("text", 0, 0, Color.BLUE, Font.SANS, FontStyle.BOLD, 10, 0);
    textItem.turnOnChangeMessages();

    String newText = "new text";
    Font newFont = Font.SERIF;
    FontStyle newFontStyle = FontStyle.ITALIC;
    double newRotation = 15;

    textItem.setText(newText);
    textItem.setFont(newFont);
    textItem.setFontStyle(newFontStyle);
    textItem.setRotation(newRotation);

    verify(playgroundMessageHandler, times(4)).sendMessage(messageCaptor.capture());
    List<PlaygroundMessage> messages = messageCaptor.getAllValues();
    assertEquals(PlaygroundSignalKey.CHANGE_ITEM.toString(), messages.get(0).getValue());
    assertEquals(newText, messages.get(0).getDetail().get(ClientMessageDetailKeys.TEXT));
    assertEquals(newFont.toString(), messages.get(1).getDetail().get(ClientMessageDetailKeys.FONT));
    assertEquals(
        newFontStyle.toString(),
        messages.get(2).getDetail().get(ClientMessageDetailKeys.FONT_STYLE));
    assertEquals(
        Double.toString(newRotation),
        messages.get(3).getDetail().get(ClientMessageDetailKeys.ROTATION));
  }

  @Test
  public void testColorSettersSendMessagesIfTheyAreOn() {
    TextItem textItem = new TextItem("text", 0, 0, Color.BLUE, Font.SANS, FontStyle.BOLD, 10, 0);
    textItem.turnOnChangeMessages();

    Color newColor = Color.GREEN;

    int colorRed = 50;
    int colorGreen = 100;
    int colorBlue = 150;

    int outOfBoundsColorRed = 300;
    int outOfBoundsColorBlue = -100;

    textItem.setColor(newColor);
    textItem.setRed(colorRed);
    textItem.setGreen(colorGreen);
    textItem.setBlue(colorBlue);
    textItem.setRed(outOfBoundsColorRed);
    textItem.setBlue(outOfBoundsColorBlue);

    verify(playgroundMessageHandler, times(6)).sendMessage(messageCaptor.capture());
    List<PlaygroundMessage> messages = messageCaptor.getAllValues();

    JSONObject colorDetails = messages.get(0).getDetail();
    assertEquals(
        Integer.toString(newColor.getRed()), colorDetails.get(ClientMessageDetailKeys.COLOR_RED));
    assertEquals(
        Integer.toString(newColor.getBlue()), colorDetails.get(ClientMessageDetailKeys.COLOR_BLUE));
    assertEquals(
        Integer.toString(newColor.getGreen()),
        colorDetails.get(ClientMessageDetailKeys.COLOR_GREEN));

    assertEquals(
        Integer.toString(colorRed),
        messages.get(1).getDetail().get(ClientMessageDetailKeys.COLOR_RED));
    assertEquals(
        Integer.toString(colorGreen),
        messages.get(2).getDetail().get(ClientMessageDetailKeys.COLOR_GREEN));
    assertEquals(
        Integer.toString(colorBlue),
        messages.get(3).getDetail().get(ClientMessageDetailKeys.COLOR_BLUE));

    assertEquals(
        Integer.toString(255), messages.get(4).getDetail().get(ClientMessageDetailKeys.COLOR_RED));
    assertEquals(
        Integer.toString(0), messages.get(5).getDetail().get(ClientMessageDetailKeys.COLOR_BLUE));
  }

  @Test
  public void testGetColorReturnsColor() {
    final Color color = Color.AQUA;
    final TextItem textItem = new TextItem("text", 0, 0, color, Font.SANS, 0, 0);

    assertSame(color, textItem.getColor());
    assertEquals(color.getRed(), textItem.getColor().getRed());
    assertEquals(color.getGreen(), textItem.getColor().getGreen());
    assertEquals(color.getBlue(), textItem.getColor().getBlue());

    final Color newColor = Color.BEIGE;
    textItem.setColor(newColor);

    assertSame(newColor, textItem.getColor());
    assertEquals(newColor.getRed(), textItem.getColor().getRed());
    assertEquals(newColor.getGreen(), textItem.getColor().getGreen());
    assertEquals(newColor.getBlue(), textItem.getColor().getBlue());
  }
}
