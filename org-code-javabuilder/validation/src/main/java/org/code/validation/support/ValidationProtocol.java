package org.code.validation.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import org.code.protocol.ClientMessage;
import org.code.protocol.JavabuilderSharedObject;
import org.code.validation.NeighborhoodLog;

public class ValidationProtocol extends JavabuilderSharedObject {
  private final Method mainMethod;
  private final NeighborhoodTracker neighborhoodTracker;
  private final SystemOutTracker systemOutTracker;

  public ValidationProtocol(
      Method mainMethod,
      NeighborhoodTracker neighborhoodTracker,
      SystemOutTracker systemOutTracker) {
    this.mainMethod = mainMethod;
    this.neighborhoodTracker = neighborhoodTracker;
    this.systemOutTracker = systemOutTracker;
  }

  public NeighborhoodLog getNeighborhoodLog() {
    return this.neighborhoodTracker.getNeighborhoodLog();
  }

  public List<String> getSystemOutMessages() {
    return this.systemOutTracker.getSystemOutMessages();
  }

  public void trackEvent(ClientMessage message) {
    this.neighborhoodTracker.trackEvent(message);
    this.systemOutTracker.trackEvent(message);
  }

  public void invokeMainMethod() {
    if (this.mainMethod == null) {
      throw new ValidationRuntimeException(ExceptionKey.NO_MAIN_METHOD_VALIDATION);
    }
    try {
      this.mainMethod.invoke(null, new Object[] {null});
    } catch (IllegalAccessException e) {
      throw new ValidationRuntimeException(ExceptionKey.ERROR_RUNNING_MAIN);
    } catch (InvocationTargetException e) {
      Throwable cause = e;
      // the cause will be more informative, as InvocationTargetException
      // wraps an underlying exception.
      if (e.getCause() != null) {
        cause = e.getCause();
      }
      throw new ValidationRuntimeException(ExceptionKey.ERROR_RUNNING_MAIN, cause);
    }
  }
}
