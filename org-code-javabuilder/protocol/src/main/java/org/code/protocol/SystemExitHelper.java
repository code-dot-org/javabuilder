package org.code.protocol;

/**
 * An object that can shut down the JVM in the case of abnormal exit. This allows us to perform any
 * final cleanup steps necessary rather than directly calling System.exit(). Accordingly, it is
 * expected that this method may not return normally.
 */
public interface SystemExitHelper {
  void exit(int status);
}
