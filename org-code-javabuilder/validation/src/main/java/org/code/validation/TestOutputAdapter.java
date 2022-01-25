package org.code.validation;

import org.code.protocol.ClientMessage;
import org.code.protocol.ClientMessageType;
import org.code.protocol.OutputAdapter;

public class TestOutputAdapter implements OutputAdapter {
  private boolean isValidation;
  private OutputAdapter delegateOutputAdapter;

  public TestOutputAdapter(OutputAdapter delegateOutputAdapter) {
    this.isValidation = false;
    this.delegateOutputAdapter = delegateOutputAdapter;
  }

  @Override
  public void sendMessage(ClientMessage message) {
    // TODO: write method
    ClientMessageType messageType = message.getType();
    switch (messageType) {
        // TODO: when we have a TEST_RESULT type send that on to the delegate as well
      case STATUS:
        delegateOutputAdapter.sendMessage(message);
        break;
      case SYSTEM_OUT:
        if (!this.isValidation) {
          delegateOutputAdapter.sendMessage(message);
        }
        break;
      case NEIGHBORHOOD:
        if (this.isValidation) {
          // TODO: parse neighborhood messages in a validation run
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