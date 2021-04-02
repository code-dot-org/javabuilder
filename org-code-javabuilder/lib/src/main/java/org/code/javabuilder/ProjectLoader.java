package org.code.javabuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

public class ProjectLoader {
  private final String channelId;
  private final ArrayList<String> fileList;
  public ProjectLoader(String channelId, Array fileList) {
    this.channelId = channelId;
    this.fileList = new ArrayList<>();
    this.fileList.add("MyClass.java");
  }

  public void loadFiles() throws MalformedURLException, ProtocolException, IOException {
    URL url = new URL("https://studio.code.org/v3/files/UVXkRDHwYNbXPZTQXPzNJ1C8Oyv1ZCCA5O6M2a-fs1E/MyClass.java");
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");
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
  }
}
