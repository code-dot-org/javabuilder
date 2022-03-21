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
    private LifecycleNotifier lifecycleNotifier;
    private ContentManager contentManager;

    private Builder() {
      this.outputAdapter = mock(OutputAdapter.class);
      this.inputAdapter = mock(InputAdapter.class);
      this.dashboardHostname = "";
      this.channelId = "";
      this.lifecycleNotifier = mock(LifecycleNotifier.class);
      this.contentManager = mock(ContentManager.class);
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

    public Builder withLifecycleNotifier(LifecycleNotifier lifecycleNotifier) {
      this.lifecycleNotifier = lifecycleNotifier;
      return this;
    }

    public Builder withContentManager(ContentManager contentManager) {
      this.contentManager = contentManager;
      return this;
    }

    public void create() {
      GlobalProtocol.create(
          this.outputAdapter,
          this.inputAdapter,
          this.dashboardHostname,
          this.channelId,
          this.lifecycleNotifier,
          this.contentManager);
    }
  }
}
