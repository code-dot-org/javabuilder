package org.code.javabuilder;

/**
 * Listens for when a task is complete. Useful for notifying processes running on separate threads.
 */
public interface CompletionListener {
  /** The task has been completed */
  void onComplete();
}
