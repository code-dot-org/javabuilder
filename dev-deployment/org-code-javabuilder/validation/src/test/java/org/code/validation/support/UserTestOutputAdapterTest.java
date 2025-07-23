package org.code.validation.support;

import static org.mockito.Mockito.*;

import java.util.HashMap;
import org.code.protocol.*;
import org.code.validation.ClientMessageHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserTestOutputAdapterTest {
  private final OutputAdapter delegateOutputAdapter = mock(OutputAdapter.class);
  private UserTestOutputAdapter testOutputAdapter;

  @BeforeEach
  public void setUp() {
    testOutputAdapter = new UserTestOutputAdapter(delegateOutputAdapter);
    JavabuilderContext.getInstance()
        .register(ValidationProtocol.class, mock(ValidationProtocol.class));
  }

  @Test
  public void sendsTestResultMessages() {
    HashMap<String, String> messageDetails = new HashMap<>();
    messageDetails.put(ClientMessageDetailKeys.STATUS, "successful");
    UserTestResultMessage userTestResultMessage =
        new UserTestResultMessage(UserTestResultSignalKey.TEST_STATUS, messageDetails);
    testOutputAdapter.sendMessage(userTestResultMessage);
    verify(delegateOutputAdapter).sendMessage(userTestResultMessage);
  }

  @Test
  public void sendsStatusMessages() {
    StatusMessage statusMessage = new StatusMessage(StatusMessageKey.RUNNING);
    testOutputAdapter.sendMessage(statusMessage);
    verify(delegateOutputAdapter).sendMessage(statusMessage);
  }

  @Test
  public void sendsSystemOutMessageIfInTestRun() {
    ClientMessage systemOutMessage =
        new ClientMessageHelper(ClientMessageType.SYSTEM_OUT, "test system out");
    testOutputAdapter.setIsValidation(false);
    testOutputAdapter.sendMessage(systemOutMessage);
    verify(delegateOutputAdapter).sendMessage(systemOutMessage);
  }

  @Test
  public void doesNotSendSystemOutMessageIfInValidationRun() {
    ClientMessage systemOutMessage =
        new ClientMessageHelper(ClientMessageType.SYSTEM_OUT, "test system out");
    testOutputAdapter.setIsValidation(true);
    testOutputAdapter.sendMessage(systemOutMessage);
    verify(delegateOutputAdapter, never()).sendMessage(systemOutMessage);
  }

  @Test
  public void neverSendsTheaterMessage() {
    ClientMessage theaterMessage =
        new ClientMessageHelper(ClientMessageType.THEATER, "test theater");
    testOutputAdapter.setIsValidation(false);
    testOutputAdapter.sendMessage(theaterMessage);
    testOutputAdapter.setIsValidation(true);
    testOutputAdapter.sendMessage(theaterMessage);
    verify(delegateOutputAdapter, never()).sendMessage(theaterMessage);
  }
}
