package org.code.javabuilder;

import java.util.Scanner;

public class JavaRunner extends Thread {
  public void run() {
    // Sample program. In the future, student code will be executed here.
    System.out.println("What's your name? ");
    Scanner in = new Scanner(System.in);
    String l = in.next();
    String s = in.nextLine();
    System.out.println("Hello " + l + "!");
    System.out.println("How");
    System.out.println("are");
    System.out.print("you? ");
    s = in.nextLine();
    System.out.println("You are " + s);
  }
}
