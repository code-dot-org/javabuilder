package org.code.javabuilder;

import java.util.Scanner;

/**
 * Currently, JavaRunner just runs a sample student code. We'll add more here when we execute
 * arbitrary student code.
 */
public class JavaRunner {
  public void runCode() {
    // Sample program. In the future, student code will be executed here.
    System.out.println("What's your name? ");
    Scanner in = new Scanner(System.in);
    String s = in.nextLine();
    System.out.print("Hello " + s + "!");
    System.out.print("How ");
    System.out.print("are ");
    System.out.print("you? ");
    s = in.nextLine();
    System.out.println("You said: " + s);
  }
}
