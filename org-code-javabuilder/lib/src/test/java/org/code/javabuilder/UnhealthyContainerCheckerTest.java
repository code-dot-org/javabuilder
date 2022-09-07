package org.code.javabuilder;

import static org.code.javabuilder.UnhealthyContainerChecker.CONTAINER_ID_KEY_NAME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import java.util.Map;
import org.code.javabuilder.UnhealthyContainerChecker.ShutdownTrigger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class UnhealthyContainerCheckerTest {
  private static final String TABLE_NAME = "tableName";

  private GetItemResult getItemResult;
  private ArgumentCaptor<Map<String, AttributeValue>> keyCaptor;
  private UnhealthyContainerChecker unitUnderTest;

  @BeforeEach
  public void setUp() {
    final AmazonDynamoDB dynamoDBClient = mock(AmazonDynamoDB.class);
    getItemResult = mock(GetItemResult.class);
    keyCaptor = ArgumentCaptor.forClass(Map.class);
    when(dynamoDBClient.getItem(anyString(), keyCaptor.capture())).thenReturn(getItemResult);

    unitUnderTest = new UnhealthyContainerChecker(dynamoDBClient, TABLE_NAME);
  }

  @Test
  public void testReturnsTrueIfContainerIDFound() {
    final String containerId = "containerId1234";
    final ShutdownTrigger trigger = ShutdownTrigger.END;
    when(getItemResult.getItem()).thenReturn(Map.of());

    assertTrue(unitUnderTest.shouldForceShutdownContainer(containerId, trigger));

    this.verifyKey(containerId, trigger);
  }

  @Test
  public void testReturnsFalseIfContainerIDNotFound() {
    final String containerId = "containerId5678";
    final ShutdownTrigger trigger = ShutdownTrigger.START;
    when(getItemResult.getItem()).thenReturn(null);

    assertFalse(unitUnderTest.shouldForceShutdownContainer(containerId, trigger));

    this.verifyKey(containerId, trigger);
  }

  @Test
  public void testReturnsFalseIfClientThrowsException() {
    final String containerId = "containerId9090";
    final ShutdownTrigger trigger = ShutdownTrigger.END;
    when(getItemResult.getItem()).thenThrow(new RuntimeException("exception"));

    assertFalse(unitUnderTest.shouldForceShutdownContainer(containerId, trigger));

    this.verifyKey(containerId, trigger);
  }

  private void verifyKey(String containerId, ShutdownTrigger trigger) {
    final Map<String, AttributeValue> key = keyCaptor.getValue();
    final AttributeValue value = key.get(CONTAINER_ID_KEY_NAME);
    assertEquals(containerId + "#" + trigger.getName(), value.getS());
  }
}
