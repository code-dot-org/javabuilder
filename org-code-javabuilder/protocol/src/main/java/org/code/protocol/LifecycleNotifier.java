package org.code.protocol;

import java.util.HashSet;
import java.util.Set;

/** Notifies delegates of code execution lifecycle events. */
public class LifecycleNotifier implements LifecycleListener {

  private final Set<LifecycleListener> listeners;

  public LifecycleNotifier() {
    this.listeners = new HashSet<>();
  }

  public void registerListener(LifecycleListener listener) {
    this.listeners.add(listener);
  }

  @Override
  public void onExecutionEnded() {
    for (LifecycleListener listener : this.listeners) {
      listener.onExecutionEnded();
    }
  }
}
