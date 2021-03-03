package org.code.javabuilder;

/** Logical representation of the commandline input received as a json object from the client. */
public class UserInput {
  private String input;

  public UserInput() {}

  public UserInput(String input) {
    this.input = input;
  }

  public String getInput() {
    return this.input;
  }

  public void setInput(String input) {
    this.input = input;
  }
}
