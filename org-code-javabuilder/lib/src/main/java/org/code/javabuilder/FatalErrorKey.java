package org.code.javabuilder;

import static org.code.javabuilder.LambdaErrorCodes.LOW_DISK_SPACE_ERROR_CODE;
import static org.code.javabuilder.LambdaErrorCodes.TEMP_DIRECTORY_CLEANUP_ERROR_CODE;

public enum FatalErrorKey {
  LOW_DISK_SPACE(LOW_DISK_SPACE_ERROR_CODE),
  TEMP_DIRECTORY_CLEANUP_ERROR(TEMP_DIRECTORY_CLEANUP_ERROR_CODE);

  private final int errorCode;

  FatalErrorKey(int errorCode) {
    this.errorCode = errorCode;
  }

  int getErrorCode() {
    return this.errorCode;
  }
}
