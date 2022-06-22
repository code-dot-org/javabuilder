package org.code.protocol;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

public class JavabuilderContextTest {
  @Test
  public void canRegisterAndGetGlobalProtocol() {
    GlobalProtocol protocol = mock(GlobalProtocol.class);
    JavabuilderContext.create();
    JavabuilderContext.getInstance().register(GlobalProtocol.class, protocol);
    GlobalProtocol result = JavabuilderContext.getInstance().getGlobalProtocol();
    assertNotNull(result);
    assertEquals(protocol, result);
  }

  @Test
  public void cannotRegisterMismatchedClass() {
    JavabuilderContext.create();
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          JavabuilderContext.getInstance().register(TestObject2.class, new TestObject1());
        });
  }

  @Test
  public void canRegisterSubclass() {
    JavabuilderContext.create();
    TestObject3 testObject3 = new TestObject3();
    JavabuilderContext.getInstance().register(TestObject1.class, testObject3);
    JavabuilderSharedObject result = JavabuilderContext.getInstance().get(TestObject1.class);
    assertNotNull(result);
    assertEquals(testObject3, result);
  }

  @Test
  public void canRegisterAndGetClass() {
    JavabuilderContext.create();
    TestObject2 testObject2 = new TestObject2();
    JavabuilderContext.getInstance().register(TestObject2.class, testObject2);
    JavabuilderSharedObject result = JavabuilderContext.getInstance().get(TestObject2.class);
    assertNotNull(result);
    assertEquals(testObject2, result);
  }

  private class TestObject1 extends JavabuilderSharedObject {}

  private class TestObject2 extends JavabuilderSharedObject {}

  private class TestObject3 extends TestObject1 {}
}
