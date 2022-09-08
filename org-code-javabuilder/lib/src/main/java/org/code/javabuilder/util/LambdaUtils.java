package org.code.javabuilder.util;

import static org.code.javabuilder.InternalFacingExceptionTypes.CONNECTION_TERMINATED;

import org.code.javabuilder.InternalFacingRuntimeException;
import org.code.protocol.ClientMessage;
import org.code.protocol.LoggerUtils;
import org.code.protocol.OutputAdapter;

public final class LambdaUtils {
  private LambdaUtils() {
    throw new UnsupportedOperationException("Instantiation of utility class is not allowed.");
  }

  /**
   * Sends a message via the OutputAdapter and handles any exceptions if they are thrown. This
   * allows us to safely try and send messages from the handler without unintentionally raising
   * uncaught exceptions.
   *
   * @param logOnLostConnection whether we should log if the OutputAdapter throws a
   *     CONNECTION_TERMINATED exception. This can be false in situations where we know we've
   *     already logged elsewhere.
   */
  public static void safelySendMessage(
      OutputAdapter outputAdapter, ClientMessage message, boolean logOnLostConnection) {
    try {
      outputAdapter.sendMessage(message);
    } catch (InternalFacingRuntimeException e) {
      // Unless logOnLostConnection is true, only log for messages that aren't CONNECTION_TERMINATED
      if (logOnLostConnection || !e.getMessage().equals(CONNECTION_TERMINATED)) {
        LoggerUtils.logTrackingException(e);
      }
    } catch (Exception e) {
      // Catch any other exceptions here to prevent them from propagating.
      LoggerUtils.logTrackingException(e);
    }
  }
}
