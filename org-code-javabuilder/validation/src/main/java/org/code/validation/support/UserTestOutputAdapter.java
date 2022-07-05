package org.code.validation.support;

import org.code.protocol.ClientMessage;
import org.code.protocol.ClientMessageType;
import org.code.protocol.JavabuilderContext;
import org.code.protocol.OutputAdapter;

/** Support class for parsing output messages during a test run. */
public class UserTestOutputAdapter implements OutputAdapter {
  private boolean isValidation;
  private OutputAdapter delegateOutputAdapter;

  public UserTestOutputAdapter(OutputAdapter delegateOutputAdapter) {
    this.isValidation = false;
    this.delegateOutputAdapter = delegateOutputAdapter;
  }

  @Override
  public void sendMessage(ClientMessage message) {
    ClientMessageType messageType = message.getType();
    switch (messageType) {
      case TEST_RESULT:
      case STATUS:
      case EXCEPTION:
        delegateOutputAdapter.sendMessage(message);
        break;
      case SYSTEM_OUT:
        if (!this.isValidation) {
          delegateOutputAdapter.sendMessage(message);
        }
        break;
      case NEIGHBORHOOD:
        if (this.isValidation) {
          ValidationProtocol protocolInstance =
              (ValidationProtocol) JavabuilderContext.getInstance().get(ValidationProtocol.class);
          protocolInstance.trackEvent(message);
        }
        break;
      default:
        break;
    }
  }

  public void setIsValidation(boolean isValidation) {
    this.isValidation = isValidation;
  }
}
