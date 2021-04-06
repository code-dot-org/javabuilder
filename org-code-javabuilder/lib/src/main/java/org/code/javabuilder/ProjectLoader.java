package org.code.javabuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

public class ProjectLoader implements Callable<Boolean> {
  private final ProjectFile projectFile;
  private final String fileUrl;

  public ProjectLoader(ProjectFile projectFile, String fileUrl) {
    this.projectFile = projectFile;
    this.fileUrl = fileUrl;
  }

  @Override
  public Boolean call() throws UserFacingException {
    try {
      URL url = new URL(fileUrl);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");
      int status = con.getResponseCode();
      Reader streamReader;
      if (status > 299) {
        streamReader = new InputStreamReader(con.getErrorStream());
      } else {
        streamReader = new InputStreamReader(con.getInputStream());
      }
      BufferedReader in = new BufferedReader(streamReader);
      StringBuilder content = new StringBuilder();
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        content.append(inputLine);
      }
      in.close();
      con.disconnect();
      if (status > 299) {
        throw new UserFacingException(
            "We hit an error on our side while loading your files. Try again. \n"
                + content.toString());
      }
      projectFile.setCode(content.toString());
      return true;
    } catch (IOException e) {
      throw new UserFacingException(
          "We hit an error on our side while loading your files. Try again.");
    }
  }
}
