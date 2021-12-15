package org.code.protocol;

import static org.mockito.Mockito.mock;

public class GlobalProtocolMocker {

  public static Builder builder() {
    return new Builder();
  }

  private static class Builder {
    private OutputAdapter outputAdapter = mock(OutputAdapter.class);
    private InputAdapter inputAdapter = mock(InputAdapter.class);
    private String dashboardHostname = "";
    private String channelId = "";
    private String levelId = "";
    private JavabuilderFileManager fileManager = mock(JavabuilderFileManager.class);
    private LifecycleNotifier lifecycleNotifier = mock(LifecycleNotifier.class);

    public Builder withOutputAdapter(OutputAdapter outputAdapter) {
      this.outputAdapter = outputAdapter;
      return this;
    }

    public Builder withInputAdapter(InputAdapter inputAdapter) {
      this.inputAdapter = inputAdapter;
      return this;
    }

    public Builder withDashboardHostname(String dashboardHostname) {
      this.dashboardHostname = dashboardHostname;
      return this;
    }

    public Builder withChannelId(String channelId) {
      this.channelId = channelId;
      return this;
    }

    public Builder withLevelId(String levelId) {
      this.levelId = levelId;
      return this;
    }

    public Builder withFileManager(JavabuilderFileManager fileManager) {
      this.fileManager = fileManager;
      return this;
    }

    public Builder withLifecycleNotifier(LifecycleNotifier lifecycleNotifier) {
      this.lifecycleNotifier = lifecycleNotifier;
      return this;
    }

    public void create() {
      GlobalProtocol.create(
          outputAdapter,
          inputAdapter,
          dashboardHostname,
          channelId,
          levelId,
          fileManager,
          lifecycleNotifier);
    }
  }
}
