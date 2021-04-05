package org.code.javabuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class ProjectLoader extends Thread {
  private final ProjectFile projectFile;
  private final String fileUrl;

  public ProjectLoader(ProjectFile projectFile, String fileUrl) {
    this.projectFile = projectFile;
    this.fileUrl = fileUrl;
  }

  // GOOD?
  public void run() {
    URL url = null;
    try {
      url = new URL(fileUrl);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    try {
      assert url != null;
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      try {
        con.setRequestMethod("GET");
      } catch (ProtocolException e) {
        e.printStackTrace();
      }
      int status = con.getResponseCode();
      Reader streamReader = null;
      if (status > 299) {
        streamReader = new InputStreamReader(con.getErrorStream());
      } else {
        streamReader = new InputStreamReader(con.getInputStream());
      }
      BufferedReader in = new BufferedReader(streamReader);
      String inputLine;
      StringBuffer content = new StringBuffer();
      while ((inputLine = in.readLine()) != null) {
        content.append(inputLine);
      }
      in.close();
      con.disconnect();
      projectFile.setCode(content.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
