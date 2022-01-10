package org.code.protocol;

import static org.mockito.Mockito.*;

public class GlobalProtocolTestFactory {
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private OutputAdapter outputAdapter;
    private InputAdapter inputAdapter;
    private String dashboardHostname;
    private String channelId;
    private String levelId;
    private JavabuilderFileManager fileManager;
    private LifecycleNotifier lifecycleNotifier;

    private Builder() {
      this.outputAdapter = mock(OutputAdapter.class);
      this.inputAdapter = mock(InputAdapter.class);
      this.dashboardHostname = "";
      this.channelId = "";
      this.levelId = "";
      this.fileManager = mock(JavabuilderFileManager.class);
      this.lifecycleNotifier = mock(LifecycleNotifier.class);
    }

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
          this.outputAdapter,
          this.inputAdapter,
          this.dashboardHostname,
          this.channelId,
          this.levelId,
          this.fileManager,
          this.lifecycleNotifier);
    }
  }
}
