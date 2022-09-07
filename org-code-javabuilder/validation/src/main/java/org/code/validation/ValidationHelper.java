package org.code.validation;

import java.util.List;
import org.code.protocol.JavabuilderContext;
import org.code.validation.support.ValidationProtocol;

public class ValidationHelper {
  public static List<String> getClassNames() {
    ValidationProtocol protocolInstance =
        (ValidationProtocol) JavabuilderContext.getInstance().get(ValidationProtocol.class);
    return protocolInstance.getUserClassNames();
  }
}
