package org.code.javabuilder;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import java.util.Map;
import org.code.protocol.LoggerUtils;

public class UnhealthyContainerChecker {
  private final AmazonDynamoDB dynamoDBClient;
  private final String tableName;

  public UnhealthyContainerChecker(AmazonDynamoDB dynamoDBClient, String tableName) {
    this.dynamoDBClient = dynamoDBClient;
    this.tableName = tableName;
  }

  public boolean shouldForceRecycleContainer(String containerId) {
    LoggerUtils.logInfo("Table name: " + this.tableName);
    final GetItemRequest request =
        new GetItemRequest()
            .withKey(Map.of("container_id", new AttributeValue(containerId)))
            .withTableName(this.tableName);

    final Map<String, AttributeValue> item;
    try {
      item = this.dynamoDBClient.getItem(request).getItem();
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
