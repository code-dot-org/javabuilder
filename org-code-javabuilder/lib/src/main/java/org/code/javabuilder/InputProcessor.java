package org.code.javabuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class InputProcessor implements Runnable {

  InputStream inputStream;
  OutputStream outputStream;

  public InputProcessor(InputStream inputStream, OutputStream outputStream) {
    this.inputStream = inputStream;
    this.outputStream = outputStream;
  }

  @Override
  public void run() {
    Scanner inputScanner = new Scanner(this.inputStream);
    while (!Thread.interrupted()) {
      while (inputScanner.hasNextLine()) {
        String nextInput = inputScanner.nextLine() + "\n";
        try {
          this.outputStream.write(nextInput.getBytes(StandardCharsets.UTF_8));
          this.outputStream.flush();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        return;
      }
    }
  }
}
