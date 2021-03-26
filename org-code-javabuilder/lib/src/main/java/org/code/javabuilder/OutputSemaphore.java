package org.code.javabuilder;

public class OutputSemaphore {
  private int outputBeingProcessed = 0;
  private boolean processingFinalOutput = false;

  public OutputSemaphore() {}

  public synchronized void addOutputInProgress() {
    outputBeingProcessed++;
  }

  public synchronized void decreaseOutputInProgress() {
    outputBeingProcessed = Math.max(outputBeingProcessed - 1, 0);
  }

  public synchronized boolean anyOutputInProgress() {
    return outputBeingProcessed > 0 || processingFinalOutput;
  }

  public synchronized void signalProcessFinalOutput() {
    processingFinalOutput = true;
  }

  public synchronized void processFinalOutput() {
    processingFinalOutput = false;
  }
}
