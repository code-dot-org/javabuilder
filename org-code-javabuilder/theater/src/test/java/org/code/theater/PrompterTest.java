package org.code.theater;

import static org.code.protocol.InputMessages.UPLOAD_ERROR;
import static org.code.protocol.InputMessages.UPLOAD_SUCCESS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.List;
import org.code.media.Image;
import org.code.protocol.*;
import org.code.theater.support.TheaterMessage;
import org.code.theater.support.TheaterSignalKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class PrompterTest {
  private OutputAdapter outputAdapter;
  private JavabuilderFileManager fileManager;
  private InputHandler inputHandler;
  private MockedStatic<Image> image;
  private Prompter unitUnderTest;

  @BeforeEach
  public void setUp() {
    outputAdapter = mock(OutputAdapter.class);
    fileManager = mock(JavabuilderFileManager.class);
    inputHandler = mock(InputHandler.class);
    image = mockStatic(Image.class);
    unitUnderTest = new Prompter(outputAdapter, fileManager, inputHandler);
  }

  @AfterEach
  public void tearDown() {
    image.close();
  }

  @Test
  public void testGetImageSendsMessageWithUploadUrlAndReturnsImage()
      throws JavabuilderException, FileNotFoundException {
    String prompt = "Upload an image please!";
    ArgumentCaptor<TheaterMessage> message = ArgumentCaptor.forClass(TheaterMessage.class);
    final String uploadUrl = "uploadUrl";
    when(fileManager.getUploadUrl(anyString())).thenReturn(uploadUrl);
    when(inputHandler.getNextMessageForType(InputMessageType.THEATER)).thenReturn(UPLOAD_SUCCESS);

    final Image expectedImage = mock(Image.class);
    image.when(() -> Image.fromMediaFile(anyString())).thenReturn(expectedImage);

    assertSame(expectedImage, unitUnderTest.getImage(prompt));

    verify(outputAdapter, times(1)).sendMessage(message.capture());
    assertEquals(TheaterSignalKey.GET_IMAGE.toString(), message.getValue().getValue());
    assertEquals(prompt, message.getValue().getDetail().get(ClientMessageDetailKeys.PROMPT));
    assertEquals(uploadUrl, message.getValue().getDetail().get(ClientMessageDetailKeys.UPLOAD_URL));
  }

  @Test
  public void testGetImageIncrementsUploadFileName()
      throws JavabuilderException, FileNotFoundException {
    final ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);
    when(fileManager.getUploadUrl(fileNameCaptor.capture())).thenReturn("url");
    when(inputHandler.getNextMessageForType(InputMessageType.THEATER)).thenReturn(UPLOAD_SUCCESS);

    when(fileManager.getFileUrl(anyString())).thenReturn(mock(URL.class));
    image.when(() -> Image.fromMediaFile(anyString())).thenReturn(mock(Image.class));

    unitUnderTest.getImage("prompt");
    unitUnderTest.getImage("prompt");
    unitUnderTest.getImage("prompt");

    final List<String> fileNames = fileNameCaptor.getAllValues();
    for (int i = 0; i < fileNames.size(); i++) {
      // Each file name should have an incremented index appended to it, starting from 1
      assertTrue(fileNames.get(i).contains(Integer.toString(i + 1)));
    }
  }

  @Test
  public void testGetImageThrowsExceptionIfUploadFails() throws JavabuilderException {
    when(fileManager.getUploadUrl(anyString())).thenReturn("url");
    when(inputHandler.getNextMessageForType(InputMessageType.THEATER)).thenReturn(UPLOAD_ERROR);

    final Exception actual =
        assertThrows(InternalServerRuntimeError.class, () -> unitUnderTest.getImage("prompt"));

    assertEquals(InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION.toString(), actual.getMessage());
  }

  @Test
  public void testGetImageThrowsExceptionIfUnknownMessageReceived() throws JavabuilderException {
    when(fileManager.getUploadUrl(anyString())).thenReturn("url");
    when(inputHandler.getNextMessageForType(InputMessageType.THEATER)).thenReturn("other message");

    final Exception actual =
        assertThrows(InternalServerRuntimeError.class, () -> unitUnderTest.getImage("prompt"));

    assertEquals(InternalErrorKey.UNKNOWN_ERROR.toString(), actual.getMessage());
  }
}
