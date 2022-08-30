package org.code.validation;

import java.util.List;
import org.code.protocol.JavabuilderContext;
import org.code.validation.support.ValidationProtocol;

public class SystemOutTestRunner {
  public static List<String> run() {
    ValidationProtocol protocolInstance =
        (ValidationProtocol) JavabuilderContext.getInstance().get(ValidationProtocol.class);
    protocolInstance.invokeMainMethod();
    return protocolInstance.getSystemOutMessages();
  }
}
