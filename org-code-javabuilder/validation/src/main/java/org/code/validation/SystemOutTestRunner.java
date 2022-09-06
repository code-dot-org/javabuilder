package org.code.validation;

import java.util.List;
import org.code.protocol.JavabuilderContext;
import org.code.validation.support.ValidationProtocol;

public class SystemOutTestRunner {
  // Run the main method of the user's program and return a list
  // of all system.out messages in order.
  public static List<String> run() {
    ValidationProtocol protocolInstance =
        (ValidationProtocol) JavabuilderContext.getInstance().get(ValidationProtocol.class);
    protocolInstance.invokeMainMethod();
    return protocolInstance.getSystemOutMessages();
  }
}
