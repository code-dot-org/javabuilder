package org.code.javabuilder;

import static org.code.javabuilder.LambdaErrorCodes.*;

public enum FatalErrorKey {
  LOW_DISK_SPACE(LOW_DISK_SPACE_ERROR_CODE),
  TEMP_DIRECTORY_CLEANUP_ERROR(TEMP_DIRECTORY_CLEANUP_ERROR_CODE),
  CONNECTION_POOL_SHUT_DOWN(CONNECTION_POOL_SHUT_DOWN_ERROR_CODE);

  private final int errorCode;

  FatalErrorKey(int errorCode) {
    this.errorCode = errorCode;
  }

  int getErrorCode() {
    return this.errorCode;
  }
}
