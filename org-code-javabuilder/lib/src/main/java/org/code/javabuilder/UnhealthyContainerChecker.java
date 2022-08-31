package org.code.javabuilder;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import java.util.Map;
import org.code.protocol.LoggerUtils;

public class UnhealthyContainerChecker {
  public enum ShutdownTrigger {
    START("start"),
    END("end");

    private final String name;

    ShutdownTrigger(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  private final AmazonDynamoDB dynamoDBClient;
  private final String tableName;

  public UnhealthyContainerChecker(AmazonDynamoDB dynamoDBClient, String tableName) {
    this.dynamoDBClient = dynamoDBClient;
    this.tableName = tableName;
  }

  public boolean shouldForceRecycleContainer(String containerId, ShutdownTrigger trigger) {
    LoggerUtils.logInfo("Table name: " + this.tableName);

    // The container ID value is a concatenation of the ID and the shutdown trigger type
    final String containerIdCompositeValue = containerId + "#" + trigger.getName();
    final Map<String, AttributeValue> item;
    try {
      item =
          this.dynamoDBClient
              .getItem(
                  this.tableName,
                  Map.of("container_id", new AttributeValue(containerIdCompositeValue)))
              .getItem();
    } catch (Exception e) {
      LoggerUtils.logInfo(e.getMessage());
      return false;
    }

    if (item == null) {
      LoggerUtils.logInfo("Nothing found for ID: " + containerId);
      return false;
    }

    for (String key : item.keySet()) {
      LoggerUtils.logInfo(String.format("%s: %s\n", key, item.get(key)));
    }

    try {
      this.dynamoDBClient.deleteItem(
          this.tableName, Map.of("container_id", new AttributeValue(containerId)));
    } catch (Exception e) {
      LoggerUtils.logInfo(e.getMessage());
    }

    return true;
  }
}
