package org.code.lambda.javabuilder;

import java.util.Scanner;

public class JavaRunner extends Thread {
  public JavaRunner() {}

  public void run() {
    System.out.println("Hello world!");
//    System.out.println("What's your name? ");
//    Scanner in = new Scanner(System.in);
//    String s = in.nextLine();
//    System.out.println("Hello " + s + "!");
//    System.out.println("How");
//    System.out.println("are");
//    System.out.println("you?");
    OutputHandler.sendDebuggingMessage("Done running code");
    OutputSemaphore.signalProcessFinalOutput();
  }
}
