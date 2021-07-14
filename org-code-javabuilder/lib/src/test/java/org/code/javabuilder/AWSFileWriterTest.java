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
    fileWriter.writeToFile("test.txt", new ByteArrayInputStream(new byte[10]));
    fileWriter.writeToFile("test2.txt", new ByteArrayInputStream(new byte[10]));
    assertThrows(
        UserInitiatedException.class,
        () -> fileWriter.writeToFile("test3.txt", new ByteArrayInputStream(new byte[10])));
  }

  @Test
  void writesToS3() throws JavabuilderException {
    ByteArrayInputStream input = new ByteArrayInputStream(new byte[10]);
    fileWriter.writeToFile("test.txt", input);
    String key = FAKE_SESSION_ID + "/test.txt";
    verify(s3ClientMock).putObject(FAKE_BUCKET_NAME, key, input, null);
  }
}
