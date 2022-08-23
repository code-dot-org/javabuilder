package org.code.javabuilder;

public final class LambdaErrorCodes {
  private LambdaErrorCodes() {
    throw new UnsupportedOperationException("Instantation of constants class not allowed.");
  }

  public static final int TEMP_DIRECTORY_CLEANUP_ERROR_CODE = 10;
  public static final int LOW_DISK_SPACE_ERROR_CODE = 50;
  public static final int OUT_OF_MEMORY_ERROR_CODE = 60;
  public static final int UNHEALTHY_CONTAINER_ERROR_CODE = 70;
  public static final int CONNECTION_POOL_SHUT_DOWN_ERROR_CODE = 70;
}
