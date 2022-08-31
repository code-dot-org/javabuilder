package org.code.validation.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.code.protocol.ClientMessageType;
import org.code.validation.ClientMessageHelper;
import org.junit.jupiter.api.Test;

public class SystemOutTrackerTest {
  private SystemOutTracker unitUnderTest;

  @Test
  public void tracksAllNonNewlineMessages() {
    unitUnderTest = new SystemOutTracker();
    // should be tracked
    unitUnderTest.trackEvent(new ClientMessageHelper(ClientMessageType.SYSTEM_OUT, "hello"));
    // should be ignored
    unitUnderTest.trackEvent(new ClientMessageHelper(ClientMessageType.SYSTEM_OUT, "\n"));
    // should be tracked
    unitUnderTest.trackEvent(new ClientMessageHelper(ClientMessageType.SYSTEM_OUT, "hello world"));

    List<String> messages = unitUnderTest.getSystemOutMessages();
    assertEquals(2, messages.size());
    assertEquals("hello world", messages.get(1));
  }

  @Test
  public void resetsSuccessfully() {
    unitUnderTest = new SystemOutTracker();
    unitUnderTest.trackEvent(new ClientMessageHelper(ClientMessageType.SYSTEM_OUT, "hello"));
    // should reset message list
    unitUnderTest.reset();
    unitUnderTest.trackEvent(
        new ClientMessageHelper(ClientMessageType.SYSTEM_OUT, "a new message"));
    unitUnderTest.trackEvent(new ClientMessageHelper(ClientMessageType.SYSTEM_OUT, "hello world"));
    unitUnderTest.trackEvent(
        new ClientMessageHelper(ClientMessageType.SYSTEM_OUT, "a third message"));

    List<String> messages = unitUnderTest.getSystemOutMessages();
    assertEquals(3, messages.size());
    assertEquals("a new message", messages.get(0));
  }
}
