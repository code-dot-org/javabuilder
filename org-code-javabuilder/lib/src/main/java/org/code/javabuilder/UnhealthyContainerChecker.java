package org.code.javabuilder;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import java.util.Map;
import org.code.protocol.LoggerUtils;

/**
 * Checks if the current container has been marked unhealthy, so that we can shut it down if needed.
 */
public class UnhealthyContainerChecker {
  static final String CONTAINER_ID_KEY_NAME = "container_id";

  /**
   * When the health status is being checked. This allows us to choose whether to trigger a shutdown
   * at the beginning or end of the session.
   */
  public enum ShutdownTrigger {
    START("start"),
    END("end");

    private final String name;

    ShutdownTrigger(String name) {
      this.name = name;
    }

    public String getName() {
      return this.name;
    }
  }

  private final AmazonDynamoDB dynamoDBClient;
  private final String tableName;

  public UnhealthyContainerChecker(AmazonDynamoDB dynamoDBClient, String tableName) {
    this.dynamoDBClient = dynamoDBClient;
    this.tableName = tableName;
  }

  public boolean shouldForceShutdownContainer(String containerId, ShutdownTrigger trigger) {
    // The container ID value is a concatenation of the ID and the shutdown trigger type
    final String containerIdCompositeValue = containerId + "#" + trigger.getName();
    final Map<String, AttributeValue> key =
        Map.of(CONTAINER_ID_KEY_NAME, new AttributeValue(containerIdCompositeValue));
    final Map<String, AttributeValue> entry;
    try {
      entry = this.dynamoDBClient.getItem(this.tableName, key).getItem();
    } catch (Exception e) {
      // Indicates an unexpected error (missing entries should return null); log warning and return
      // false silently to be safe.
      LoggerUtils.logWarning(e.getMessage());
      return false;
    }

    return entry != null;
  }
}
