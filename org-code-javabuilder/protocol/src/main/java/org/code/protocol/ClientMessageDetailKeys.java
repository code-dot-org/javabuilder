package org.code.protocol;

/** Expected keys in the optional detail object of {@link ClientMessage}s */
public enum ClientMessageDetailKeys {
  FILENAME("filename");

  private final String name;

  ClientMessageDetailKeys(String name) {
    this.name = name;
  }

  public String toString() {
    return this.name;
  }
}
