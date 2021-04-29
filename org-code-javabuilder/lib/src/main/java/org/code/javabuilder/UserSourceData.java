package org.code.javabuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSourceData {
  private Map<String, UserFileData> source;

  public Map<String, UserFileData> getSource() {
    return this.source;
  }

  public void setSource(Map<String, UserFileData> source) {
    this.source = source;
  }
}
