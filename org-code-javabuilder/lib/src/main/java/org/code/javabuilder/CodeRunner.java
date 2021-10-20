package org.code.javabuilder;

import java.net.URLClassLoader;
import org.code.protocol.JavabuilderException;

public interface CodeRunner {
  void run(URLClassLoader urlClassLoader) throws JavabuilderException;
}
