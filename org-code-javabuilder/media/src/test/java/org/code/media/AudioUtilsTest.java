package org.code.media;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.code.protocol.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class AudioUtilsTest {

  private static final String TEST_FILE_URL =
      "https://localhost-studio.code.org/assets/beatbox.wav";
  private static final String TEST_FILE_NAME = "beatbox.wav";
  private static final int TEST_CHANNELS = 1;

  private static final double[] DOUBLE_ARRAY = {1.0, -1.0, 0.5, -0.5, 0.0};
  private static final byte[] BYTE_ARRAY_MONO = {
    (byte) 0xFF, (byte) 0x7F, // 1.0
    (byte) 0x00, (byte) 0x80, // -1.0
    (byte) 0x00, (byte) 0x40, // 0.5
    (byte) 0x00, (byte) 0xC0, // -0.5
    (byte) 0x00, (byte) 0x00 // 0.0
  };

  private MockedStatic<AudioSystem> audioSystem;
  private AudioInputStream audioInputStream;
  private AudioFormat audioFormat;

  @BeforeEach
  public void setUp() {
    final ContentManager contentManager = mock(ContentManager.class);
    GlobalProtocolTestFactory.builder().withContentManager(contentManager).create();

    when(contentManager.getAssetUrl(TEST_FILE_NAME)).thenReturn(TEST_FILE_URL);

    audioSystem = mockStatic(AudioSystem.class);
    audioInputStream = mock(AudioInputStream.class);
    audioFormat = mock(AudioFormat.class);

    when(audioInputStream.getFormat()).thenReturn(audioFormat);
    when(audioFormat.getChannels()).thenReturn(TEST_CHANNELS);
  }

  @AfterEach
  public void tearDown() {
    GlobalProtocolTestFactory.tearDown();
    audioSystem.close();
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
              AudioUtils.readSamplesFromAssetFile(TEST_FILE_NAME);
            });
    assertTrue(exception.getMessage().contains(TEST_FILE_NAME));
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
              AudioUtils.readSamplesFromAssetFile(TEST_FILE_NAME);
            });
    assertEquals(SoundExceptionKeys.INVALID_AUDIO_FILE_FORMAT.toString(), exception.getMessage());
  }

  @Test
  public void testReadThrowsSoundExceptionIfFormatConversionNotSupported() {
    final SoundException soundException =
        new SoundException(SoundExceptionKeys.INVALID_AUDIO_FILE_FORMAT);
    audioSystem
        .when(() -> AudioSystem.getAudioInputStream(any(URL.class)))
        .thenReturn(audioInputStream);
    audioSystem
        .when(() -> AudioSystem.isConversionSupported(any(AudioFormat.class), eq(audioFormat)))
        .thenThrow(soundException);
    Exception exception =
        assertThrows(
            SoundException.class,
            () -> {
              AudioUtils.readSamplesFromAssetFile(TEST_FILE_NAME);
            });
    assertSame(soundException, exception);
  }

  @Test
  public void testReadThrowsInternalExceptionIfErrorInReadingBytes() throws IOException {
    audioSystem
        .when(() -> AudioSystem.getAudioInputStream(any(URL.class)))
        .thenReturn(audioInputStream);
    audioSystem
        .when(() -> AudioSystem.isConversionSupported(any(AudioFormat.class), eq(audioFormat)))
        .thenReturn(true);
    audioSystem
        .when(() -> AudioSystem.getAudioInputStream(any(AudioFormat.class), eq(audioInputStream)))
        .thenReturn(audioInputStream);

    when(audioInputStream.readAllBytes()).thenThrow(IOException.class);

    Exception exception =
        assertThrows(
            InternalServerRuntimeError.class,
            () -> {
              AudioUtils.readSamplesFromAssetFile(TEST_FILE_NAME);
            });

    assertEquals(InternalErrorKey.INTERNAL_EXCEPTION.toString(), exception.getMessage());
    verify(audioInputStream).readAllBytes();
  }

  @Test
  public void testReadThrowsInternalExceptionIfErrorInClosingStream() throws IOException {
    audioSystem
        .when(() -> AudioSystem.getAudioInputStream(any(URL.class)))
        .thenReturn(audioInputStream);
    audioSystem
        .when(() -> AudioSystem.isConversionSupported(any(AudioFormat.class), eq(audioFormat)))
        .thenReturn(true);
    audioSystem
        .when(() -> AudioSystem.getAudioInputStream(any(AudioFormat.class), eq(audioInputStream)))
        .thenReturn(audioInputStream);

    when(audioInputStream.readAllBytes()).thenReturn(BYTE_ARRAY_MONO);
    doThrow(IOException.class).when(audioInputStream).close();

    Exception exception =
        assertThrows(
            InternalServerRuntimeError.class,
            () -> {
              AudioUtils.readSamplesFromAssetFile(TEST_FILE_NAME);
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
    audioSystem
        .when(() -> AudioSystem.isConversionSupported(any(AudioFormat.class), eq(audioFormat)))
        .thenReturn(true);
    audioSystem
        .when(() -> AudioSystem.getAudioInputStream(any(AudioFormat.class), eq(audioInputStream)))
        .thenReturn(audioInputStream);

    when(audioInputStream.readAllBytes()).thenReturn(BYTE_ARRAY_MONO);

    final double[] converted = AudioUtils.readSamplesFromAssetFile(TEST_FILE_NAME);
    assertSampleArraysMatch(DOUBLE_ARRAY, converted);

    verify(audioFormat).getChannels();
  }

  @Test
  public void testConvertByteArrayThrowsIfArrayIsNull() {
    Exception exception =
        assertThrows(
            SoundException.class,
            () -> {
              AudioUtils.convertByteArrayToDoubleArray(null, 1);
            });

    assertEquals(exception.getMessage(), SoundExceptionKeys.MISSING_AUDIO_DATA.toString());
  }

  @Test
  public void testConvertByteArrayThrowsIfInvalidNumberOfChannels() {
    Exception exception =
        assertThrows(
            SoundException.class,
            () -> {
              AudioUtils.convertByteArrayToDoubleArray(BYTE_ARRAY_MONO, 6);
            });

    assertEquals(exception.getMessage(), SoundExceptionKeys.INVALID_AUDIO_FILE_FORMAT.toString());
  }

  @Test
  public void testConvertByteArrayConvertsCorrectly() throws SoundException {
    double[] converted = AudioUtils.convertByteArrayToDoubleArray(BYTE_ARRAY_MONO, 1);
    assertSampleArraysMatch(DOUBLE_ARRAY, converted);
  }

  @Test
  public void testConvertDoubleArrayThrowsIfArrayIsNull() {
    Exception exception =
        assertThrows(
            SoundException.class,
            () -> {
              AudioUtils.convertDoubleArrayToByteArray(null);
            });

    assertEquals(exception.getMessage(), SoundExceptionKeys.MISSING_AUDIO_DATA.toString());
  }

  @Test
  public void testConvertDoubleArrayConvertsCorrectly() throws SoundException {
    assertArrayEquals(BYTE_ARRAY_MONO, AudioUtils.convertDoubleArrayToByteArray(DOUBLE_ARRAY));
  }

  @Test
  public void testTruncateSamplesTruncatesCorrectly() {
    final double[] samples = new double[5 * 44100]; // 5 second audio
    Arrays.fill(samples, 0.5);

    final double[] expected = new double[3 * 44100];
    Arrays.fill(expected, 0.5);
    assertArrayEquals(expected, AudioUtils.truncateSamples(samples, 3.0));
  }

  @Test
  public void testTruncateSamplesDoesNothingIfAudioShorterThanRequestedLength() {
    final double[] samples = new double[5 * 44100]; // 5 second audio
    Arrays.fill(samples, 0.5);

    assertSame(samples, AudioUtils.truncateSamples(samples, 7.0));
  }

  @Test
  public void testBlendSamplesBlendsCorrectly() {
    final double[] samples = new double[] {0.2, -0.4, 0.7, -0.9};
    // blend starting at index 0
    double[] expected = new double[] {0.4, -0.8, 1.0, -1.0};
    assertSampleArraysMatch(expected, AudioUtils.blendSamples(samples, samples, 0));

    // blend starting at index 1
    expected = new double[] {0.2, -0.2, 0.3, -0.2, -0.9};
    assertSampleArraysMatch(expected, AudioUtils.blendSamples(samples, samples, 1));

    // blend starting at index 2
    expected = new double[] {0.2, -0.4, 0.9, -1.0, 0.7, -0.9};
    assertSampleArraysMatch(expected, AudioUtils.blendSamples(samples, samples, 2));
  }

  // Verify values are correct when rounded
  private void assertSampleArraysMatch(double[] expected, double[] actual) {
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], Math.round(actual[i] * 10) / 10.0);
    }
  }
}
