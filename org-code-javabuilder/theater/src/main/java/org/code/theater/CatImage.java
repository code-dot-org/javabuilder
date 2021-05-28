package org.code.theater;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import org.apache.commons.io.FileUtils;
import org.code.protocol.GlobalProtocol;
import org.code.protocol.OutputAdapter;

import javax.imageio.ImageIO;
import javax.sound.midi.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

public class CatImage {
  private final OutputAdapter outputAdapter;
  public CatImage() {
    this.outputAdapter = GlobalProtocol.getInstance().getOutputAdapter();
  }
  public CatImage(OutputAdapter outputAdapter) {
    this.outputAdapter = outputAdapter;
  }


  Synthesizer synth;
  Soundbank soundbank;
  MidiChannel[] channels;
  MidiChannel channel;

  public void playSound() {
    // Next up: Look into this: https://docs.oracle.com/javase/tutorial/sound/SPI-providing-MIDI.html
    try {
      synth = MidiSystem.getSynthesizer();
      synth.open();
      soundbank = synth.getDefaultSoundbank();
      if (soundbank != null) {
        synth.loadAllInstruments(soundbank);
      }
      channels = synth.getChannels();
      channel = channels[0];
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    int[] songNotes = new int[]{86, 85, 88, 91};

    playNote(songNotes[0], 1000);
    rest(500);

    playNote(songNotes[0], 300);
    playNote(songNotes[1], 300);
    playNote(songNotes[0], 1000);
    rest(500);

    playNote(songNotes[0], 300);
    playNote(songNotes[1], 300);
    playNote(songNotes[0], 1000);
    playNote(songNotes[2], 300);
    playNote(songNotes[2], 300);
    playNote(songNotes[3], 300);
    playNote(songNotes[3], 600);

    songNotes = new int[]{43, 46, 48, 41, 42};
    playNote(songNotes[0], 200);
    playNote(songNotes[0], 500);
    playNote(songNotes[1], 200);
    playNote(songNotes[2], 200);
    rest(200);

    playNote(songNotes[0], 200);
    playNote(songNotes[0], 500);
    playNote(songNotes[3], 200);
    playNote(songNotes[4], 200);
    rest(200);

    int[] verse1 = {60, 67, 69};
    int[] verse2 = {65, 64, 62, 60};

    playNote(verse1[0], 400);
    playNote(verse1[0], 400);
    playNote(verse1[1], 400);
    playNote(verse1[1], 400);
    playNote(verse1[2], 400);
    playNote(verse1[2], 400);
    playNote(verse1[1], 800);
    rest(200);

    playNote(verse2[0], 400);
    playNote(verse2[0], 400);
    playNote(verse2[1], 400);
    playNote(verse2[1], 400);
    playNote(verse2[2], 400);
    playNote(verse2[2], 400);
    playNote(verse2[3], 800);
    rest(200);
  }

  public void playNote(int note, int duration) {
    playNote(note, duration, 100);
  }

  public void playNote(int note, int duration, int intensity) {
    try {
      channel.noteOn(note, intensity);
      Thread.sleep(duration);
      channel.noteOff(note, intensity);
    } catch (InterruptedException e) {}
  }

  public void rest(int duration) {
    try {
      Thread.sleep(duration);
    } catch (InterruptedException e) {}
  }

  public void buildImageFilter(Boolean filter) {
    ImagePlus image = IJ.openImage("C:\\Users\\jmkul\\Downloads\\placeimg_300_150_animals.jpg");
    ImageProcessor ip = image.getProcessor();
    if (filter) {
      int count = ip.getPixelCount();
      int totalWidth = ip.getWidth();
      int totalHeight = ip.getHeight();
      for (int width = 0; width < totalWidth; width++) {
        for (int height = 0; height < totalHeight; height++) {
          int pixel = ip.getPixel(width, height);

          ip.putPixel(width, height, pixel - 300);
        }
      }
    }

    BufferedImage bufferedImage = (BufferedImage)ip.createImage();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      ImageIO.write(bufferedImage, "png", out);
    } catch (IOException e) {
      e.printStackTrace();
    }
    String encodedString = Base64.getEncoder().encodeToString(out.toByteArray());

//    bufferImage = clock.millis();
    HashMap<String, String> message = new HashMap<>();
    message.put("image", encodedString);
//    outputAdapter.sendBinaryMessage(ByteBuffer.wrap(out.toByteArray()));
    outputAdapter.sendMessage(new TheaterMessage(TheaterSignalKey.DEFAULT, message));

    // Original: 290KB
    // PNG: 2258KB
//    IJ.save(image, "C:\\Users\\jmkul\\Downloads\\cat-new-1.png");
//    // JPG: 302KB
//    IJ.save(image, "C:\\Users\\jmkul\\Downloads\\cat-new-1.jpeg");
    IJ.save(image, "C:\\Users\\jmkul\\Downloads\\cat-new-1.jpg");
    // TIFF & BMP: 2346KB
//    IJ.save(image, "C:\\Users\\jmkul\\Downloads\\cat-new-1.tiff");
//    IJ.save(image, "C:\\Users\\jmkul\\Downloads\\cat-new-1.bmp");
//    // GIF: 440KB
//    IJ.save(image, "C:\\Users\\jmkul\\Downloads\\cat-new-1.gif");

//    ip.pix
//    ByteBuffer buffer = ByteBuffer.wrap(ip.getBufferedImage());
  }

