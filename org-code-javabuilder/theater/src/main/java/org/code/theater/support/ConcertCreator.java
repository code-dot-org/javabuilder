package org.code.theater.support;

import static org.code.protocol.AllowedFileNames.THEATER_AUDIO_NAME;
import static org.code.protocol.AllowedFileNames.THEATER_IMAGE_NAME;
import static org.code.protocol.ClientMessageDetailKeys.URL;
import static org.code.theater.support.Constants.THEATER_HEIGHT;
import static org.code.theater.support.Constants.THEATER_WIDTH;
import static org.code.theater.support.DrawImageAction.UNSPECIFIED;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import org.code.media.AudioWriter;
import org.code.media.Color;
import org.code.media.FontHelper;
import org.code.protocol.*;
import org.code.theater.Instrument;

/**
 * Creates and publishes a "concert" (the output of a student's Theater project) from a list of
 * {@link SceneAction}s. A concert consists of a GIF file and an audio file. This class implements
 * {@link AutoCloseable} so image and audio resources are always released if an exception is thrown.
 */
public class ConcertCreator implements AutoCloseable {
  private final BufferedImage image;
  private final OutputAdapter outputAdapter;
  private final JavabuilderFileManager fileManager;
  private final GifWriter gifWriter;
  private final ByteArrayOutputStream imageOutputStream;
  private final GraphicsHelper graphicsHelper;
  private final ByteArrayOutputStream audioOutputStream;
  private final AudioWriter audioWriter;
  private final InstrumentSampleLoader instrumentSampleLoader;
  private final TheaterProgressPublisher progressPublisher;
  private boolean hasClosed;

  public ConcertCreator() {
    this(
        new BufferedImage(THEATER_WIDTH, THEATER_HEIGHT, BufferedImage.TYPE_INT_RGB),
        new GifWriter.Factory(),
        new AudioWriter.Factory(),
        new GraphicsHelper.Factory(),
        new InstrumentSampleLoader(),
        new TheaterProgressPublisher(),
        GlobalProtocol.getInstance().getOutputAdapter(),
        GlobalProtocol.getInstance().getFileManager());
  }

  // Visible for testing
  ConcertCreator(
      BufferedImage image,
      GifWriter.Factory gifWriterFactory,
      AudioWriter.Factory audioWriterFactory,
      GraphicsHelper.Factory graphicsHelperFactory,
      InstrumentSampleLoader instrumentSampleLoader,
      TheaterProgressPublisher progressPublisher,
      OutputAdapter outputAdapter,
      JavabuilderFileManager fileManager) {
    this.imageOutputStream = new ByteArrayOutputStream();
    this.audioOutputStream = new ByteArrayOutputStream();

    this.image = image;
    this.gifWriter = gifWriterFactory.createGifWriter(this.imageOutputStream);
    this.audioWriter = audioWriterFactory.createAudioWriter(this.audioOutputStream);
    this.graphicsHelper =
        graphicsHelperFactory.createGraphicsHelper(this.image.createGraphics(), new FontHelper());
    this.instrumentSampleLoader = instrumentSampleLoader;
    this.progressPublisher = progressPublisher;
    this.outputAdapter = outputAdapter;
    this.fileManager = fileManager;
    this.hasClosed = false;

    // set up the image for drawing (set a white background and black stroke/fill)
    this.graphicsHelper.clear(Color.WHITE);

    System.setProperty("java.awt.headless", "true");
  }

