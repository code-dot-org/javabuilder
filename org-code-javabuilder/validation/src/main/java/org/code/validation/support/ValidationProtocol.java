package org.code.validation.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.code.protocol.ClientMessage;
import org.code.validation.NeighborhoodLog;

public class ValidationProtocol {
  private static ValidationProtocol instance;
  private final Method mainMethod;
  private final NeighborhoodTracker neighborhoodTracker;

  public static void create(Method mainMethod, NeighborhoodTracker neighborhoodTracker) {
    instance = new ValidationProtocol(mainMethod, neighborhoodTracker);
  }

  public static ValidationProtocol getInstance() {
    return instance;
  }

  protected ValidationProtocol(Method mainMethod, NeighborhoodTracker neighborhoodTracker) {
    this.mainMethod = mainMethod;
    this.neighborhoodTracker = neighborhoodTracker;
  }

  public NeighborhoodLog getNeighborhoodLog() {
    return this.neighborhoodTracker.getNeighborhoodLog();
  }

  public void trackEvent(ClientMessage message) {
    this.neighborhoodTracker.trackEvent(message);
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
