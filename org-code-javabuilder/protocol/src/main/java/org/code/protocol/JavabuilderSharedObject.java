package org.code.protocol;

public abstract class JavabuilderSharedObject {
  // Do any end of code execution clean up. By default this is a no-op.
  public void onExecutionEnded() {}

  // Destroy this object. By default this is a no-op.
  public void destroy() {}
}