  /**
   * Creates the concert content from the provided list of {@link SceneAction}s and publishes the
   * generated file URLs using the {@link OutputAdapter}
   *
   * @param actions from which to create the concert
   */
  public void publishConcert(List<SceneAction> actions) {
    for (SceneAction action : actions) {
      switch (action.getType()) {
        case CLEAR_SCENE:
          this.graphicsHelper.clear(((ClearSceneAction) action).getColor());
          break;
        case PLAY_SOUND:
          this.audioWriter.writeAudioSamples(((PlaySoundAction) action).getSamples());
          break;
        case PLAY_NOTE:
          final PlayNoteAction playNoteAction = (PlayNoteAction) action;
          this.playNote(
              playNoteAction.getInstrument(),
              playNoteAction.getNote(),
              playNoteAction.getSeconds());
          break;
        case PAUSE:
          this.pause(((PauseAction) action).getSeconds());
          break;
        case DRAW_IMAGE:
          final DrawImageAction drawImageAction = (DrawImageAction) action;
          final int width, height;
          if (drawImageAction.getSize() != UNSPECIFIED) {
            // If the "size" value was provided, we need to scale the height proportionally.
            width = drawImageAction.getSize();
            final int imageHeight = drawImageAction.getImage().getHeight();
            final int imageWidth = drawImageAction.getImage().getWidth();
            height = (int) ((double) imageHeight * ((double) width / (double) imageWidth));
          } else {
            width = drawImageAction.getWidth();
            height = drawImageAction.getHeight();
          }
          this.graphicsHelper.drawImage(
              drawImageAction.getImage().getBufferedImage(),
              drawImageAction.getX(),
              drawImageAction.getY(),
              width,
              height,
              drawImageAction.getRotation());
          break;
        case DRAW_TEXT:
          final DrawTextAction drawTextAction = (DrawTextAction) action;
          this.graphicsHelper.drawText(
              drawTextAction.getText(),
              drawTextAction.getX(),
              drawTextAction.getY(),
              drawTextAction.getColor(),
              drawTextAction.getFont(),
              drawTextAction.getFontStyle(),
              drawTextAction.getHeight(),
              drawTextAction.getRotation());
          break;
        case DRAW_LINE:
          final DrawLineAction drawLineAction = (DrawLineAction) action;
          this.graphicsHelper.setStrokeWidth(drawLineAction.getStrokeWidth());
          this.graphicsHelper.drawLine(
              drawLineAction.getColor(),
              drawLineAction.getStartX(),
              drawLineAction.getStartY(),
              drawLineAction.getEndX(),
              drawLineAction.getEndY());
          break;
        case DRAW_POLYGON:
          final DrawPolygonAction drawPolygonAction = (DrawPolygonAction) action;
          this.graphicsHelper.setStrokeWidth(drawPolygonAction.getStrokeWidth());
          this.graphicsHelper.drawRegularPolygon(
              drawPolygonAction.getX(),
              drawPolygonAction.getY(),
              drawPolygonAction.getSides(),
              drawPolygonAction.getRadius(),
              drawPolygonAction.getStrokeColor(),
              drawPolygonAction.getFillColor());
          break;
        case DRAW_SHAPE:
          final DrawShapeAction drawShapeAction = (DrawShapeAction) action;
          this.graphicsHelper.setStrokeWidth(drawShapeAction.getStrokeWidth());
          this.graphicsHelper.drawShape(
              drawShapeAction.getPoints(),
              drawShapeAction.isClosed(),
              drawShapeAction.getStrokeColor(),
              drawShapeAction.getFillColor());
          break;
        case DRAW_ELLIPSE:
          final DrawEllipseAction drawEllipseAction = (DrawEllipseAction) action;
          this.graphicsHelper.setStrokeWidth(drawEllipseAction.getStrokeWidth());
          this.graphicsHelper.drawEllipse(
              drawEllipseAction.getX(),
              drawEllipseAction.getY(),
              drawEllipseAction.getWidth(),
              drawEllipseAction.getHeight(),
              drawEllipseAction.getStrokeColor(),
              drawEllipseAction.getFillColor());
          break;
        case DRAW_RECTANGLE:
          final DrawRectangleAction drawRectangleAction = (DrawRectangleAction) action;
          this.graphicsHelper.setStrokeWidth(drawRectangleAction.getStrokeWidth());
          this.graphicsHelper.drawRectangle(
              drawRectangleAction.getX(),
              drawRectangleAction.getY(),
              drawRectangleAction.getWidth(),
              drawRectangleAction.getHeight(),
              drawRectangleAction.getStrokeColor(),
              drawRectangleAction.getFillColor());
          break;
        default:
          break;
      }
    }

    this.writeImageAndAudioToFile();
  }

  private void playNote(Instrument instrument, int note, double noteLength) {
    final String sampleFilePath = instrumentSampleLoader.getSampleFilePath(instrument, note);
    if (sampleFilePath == null) {
      return;
    }

    try {
      this.audioWriter.writeAudioFromLocalFile(sampleFilePath, noteLength);
    } catch (FileNotFoundException e) {
      System.out.printf("Could not play instrument: %s at note: %s%n", instrument, note);
    }
  }

  private void pause(double seconds) {
    this.gifWriter.writeToGif(this.image, (int) (Math.max(seconds, 0.1) * 1000));
    this.audioWriter.addDelay(Math.max(seconds, 0.1));
    this.progressPublisher.onPause(seconds);
  }

  private void writeImageAndAudioToFile() {
    this.progressPublisher.onPlay(this.audioWriter.getTotalAudioLength());
    this.gifWriter.writeToGif(this.image, 0);
    this.audioWriter.writeToAudioStream();

    // We must call close() before write so that the streams are flushed.
    this.close();

    try {
      String imageUrl =
          this.fileManager.writeToFile(
              THEATER_IMAGE_NAME, this.imageOutputStream.toByteArray(), "image/gif");
      String audioUrl =
          this.fileManager.writeToFile(
              THEATER_AUDIO_NAME, this.audioOutputStream.toByteArray(), "audio/wav");

      HashMap<String, String> imageMessage = new HashMap<>();
      imageMessage.put(URL, imageUrl);
      this.outputAdapter.sendMessage(new TheaterMessage(TheaterSignalKey.VISUAL_URL, imageMessage));

      HashMap<String, String> audioMessage = new HashMap<>();
      audioMessage.put(URL, audioUrl);
      this.outputAdapter.sendMessage(new TheaterMessage(TheaterSignalKey.AUDIO_URL, audioMessage));
    } catch (JavabuilderException e) {
      // we should not hit this (caused by too many file writes)
      // in normal execution as it is only called via play,
      // and play can only be called once.
      throw new InternalServerRuntimeError(InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION, e);
    }
  }

  @Override
  public void close() {
    if (!this.hasClosed) {
      System.out.println("Closing ConcertCreator!");
      this.gifWriter.close();
      this.audioWriter.close();
      this.hasClosed = true;
    }
  }
}
