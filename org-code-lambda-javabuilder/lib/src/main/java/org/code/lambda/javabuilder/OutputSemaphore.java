package org.code.lambda.javabuilder;

public class OutputSemaphore {
  private static int outputBeingProcessed = 0;
  private static boolean processingFinalOutput = false;

  public OutputSemaphore() {}

  public synchronized static void addOutputInProgress() {
    outputBeingProcessed++;
  }

  public synchronized static void decreaseOutputInProgress() {
    outputBeingProcessed = Math.max(outputBeingProcessed - 1, 0);
  }

  public synchronized static boolean anyOutputInProgress() {
    OutputHandler.sendDebuggingMessage("Output in progress: " + outputBeingProcessed);
    return outputBeingProcessed > 0 || processingFinalOutput;
  }

  public synchronized static void signalProcessFinalOutput() {
    processingFinalOutput = true;
  }

  public synchronized static void processFinalOutput() {
    processingFinalOutput = false;
  }
}
