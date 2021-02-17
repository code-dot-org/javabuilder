package org.code.codebuilderapp;

/**
 * Logical representation of the output from the user's program. Will be sent to
 * the client as a json object.
 */
public class UserProgramOutput {

  private String output;

  public UserProgramOutput() {}

  public UserProgramOutput(String output) {
    this.output = output;
  }

  public String getOutput() {
    return output;
  }
}
