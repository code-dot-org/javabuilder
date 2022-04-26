package org.code.theater.support;

import static org.code.protocol.AllowedFileNames.THEATER_AUDIO_NAME;
import static org.code.protocol.AllowedFileNames.THEATER_IMAGE_NAME;
import static org.code.protocol.ClientMessageDetailKeys.URL;
import static org.code.theater.support.DrawImageAction.UNSPECIFIED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.util.List;
import org.code.media.*;
import org.code.media.support.AudioWriter;
import org.code.protocol.*;
import org.code.theater.Instrument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ConcertCreatorTest {

  private BufferedImage image;
  private GifWriter gifWriter;
  private AudioWriter audioWriter;
  private GraphicsHelper graphicsHelper;
  private InstrumentSampleLoader instrumentSampleLoader;
  private TheaterProgressPublisher theaterProgressPublisher;
  private OutputAdapter outputAdapter;
  private ContentManager contentManager;
  private ArgumentCaptor<TheaterMessage> theaterMessageCaptor;
  private ConcertCreator unitUnderTest;

  @BeforeEach
  public void setUp() {
    CachedResources.create();
    image = mock(BufferedImage.class);
    gifWriter = mock(GifWriter.class);
    audioWriter = mock(AudioWriter.class);
    graphicsHelper = mock(GraphicsHelper.class);
    instrumentSampleLoader = mock(InstrumentSampleLoader.class);
    theaterProgressPublisher = mock(TheaterProgressPublisher.class);
    outputAdapter = mock(OutputAdapter.class);
    contentManager = mock(ContentManager.class);
    theaterMessageCaptor = ArgumentCaptor.forClass(TheaterMessage.class);

    final GifWriter.Factory gifWriterFactory = mock(GifWriter.Factory.class);
    when(gifWriterFactory.createGifWriter(any())).thenReturn(gifWriter);
    final AudioWriter.Factory audioWriterFactory = mock(AudioWriter.Factory.class);
    when(audioWriterFactory.createAudioWriter(any())).thenReturn(audioWriter);
    final GraphicsHelper.Factory graphicsHelperFactory = mock(GraphicsHelper.Factory.class);
    when(graphicsHelperFactory.createGraphicsHelper(any(), any())).thenReturn(graphicsHelper);

    unitUnderTest =
        new ConcertCreator(
            image,
            gifWriterFactory,
            audioWriterFactory,
            graphicsHelperFactory,
            instrumentSampleLoader,
            theaterProgressPublisher,
            outputAdapter,
            contentManager);
  }

  @Test
  public void testClearScene() {
    final ClearSceneAction action = new ClearSceneAction(Color.BEIGE);
    unitUnderTest.publishConcert(List.of(action));
    verify(graphicsHelper).clear(action.getColor());
  }

  @Test
  public void testPlaySound() {
    final double[] samples = {1.0, 0.0, 1.0, 0.0};
    final PlaySoundAction action = new PlaySoundAction(samples);
    unitUnderTest.publishConcert(List.of(action));
    verify(audioWriter).writeAudioSamples(samples);
  }

  @Test
  public void testPlayNoteDoesNothingWhenFileNotFound() throws FileNotFoundException {
    when(instrumentSampleLoader.getSampleFilePath(any(Instrument.class), anyInt()))
        .thenReturn(null);

    final PlayNoteAction action = new PlayNoteAction(Instrument.PIANO, 60, 1);
    unitUnderTest.publishConcert(List.of(action));

    verify(instrumentSampleLoader).getSampleFilePath(Instrument.PIANO, 60);
    verify(audioWriter, never()).writeAudioFromLocalFile(anyString(), anyDouble());
  }

  @Test
  public void testPlayNoteCallsAudioWriterIfFileExists() throws FileNotFoundException {
    final String testSampleFile = "test.wav";
    final int note = 60;
    final double seconds = 2.0;
    when(instrumentSampleLoader.getSampleFilePath(any(Instrument.class), eq(note)))
        .thenReturn(testSampleFile);

    final PlayNoteAction action = new PlayNoteAction(Instrument.PIANO, note, seconds);
    unitUnderTest.publishConcert(List.of(action));

    verify(instrumentSampleLoader).getSampleFilePath(Instrument.PIANO, note);
    verify(audioWriter).writeAudioFromLocalFile(testSampleFile, seconds);
    verify(audioWriter, never()).addDelay(anyDouble());
  }

  @Test
  public void testPause() {
    final double pauseTime = 15.0;
    final PauseAction action = new PauseAction(pauseTime);
    unitUnderTest.publishConcert(List.of(action));

    verify(gifWriter).writeToGif(image, (int) pauseTime * 1000);
    verify(audioWriter).addDelay(pauseTime);
    verify(theaterProgressPublisher).onPause(pauseTime);
  }

  @Test
  public void testDrawImageWithWidthAndHeight() {
    final BufferedImage bufferedImage = mock(BufferedImage.class);
    final Image image = mock(Image.class);
    when(image.getBufferedImage()).thenReturn(bufferedImage);
    final DrawImageAction action = new DrawImageAction(image, 10, 10, UNSPECIFIED, 100, 100, 45.0);

    unitUnderTest.publishConcert(List.of(action));

    verify(graphicsHelper)
        .drawImage(
            bufferedImage,
            action.getX(),
            action.getY(),
            action.getWidth(),
            action.getHeight(),
            action.getRotation());
  }

  @Test
  public void testDrawImageWithSize() {
    final BufferedImage bufferedImage = mock(BufferedImage.class);
    final Image image = mock(Image.class);
    when(image.getBufferedImage()).thenReturn(bufferedImage);
    final int originalWidth = 100;
    final int originalHeight = 200;
    final int desiredSize = 50;
    final int expectedHeight = 100; // Height should scale based on size
    when(image.getWidth()).thenReturn(originalWidth);
    when(image.getHeight()).thenReturn(originalHeight);

    final DrawImageAction action =
        new DrawImageAction(image, 10, 10, desiredSize, UNSPECIFIED, UNSPECIFIED, 90.0);

    unitUnderTest.publishConcert(List.of(action));

    verify(graphicsHelper)
        .drawImage(
            bufferedImage,
            action.getX(),
            action.getY(),
            desiredSize,
            expectedHeight,
            action.getRotation());
  }

  @Test
  public void testDrawText() {
    final DrawTextAction action =
        new DrawTextAction("text", 50, 50, 100.0, 100, Font.SERIF, FontStyle.ITALIC, Color.BLUE);
    unitUnderTest.publishConcert(List.of(action));
    verify(graphicsHelper)
        .drawText(
            action.getText(),
            action.getX(),
            action.getY(),
            action.getColor(),
            action.getFont(),
            action.getFontStyle(),
            action.getHeight(),
            action.getRotation());
  }

  @Test
  public void testDrawPolygon() {
    final DrawPolygonAction action =
        new DrawPolygonAction(10, 10, 5, 100, Color.RED, Color.GREEN, 5.0);
    unitUnderTest.publishConcert(List.of(action));
    verify(graphicsHelper).setStrokeWidth(action.getStrokeWidth());
    verify(graphicsHelper)
        .drawRegularPolygon(
            action.getX(),
            action.getY(),
            action.getSides(),
            action.getRadius(),
            action.getStrokeColor(),
            action.getFillColor());
  }

  @Test
  public void testDrawShape() {
    final int[] points = {1, 1, 10, 10};
    final DrawShapeAction action = new DrawShapeAction(points, false, Color.IVORY, Color.LIME, 1.0);
    unitUnderTest.publishConcert(List.of(action));
    verify(graphicsHelper).setStrokeWidth(action.getStrokeWidth());
    verify(graphicsHelper)
        .drawShape(
            action.getPoints(), action.isClosed(), action.getStrokeColor(), action.getFillColor());
  }

  @Test
  public void testDrawEllipse() {
    final DrawEllipseAction action =
        new DrawEllipseAction(20, 20, 100, 100, Color.CHOCOLATE, Color.INDIGO, 10.0);
    unitUnderTest.publishConcert(List.of(action));
    verify(graphicsHelper).setStrokeWidth(action.getStrokeWidth());
    verify(graphicsHelper)
        .drawEllipse(
            action.getX(),
            action.getY(),
            action.getWidth(),
            action.getHeight(),
            action.getStrokeColor(),
            action.getFillColor());
  }

  @Test
  public void testDrawRectangle() {
    final DrawRectangleAction action =
        new DrawRectangleAction(20, 20, 100, 100, Color.CHOCOLATE, Color.INDIGO, 10.0);
    unitUnderTest.publishConcert(List.of(action));
    verify(graphicsHelper).setStrokeWidth(action.getStrokeWidth());
    verify(graphicsHelper)
        .drawRectangle(
            action.getX(),
            action.getY(),
            action.getWidth(),
            action.getHeight(),
            action.getStrokeColor(),
            action.getFillColor());
  }

  @Test
  public void testWritesOutputFilesAndClosesStreams() throws JavabuilderException {
    final double length = 10.0;
    when(audioWriter.getTotalAudioLength()).thenReturn(length);

    final String imageUrl = "imageUrl";
    final String audioUrl = "audioUrl";
    when(contentManager.writeToOutputFile(eq(THEATER_IMAGE_NAME), any(), any()))
        .thenReturn(imageUrl);
    when(contentManager.writeToOutputFile(eq(THEATER_AUDIO_NAME), any(), any()))
        .thenReturn(audioUrl);
    doNothing().when(outputAdapter).sendMessage(theaterMessageCaptor.capture());

    unitUnderTest.publishConcert(List.of());

    verify(theaterProgressPublisher).onPlay(length);
    verify(gifWriter).writeToGif(image, 0);
    verify(audioWriter).writeToAudioStream();
    verify(gifWriter).close();
    verify(audioWriter).close();

    final TheaterMessage imageMessage = theaterMessageCaptor.getAllValues().get(0);
    assertEquals(imageUrl, imageMessage.getDetail().get(URL));

    final TheaterMessage audioMessage = theaterMessageCaptor.getAllValues().get(1);
    assertEquals(audioUrl, audioMessage.getDetail().get(URL));
  }

  @Test
  public void testDoesNotCloseTwice() {
    unitUnderTest.publishConcert(List.of());
    unitUnderTest.close();

    // Should have only closed once
    verify(gifWriter, times(1)).close();
    verify(audioWriter, times(1)).close();
  }
}
