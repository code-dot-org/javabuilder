package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.amazonaws.services.s3.AmazonS3;
import java.io.ByteArrayInputStream;
import org.code.protocol.JavabuilderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AWSFileWriterTest {
  private AmazonS3 s3ClientMock;
  private AWSFileWriter fileWriter;

  private static final String FAKE_BUCKET_NAME = "bucket-name";
  private static final String FAKE_SESSION_ID = "12345";
  private static final String FAKE_OUTPUT_URL = "a-url";

  @BeforeEach
  void setUp() {
    s3ClientMock = mock(AmazonS3.class);
    fileWriter =
        new AWSFileWriter(s3ClientMock, FAKE_BUCKET_NAME, FAKE_SESSION_ID, FAKE_OUTPUT_URL);
  }

  @Test
  void canOnlyWriteTwice() throws JavabuilderException {
    fileWriter.writeToFile("test.gif", new byte[10], "image/gif");
    fileWriter.writeToFile("test2.wav", new byte[10], "audio/wav");
    assertThrows(
        UserInitiatedException.class,
        () -> fileWriter.writeToFile("test3.txt", new byte[10], "text/plain"));
  }

  @Test
  void writesToS3() throws JavabuilderException {
    byte[] input = new byte[10];
    fileWriter.writeToFile("test.txt", input, "text/plain");
    String key = FAKE_SESSION_ID + "/test.txt";
    verify(s3ClientMock)
        .putObject(anyString(), anyString(), any(ByteArrayInputStream.class), any());
  }
}
