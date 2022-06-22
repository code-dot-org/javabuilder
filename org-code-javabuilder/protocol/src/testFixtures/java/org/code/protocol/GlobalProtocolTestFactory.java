package org.code.protocol;

import static org.mockito.Mockito.*;

public class GlobalProtocolTestFactory {
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private OutputAdapter outputAdapter;
    private InputAdapter inputAdapter;
    private LifecycleNotifier lifecycleNotifier;
    private ContentManager contentManager;

    private Builder() {
      this.outputAdapter = mock(OutputAdapter.class);
      this.inputAdapter = mock(InputAdapter.class);
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

    public Builder withLifecycleNotifier(LifecycleNotifier lifecycleNotifier) {
      this.lifecycleNotifier = lifecycleNotifier;
      return this;
    }

    public Builder withContentManager(ContentManager contentManager) {
      this.contentManager = contentManager;
      return this;
    }

    public void create() {
      GlobalProtocol protocol =
          new GlobalProtocol(
              this.outputAdapter,
              new InputHandler(this.inputAdapter),
              this.lifecycleNotifier,
              this.contentManager);
      JavabuilderContext.getInstance().register(GlobalProtocol.class, protocol);
    }
  }
}
