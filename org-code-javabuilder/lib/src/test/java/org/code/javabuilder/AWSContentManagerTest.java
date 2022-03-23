package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Date;
import org.code.protocol.JavabuilderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AWSContentManagerTest {
  private AmazonS3 s3ClientMock;
  private AWSContentManager contentManager;
  private Context context;
  private ProjectData projectData;
  private UserProjectFiles projectFiles;
  private ArgumentCaptor<String> assetUrlCaptor;

  private static final String FAKE_BUCKET_NAME = "bucket-name";
  private static final String FAKE_SESSION_ID = "12345";
  private static final String FAKE_OUTPUT_URL = "a-url";

  @BeforeEach
  void setUp() {
    s3ClientMock = mock(AmazonS3.class);
    context = mock(Context.class);
    projectData = mock(ProjectData.class);
    projectFiles = mock(UserProjectFiles.class);
    assetUrlCaptor = ArgumentCaptor.forClass(String.class);
    contentManager =
        new AWSContentManager(
            s3ClientMock, FAKE_BUCKET_NAME, FAKE_SESSION_ID, FAKE_OUTPUT_URL, context, projectData);
  }

  @Test
  void canOnlyWriteTwice() throws JavabuilderException {
    contentManager.writeToOutputFile("test.gif", new byte[10], "image/gif");
    contentManager.writeToOutputFile("test2.wav", new byte[10], "audio/wav");
    assertThrows(
        UserInitiatedException.class,
        () -> contentManager.writeToOutputFile("test3.txt", new byte[10], "text/plain"));
  }

  @Test
  void writesToS3() throws JavabuilderException {
    byte[] input = new byte[10];
    contentManager.writeToOutputFile("test.txt", input, "text/plain");
    String key = FAKE_SESSION_ID + "/test.txt";
    verify(s3ClientMock)
        .putObject(anyString(), anyString(), any(ByteArrayInputStream.class), any());
  }

  @Test
  public void testGetUploadUrlReturnsGeneratedUrl() throws JavabuilderException {
    final String fileName = "file1";
    final String key = FAKE_SESSION_ID + "/" + fileName;
    final String urlFileName = "/file/path?queryParams";
    final URL presignedUrl = mock(URL.class);
    when(presignedUrl.getFile()).thenReturn(urlFileName);
    when(context.getRemainingTimeInMillis()).thenReturn(1000);
    when(s3ClientMock.generatePresignedUrl(
            eq(FAKE_BUCKET_NAME), eq(key), any(Date.class), eq(HttpMethod.PUT)))
        .thenReturn(presignedUrl);

    final String uploadUrl = contentManager.generateAssetUploadUrl(fileName);
    assertEquals(FAKE_OUTPUT_URL + urlFileName, uploadUrl);
    verify(s3ClientMock)
        .generatePresignedUrl(eq(FAKE_BUCKET_NAME), eq(key), any(Date.class), eq(HttpMethod.PUT));
    // Verify that the URL was added to the project data's asset map
    verify(projectData).addNewAssetUrl(eq(fileName), assetUrlCaptor.capture());
    assertTrue(assetUrlCaptor.getValue().contains(key));
  }

  @Test
  public void testGetUploadUrlThrowsExceptionForTooManyUploads() throws JavabuilderException {
    final URL presignedUrl = mock(URL.class);
    when(presignedUrl.getFile()).thenReturn("/file/path?queryParams");
    when(context.getRemainingTimeInMillis()).thenReturn(1000);
    when(s3ClientMock.generatePresignedUrl(
            eq(FAKE_BUCKET_NAME), anyString(), any(Date.class), eq(HttpMethod.PUT)))
        .thenReturn(presignedUrl);

    // Upload limit is 20
    for (int i = 0; i < 20; i++) {
      contentManager.generateAssetUploadUrl("file");
    }

    final Exception exception =
        assertThrows(
            UserInitiatedException.class, () -> contentManager.generateAssetUploadUrl("file"));
    assertEquals(UserInitiatedExceptionKey.TOO_MANY_UPLOADS.toString(), exception.getMessage());
  }

  @Test
  public void testGenerateAssetUrlReturnsUrlFromProjectData() {
    final String filename = "file";
    final String url = "url";
    when(projectData.getAssetUrl(filename)).thenReturn(url);
    assertEquals(url, contentManager.getAssetUrl(filename));
  }

  @Test
  public void testVerifyAssetFileNameThrowsExceptionIfFileNotFound() {
    final String filename = "file";
    when(projectData.doesAssetUrlExist(anyString())).thenReturn(false);
    final Exception actual =
        assertThrows(
            FileNotFoundException.class, () -> contentManager.verifyAssetFilename(filename));
    assertEquals(filename, actual.getMessage());
  }
}
