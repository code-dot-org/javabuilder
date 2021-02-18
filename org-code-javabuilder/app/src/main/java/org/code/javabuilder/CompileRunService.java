package org.code.javabuilder;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Compiles and runs the provided user code. Logs output from the user's program to the user on the
 * /topic/output channel.
 */
@Service
public class CompileRunService {

  private final SimpMessagingTemplate simpMessagingTemplate;
  private static final String WS_MESSAGE_TRANSFER_DESTINATION =
      Destinations.PTP_PREFIX + Destinations.OUTPUT_CHANNEL;

  CompileRunService(SimpMessagingTemplate simpMessagingTemplate) {
    this.simpMessagingTemplate = simpMessagingTemplate;
  }

  /** Logs output messages to the user. */
  public void sendMessages(String userName, String message) {
    simpMessagingTemplate.convertAndSendToUser(
        userName, WS_MESSAGE_TRANSFER_DESTINATION, new UserProgramOutput(message));
  }
}
