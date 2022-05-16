package org.code.javabuilder;

public final class LambdaErrorCodes {
  private LambdaErrorCodes() {
    throw new UnsupportedOperationException("Instantation of constants class not allowed.");
  }

  public static final int TEMP_DIRECTORY_CLEANUP_ERROR_CODE = 10;
  public static final int LOW_DISK_SPACE_ERROR_CODE = 50;
}
