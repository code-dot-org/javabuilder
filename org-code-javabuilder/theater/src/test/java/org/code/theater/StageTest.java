package org.code.theater;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import org.code.media.AudioWriter;
import org.code.media.Image;
import org.code.protocol.GlobalProtocol;
import org.code.protocol.InputAdapter;
import org.code.protocol.JavabuilderFileWriter;
import org.code.protocol.OutputAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class StageTest {
  private final OutputAdapter outputAdapter = mock(OutputAdapter.class);
  private final PrintStream standardOut = System.out;
  private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
  private BufferedImage bufferedImage;
  private Graphics2D graphics;
  private AudioWriter.Factory audioWriterFactory;
  private AudioWriter audioWriter;
  private InstrumentSampleLoader instrumentSampleLoader;

  private Stage s;

  @BeforeEach
  public void setUp() {
    GlobalProtocol.create(
        outputAdapter, mock(InputAdapter.class), "", "", mock(JavabuilderFileWriter.class));
    System.setOut(new PrintStream(outputStreamCaptor));
    bufferedImage = mock(BufferedImage.class);
    graphics = mock(Graphics2D.class);
    when(bufferedImage.createGraphics()).thenReturn(graphics);
    audioWriterFactory = mock(AudioWriter.Factory.class);
    audioWriter = mock(AudioWriter.class);
    when(audioWriterFactory.createAudioWriter(any(ByteArrayOutputStream.class)))
        .thenReturn(audioWriter);
    instrumentSampleLoader = mock(InstrumentSampleLoader.class);

    s = new Stage(bufferedImage, audioWriterFactory, instrumentSampleLoader);
  }

  @AfterEach
  public void tearDown() {
    System.setOut(standardOut);
  }

  @Test
  void drawLineDefaultsToBlack() {
    s.removeStrokeColor();
    s.drawLine(0, 0, 100, 100);
    verify(graphics).setColor(Color.BLACK);
    verify(graphics).drawLine(0, 0, 100, 100);
  }

  @Test
  void removingStrokeAndFillPreventsShapeDrawing() {
    s.removeFillColor();
    s.removeStrokeColor();
    s.drawRegularPolygon(0, 0, 5, 100);
    verify(graphics, never()).drawPolygon(any());
    verify(graphics, never()).fillPolygon(any());
  }

  @Test
  void removingStrokeStillCallsFill() {
    s.removeStrokeColor();
    s.drawEllipse(5, 5, 100, 100);
    verify(graphics, never()).drawOval(5, 5, 100, 100);
    verify(graphics).fillOval(5, 5, 100, 100);
  }

  @Test
  void drawOpenShapeDrawsLines() {
    s.drawShape(new int[] {0, 0, 25, 30, 0, 100}, false);
    verify(graphics, Mockito.times(2)).drawLine(anyInt(), anyInt(), anyInt(), anyInt());
  }

  @Test
  void drawClosedShapeDrawsAndFillsShape() {
    s.drawShape(new int[] {0, 0, 25, 30, 0, 100}, true);
    int[] xPoints = new int[] {0, 25, 0};
    int[] yPoints = new int[] {0, 30, 100};
    verify(graphics).drawPolygon(xPoints, yPoints, 3);
    verify(graphics).fillPolygon(xPoints, yPoints, 3);
  }

  @Test
  void throwsExceptionIfPlayCalledTwice() {
    Stage s = new Stage();
    s.play();
    Exception exception =
        assertThrows(
            TheaterRuntimeException.class,
            () -> {
              s.play();
            });
    String expectedMessage = ExceptionKeys.DUPLICATE_PLAY_COMMAND.toString();
    assertEquals(exception.getMessage(), expectedMessage);
  }

  @Test
  void throwsExceptionOnInvalidShape() {
    Stage s = new Stage();
    int[] tooFewPoints = new int[] {1, 0};
    int[] oddNumberOfPoints = new int[] {0, 0, 1, 1, 2};
    verifyInvalidShapeThrowsException(s, tooFewPoints, false);
    verifyInvalidShapeThrowsException(s, oddNumberOfPoints, true);
  }

  @Test
  void testPlaySoundWithArrayCallsAudioWriter() {
    final double[] testSamples = {1.0, -1.0, 0.0};
    s.playSound(testSamples);
    verify(audioWriter).writeAudioSamples(testSamples);
  }

  @Test
  void testPlaySoundWithFilenameCallsAudioWriter() throws FileNotFoundException {
    final String testFile = "test.wav";
    s.playSound(testFile);
    verify(audioWriter).writeAudioFromAssetFile(testFile);
  }

  @Test
  void testPlayNoteDoesNothingWhenFileNotFound() throws FileNotFoundException {
    when(instrumentSampleLoader.getSampleFilePath(any(Instrument.class), anyInt()))
        .thenReturn(null);

    s.playNote(Instrument.PIANO, 60, 1);

    verify(instrumentSampleLoader).getSampleFilePath(Instrument.PIANO, 60);
    verify(audioWriter, never()).writeAudioFromLocalFile(anyString(), anyDouble());
  }

  @Test
  void testPlayNoteCallsAudioWriterIfFileExists() throws FileNotFoundException {
    final String testSampleFile = "test.wav";
    final int note = 60;
    final double seconds = 2.0;
    when(instrumentSampleLoader.getSampleFilePath(any(Instrument.class), eq(note)))
        .thenReturn(testSampleFile);

    s.playNote(Instrument.PIANO, note, seconds);

    verify(instrumentSampleLoader).getSampleFilePath(Instrument.PIANO, note);
    verify(audioWriter).writeAudioFromLocalFile(testSampleFile, seconds);
  }

  @Test
  void drawImageWithRotationCreatesTransform() {
    Image testImage = new Image(200, 300);
    s.drawImage(testImage, 0, 300, 50, 75, 90);
    verify(graphics).drawImage(any(BufferedImage.class), any(AffineTransform.class), any());
  }

  @Test
  void drawImageWithoutRotationDoesNotCreateTransform() {
    Image testImage = new Image(200, 300);
    s.drawImage(testImage, 0, 300, 50, 75, 0);
    verify(graphics)
        .drawImage(any(BufferedImage.class), anyInt(), anyInt(), anyInt(), anyInt(), any());
  }

  private void verifyInvalidShapeThrowsException(Stage s, int[] points, boolean close) {
    Exception exception =
        assertThrows(
            TheaterRuntimeException.class,
            () -> {
              s.drawShape(points, close);
            });
    String expectedMessage = ExceptionKeys.INVALID_SHAPE.toString();
    assertEquals(exception.getMessage(), expectedMessage);
  }
}
