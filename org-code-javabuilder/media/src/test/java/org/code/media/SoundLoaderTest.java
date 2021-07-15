package org.code.media;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.code.protocol.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class SoundLoaderTest {

  private static final String TEST_DASHBOARD = "https://localhost-studio.code.org";
  private static final String TEST_CHANNEL_ID = "0";
  private static final String TEST_FILE_NAME = "beatbox.wav";
  private static final byte[] TEST_BYTE_ARRAY = {1, 0, 1, 0};
  private static final int TEST_CHANNELS = 2;
  private static final double[] TEST_DOUBLE_ARRAY = {1.0, 0.0, 1.0, 0.0};

  private MockedStatic<AudioSystem> audioSystem;
  private MockedStatic<AudioUtils> audioUtils;
  private AudioInputStream audioInputStream;
  private AudioFormat audioFormat;

  @BeforeEach
  public void setUp() {
    GlobalProtocol.create(
        mock(OutputAdapter.class),
        mock(InputAdapter.class),
        TEST_DASHBOARD,
        TEST_CHANNEL_ID,
        mock(JavabuilderFileWriter.class));

    audioSystem = mockStatic(AudioSystem.class);
    audioUtils = mockStatic(AudioUtils.class);

    audioInputStream = mock(AudioInputStream.class);
    audioFormat = mock(AudioFormat.class);

    when(audioInputStream.getFormat()).thenReturn(audioFormat);
    when(audioFormat.getChannels()).thenReturn(TEST_CHANNELS);
  }

  @AfterEach
  public void tearDown() {
    audioSystem.close();
    audioUtils.close();
  }

  @Test
  public void testReadThrowsIOExceptionIfFilenameInvalid() {
    audioSystem
        .when(() -> AudioSystem.getAudioInputStream(any(URL.class)))
        .thenThrow(IOException.class);
    Exception exception =
        assertThrows(
            FileNotFoundException.class,
            () -> {
              SoundLoader.read(TEST_FILE_NAME);
            });
    assertTrue(exception.getMessage().contains("Could not find file: " + TEST_FILE_NAME));
  }

  @Test
  public void testReadThrowsSoundExceptionIfFileFormatInvalid() {
    audioSystem
        .when(() -> AudioSystem.getAudioInputStream(any(URL.class)))
        .thenThrow(UnsupportedAudioFileException.class);
    Exception exception =
        assertThrows(
            SoundException.class,
            () -> {
              SoundLoader.read(TEST_FILE_NAME);
            });
    assertEquals(SoundExceptionKeys.INVALID_AUDIO_FILE_FORMAT.toString(), exception.getMessage());
  }

  @Test
  public void testReadThrowsSoundExceptionIfAudioFormatInvalid() {
    audioSystem
        .when(() -> AudioSystem.getAudioInputStream(any(URL.class)))
        .thenReturn(audioInputStream);
    audioUtils.when(() -> AudioUtils.isAudioFormatValid(audioFormat)).thenReturn(false);
    Exception exception =
        assertThrows(
            SoundException.class,
            () -> {
              SoundLoader.read(TEST_FILE_NAME);
            });
    assertEquals(SoundExceptionKeys.INVALID_AUDIO_FILE_FORMAT.toString(), exception.getMessage());
  }

  @Test
  public void testReadThrowsInternalExceptionIfErrorInReadingBytes() throws IOException {
    audioSystem
        .when(() -> AudioSystem.getAudioInputStream(any(URL.class)))
        .thenReturn(audioInputStream);
    audioUtils.when(() -> AudioUtils.isAudioFormatValid(audioFormat)).thenReturn(true);

    when(audioInputStream.readAllBytes()).thenThrow(IOException.class);

    Exception exception =
        assertThrows(
            InternalJavabuilderError.class,
            () -> {
              SoundLoader.read(TEST_FILE_NAME);
            });

    assertEquals(InternalErrorKey.INTERNAL_EXCEPTION.toString(), exception.getMessage());
    verify(audioInputStream).readAllBytes();
  }

  @Test
  public void testReadThrowsInternalExceptionIfErrorInClosingStream() throws IOException {
    audioSystem
        .when(() -> AudioSystem.getAudioInputStream(any(URL.class)))
        .thenReturn(audioInputStream);
    audioUtils.when(() -> AudioUtils.isAudioFormatValid(audioFormat)).thenReturn(true);

    when(audioInputStream.readAllBytes()).thenReturn(TEST_BYTE_ARRAY);
    doThrow(IOException.class).when(audioInputStream).close();

    Exception exception =
        assertThrows(
            InternalJavabuilderError.class,
            () -> {
              SoundLoader.read(TEST_FILE_NAME);
            });

    assertEquals(InternalErrorKey.INTERNAL_EXCEPTION.toString(), exception.getMessage());
    verify(audioInputStream).readAllBytes();
    verify(audioInputStream).close();
  }

  @Test
  public void testReadReturnsConvertedArrayIfFileIsValid() throws IOException {
    audioSystem
        .when(() -> AudioSystem.getAudioInputStream(any(URL.class)))
        .thenReturn(audioInputStream);
    audioUtils.when(() -> AudioUtils.isAudioFormatValid(audioFormat)).thenReturn(true);
    audioUtils
        .when(() -> AudioUtils.convertByteArrayToDoubleArray(TEST_BYTE_ARRAY, TEST_CHANNELS))
        .thenReturn(TEST_DOUBLE_ARRAY);

    when(audioInputStream.readAllBytes()).thenReturn(TEST_BYTE_ARRAY);

    assertEquals(TEST_DOUBLE_ARRAY, SoundLoader.read(TEST_FILE_NAME));

    verify(audioFormat).getChannels();
  }
}
