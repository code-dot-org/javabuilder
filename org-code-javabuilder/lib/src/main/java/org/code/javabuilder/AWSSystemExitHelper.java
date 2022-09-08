package org.code.javabuilder;

import static org.code.protocol.LoggerNames.MAIN_LOGGER;

import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.model.DeleteConnectionRequest;
import com.amazonaws.services.apigatewaymanagementapi.model.GoneException;
import java.util.logging.Handler;
import java.util.logging.Logger;
import org.code.protocol.JavabuilderContext;
import org.code.protocol.LoggerUtils;

public class AWSSystemExitHelper implements SystemExitHelper {
  private final String connectionId;
  private final AmazonApiGatewayManagementApi api;

  public AWSSystemExitHelper(String connectionId, AmazonApiGatewayManagementApi api) {
    this.connectionId = connectionId;
    this.api = api;
  }

  @Override
  public void exit(int status) {
    PerformanceTracker performanceTracker =
        (PerformanceTracker) JavabuilderContext.getInstance().get(PerformanceTracker.class);
    performanceTracker.trackInstanceEnd();
    performanceTracker.logPerformance();
    this.cleanUpResources();
    System.exit(status);
  }

  private void cleanUpResources() {
    final DeleteConnectionRequest deleteConnectionRequest =
        new DeleteConnectionRequest().withConnectionId(connectionId);
    // Deleting the API Gateway connection should always be the last thing executed because the
    // delete action cleans up the AWS resources associated with this lambda
    try {
      api.deleteConnection(deleteConnectionRequest);
    } catch (GoneException e) {
      // if the connection is already gone, we don't need to delete the connection.
    } catch (Exception e) {
      // Handle any other exceptions so that shut down proceeds normally. If this is an
      // IllegalStateException, it indicates that the connection was already shut down for
      // some reason.
      LoggerUtils.logTrackingException(e);
    }
    // clean up log handler to avoid duplicate logs in future runs.
    Handler[] allHandlers = Logger.getLogger(MAIN_LOGGER).getHandlers();
    for (int i = 0; i < allHandlers.length; i++) {
      Logger.getLogger(MAIN_LOGGER).removeHandler(allHandlers[i]);
    }
  }
}
