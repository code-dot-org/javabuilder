package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Date;
import org.code.protocol.JavabuilderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AWSFileManagerTest {
  private AmazonS3 s3ClientMock;
  private AWSFileManager fileManager;
  private Context context;

  private static final String FAKE_BUCKET_NAME = "bucket-name";
  private static final String FAKE_SESSION_ID = "12345";
  private static final String FAKE_OUTPUT_URL = "a-url";

  @BeforeEach
  void setUp() {
    s3ClientMock = mock(AmazonS3.class);
    context = mock(Context.class);
    fileManager =
        new AWSFileManager(
            s3ClientMock, FAKE_BUCKET_NAME, FAKE_SESSION_ID, FAKE_OUTPUT_URL, context);
  }

  @Test
  void canOnlyWriteTwice() throws JavabuilderException {
    fileManager.writeToFile("test.gif", new byte[10], "image/gif");
    fileManager.writeToFile("test2.wav", new byte[10], "audio/wav");
    assertThrows(
        UserInitiatedException.class,
        () -> fileManager.writeToFile("test3.txt", new byte[10], "text/plain"));
  }

  @Test
  void writesToS3() throws JavabuilderException {
    byte[] input = new byte[10];
    fileManager.writeToFile("test.txt", input, "text/plain");
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

    final String uploadUrl = fileManager.getUploadUrl(fileName);
    assertEquals(FAKE_OUTPUT_URL + urlFileName, uploadUrl);
    verify(s3ClientMock)
        .generatePresignedUrl(eq(FAKE_BUCKET_NAME), eq(key), any(Date.class), eq(HttpMethod.PUT));
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
      fileManager.getUploadUrl("file");
    }

    final Exception exception =
        assertThrows(UserInitiatedException.class, () -> fileManager.getUploadUrl("file"));
    assertEquals(UserInitiatedExceptionKey.TOO_MANY_UPLOADS.toString(), exception.getMessage());
  }
}