  public void buildCanvas() {
    Clock clock = Clock.systemDefaultZone();
    long start;
    long drewImage;
    long builtImage;
    long bufferImage;
    long sentImage;
    start = clock.millis();
    ImagePlus image = IJ.createImage("test", 300, 150, 1, 24);

    ImageProcessor ip = image.getProcessor();
    ip.setColor(Color.BLUE);
    ip.setLineWidth(10);
//    System.out.println(ip.getPixel(200,200));
    ip.fillRect(50, 50, 100, /*image.getHeight() - */ 100);
    int[] x = {10, 40, 25};
    int[] y = {10, 10, 30};
    ip.setColor(Color.RED);
    ip.fillPolygon(new Polygon(x, y, 3));
    ip.setColor(Color.GREEN);
    ip.fillOval(30, 20, 50, 30);
    ip.setFontSize(30);
    ip.setColor(Color.YELLOW);
    ip.drawString("Hello World!!!", 100, 90);
//    image.getFileInfo();
    drewImage = clock.millis();

//    IJ.save(image, "C:\\Users\\jmkul\\Downloads\\shapes.png");
    builtImage = clock.millis();
//    String encodedString;
//    try {
//      encodedString = Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(new File("C:\\Users\\jmkul\\Downloads\\shapes.png")));
//    } catch (IOException e) {
//      System.out.println("caught an error");
//      throw new RuntimeException(e);
//    }

    BufferedImage bufferedImage = (BufferedImage)ip.createImage();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      ImageIO.write(bufferedImage, "png", out);
    } catch (IOException e) {
      e.printStackTrace();
    }
    String encodedString = Base64.getEncoder().encodeToString(out.toByteArray());

    bufferImage = clock.millis();
    HashMap<String, String> message = new HashMap<>();
    message.put("image", encodedString);
//    outputAdapter.sendBinaryMessage(ByteBuffer.wrap(out.toByteArray()));
    outputAdapter.sendMessage(new TheaterMessage(TheaterSignalKey.DEFAULT, message));
//    try {
//      out.close();
//    } catch (IOException e) {
//      e.printStackTrace();
//    }

    sentImage = clock.millis();
    System.out.println(drewImage - start);
    System.out.println(builtImage - drewImage);
    System.out.println(bufferImage - builtImage);
    System.out.println(bufferImage);
    System.out.println(sentImage - bufferImage);

//    image.show();
  }
}
