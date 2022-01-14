package org.code.patches;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class PatchClassTester {
  public static void main(String[] args) throws FileNotFoundException {
    PatchClassTester.customInteger();
    PatchClassTester.customSystem();
  }

  public static void customInteger() {
    System.out.println("-- Testing custom mutable integer class: --");
    Integer i = Integer.valueOf(1);
    System.out.println("Original value: " + i);
    i.setValue(10);
    System.out.println("Updated value: " + i);
  }

  public static void customSystem() throws FileNotFoundException {
    System.out.println("-- Testing custom secured System class: --");
    System.setIn(new ByteArrayInputStream(new byte[] {}));
    System.setOut(new PrintStream("file"));
  }
}
