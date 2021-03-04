package org.code.javabuilder;

import java.security.Principal;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

/** Accepts requests to the /userInput channel to pass input to the user's program runtime. */
@Controller
public class RuntimeInputController {
  private final JavaRunner javaRunner;

  RuntimeInputController(JavaRunner javaRunner) {
    this.javaRunner = javaRunner;
  }

  /**
   * Sends input to the currently running program.
   *
   * @param userInput the input from the client console
   * @param principal the client's identification
   */
  @MessageMapping(Destinations.PROCESS_INPUT)
  public void sendInput(UserInput userInput, Principal principal) {
    javaRunner.passInputToRuntime(userInput, principal);
  }
}
