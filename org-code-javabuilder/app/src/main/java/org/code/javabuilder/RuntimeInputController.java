package org.code.javabuilder;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class RuntimeInputController {
  private final JavaRunner javaRunner;

  RuntimeInputController(JavaRunner javaRunner) {
    this.javaRunner = javaRunner;
  }

  @MessageMapping(Destinations.PROCESS_INPUT)
  public void execute(UserInput userInput, Principal principal) {
    javaRunner.passInputToRuntime(userInput, principal);
  }
}
