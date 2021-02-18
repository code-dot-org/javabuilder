package org.code.javabuilder;

import java.security.Principal;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

/**
 * Accepts requests to the /execute channel to compile and run user code. Directs output from the
 * user's program to the user on the /topic/output channel.
 */
@Controller
public class JavaBuildController {

  private final CompileRunService compileRunService;

  JavaBuildController(CompileRunService compileRunService) {
    this.compileRunService = compileRunService;
  }

  /** Executes the user code and sends the output of that code across the established websocket. */
  @MessageMapping(Destinations.EXECUTE_CODE)
  @SendToUser(Destinations.PTP_PREFIX + Destinations.OUTPUT_CHANNEL)
  public UserProgramOutput execute(UserProgram userProgram, Principal principal) {
    // Send fake output
    compileRunService.sendMessages(principal.getName(), "Compiling...");
    return new UserProgramOutput("Hello World!");
  }
}
