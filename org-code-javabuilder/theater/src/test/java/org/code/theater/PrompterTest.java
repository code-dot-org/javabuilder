package org.code.theater;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.util.List;
import org.code.protocol.ClientMessageDetailKeys;
import org.code.protocol.JavabuilderException;
import org.code.protocol.JavabuilderFileManager;
import org.code.protocol.OutputAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class PrompterTest {
  private OutputAdapter outputAdapter;
  private JavabuilderFileManager fileManager;
  private Prompter unitUnderTest;

  @BeforeEach
  public void setUp() {
    outputAdapter = mock(OutputAdapter.class);
    fileManager = mock(JavabuilderFileManager.class);
    unitUnderTest = new Prompter(outputAdapter, fileManager);
  }

  @Test
  public void testGetImageSendsMessageWithUploadUrl() throws JavabuilderException {
    String prompt = "Upload an image please!";
    ArgumentCaptor<TheaterMessage> message = ArgumentCaptor.forClass(TheaterMessage.class);
    final String uploadUrl = "uploadUrl";
    when(fileManager.getUploadUrl(anyString())).thenReturn(uploadUrl);

    unitUnderTest.getImage(prompt);

    verify(outputAdapter, times(1)).sendMessage(message.capture());
    assertEquals(TheaterSignalKey.GET_IMAGE.toString(), message.getValue().getValue());
    assertEquals(prompt, message.getValue().getDetail().get(ClientMessageDetailKeys.PROMPT));
    assertEquals(uploadUrl, message.getValue().getDetail().get(ClientMessageDetailKeys.UPLOAD_URL));
  }

  @Test
  public void testGetImageIncrementsUploadFileName() throws JavabuilderException {
    final ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);
    when(fileManager.getUploadUrl(fileNameCaptor.capture())).thenReturn("url");

    unitUnderTest.getImage("prompt");
    unitUnderTest.getImage("prompt");
    unitUnderTest.getImage("prompt");

    final List<String> fileNames = fileNameCaptor.getAllValues();
    for (int i = 0; i < fileNames.size(); i++) {
      // Each file name should have an incremented index appended to it, starting from 1
      assertTrue(fileNames.get(i).contains(Integer.toString(i + 1)));
    }
  }
}
