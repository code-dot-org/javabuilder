package org.code.protocol;

import java.io.InputStream;

public interface FileWriter {
  String writeToFile(String filename, InputStream input) throws JavabuilderException;
}
