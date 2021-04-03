package org.code.javabuilder;

import java.net.URI;
import javax.tools.SimpleJavaFileObject;

// GOOD?
/** A file object used to represent source coming from a string. */
public class JavaSourceFromString extends SimpleJavaFileObject {
  /** The source code of this "file". */
  final String code;

  /**
   * Constructs a new JavaSourceFromString.
   *
   * @param name the name of the compilation unit represented by this file object
   * @param code the source code for the compilation unit represented by this file object
   */
  JavaSourceFromString(String name, String code) {
    super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
    this.code = code;
  }

  @Override
  public CharSequence getCharContent(boolean ignoreEncodingErrors) {
    return code;
  }
}
