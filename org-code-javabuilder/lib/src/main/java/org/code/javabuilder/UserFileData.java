package org.code.javabuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserFileData {
  private String text;
  private boolean isVisible;

  public String getText() {
    return this.text;
  }

  public boolean getVisible() {
    return this.isVisible;
  }

  public void setText(String text) {
    this.text = text;
  }

  public void setVisible(boolean visible) {
    this.isVisible = visible;
  }
}
