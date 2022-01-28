package org.code.validation;

/**
 * User-facing class that represents the results of a neighborhood project run. Includes helper
 * functions for analyzing the actions painter(s) took in the neighborhood.
 */
public class NeighborhoodLog {
  private final PainterLog[] painterLogs;
  private final String[][] finalOutput;

  public NeighborhoodLog(PainterLog[] painterLogs, String[][] finalOutput) {
    this.painterLogs = painterLogs;
    this.finalOutput = finalOutput;
  }

  public PainterLog[] getPainterLogs() {
    return this.painterLogs;
  }

  public String[][] getFinalOutput() {
    return this.finalOutput;
  }

  /**
   * @param neighborhoodActionType
   * @param times
   * @return true if the given neighborhoodActionType occurred exactly "times" times for at least
   *     one painter, false otherwise.
   */
  public boolean onePainterDidAction(NeighborhoodActionType neighborhoodActionType, int times) {
    for (PainterLog painterLog : this.painterLogs) {
      if (painterLog.didActionExactly(neighborhoodActionType, times)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param neighborhoodActionType
   * @param times
   * @return true if the given neighborhoodActionType occurred exactly "times" times across all
   *     painters, false otherwise.
   */
  public boolean actionHappened(NeighborhoodActionType neighborhoodActionType, int times) {
    int actionCount = 0;
    for (PainterLog painterLog : this.painterLogs) {
      actionCount += painterLog.actionCount(neighborhoodActionType);
    }
    return actionCount == times;
  }

  /**
   * @param expectedOutput 2d array of Strings, where a non-null String is a paint color for the
   *     cell.
   * @return true if expectedOutput matches finalOutput, false otherwise.
   */
  public boolean finalOutputMatches(String[][] expectedOutput) {
    if (expectedOutput.length != this.finalOutput.length) {
      return false;
    }
    for (int i = 0; i < this.finalOutput.length; i++) {
      if (this.finalOutput[i].length != expectedOutput[i].length) {
        return false;
      }
      for (int j = 0; j < this.finalOutput[i].length; j++) {
        String expectedValue = expectedOutput[i][j];
        String actualValue = this.finalOutput[i][j];
        // values will have object equality if both are null
        if (expectedValue == actualValue) {
          continue;
        }
        if ((actualValue == null && expectedValue != null) || !actualValue.equals(expectedValue)) {
          return false;
        }
      }
    }
    return true;
  }
  /**
   * @param expectedOutput 2d array of booleans, where true means the cell has paint.
   * @return true if paint of any color is in the cells set to true in expectedOutput, false
   *     otherwise.
   */
  public boolean finalOutputContainsPaint(boolean[][] expectedOutput) {
    if (expectedOutput.length != this.finalOutput.length) {
      return false;
    }
    for (int i = 0; i < this.finalOutput.length; i++) {
      if (this.finalOutput[i].length != expectedOutput[i].length) {
        return false;
      }
      for (int j = 0; j < this.finalOutput[i].length; j++) {
        boolean cellHasPaint = this.finalOutput[i][j] != null;
        if (expectedOutput[i][j] != cellHasPaint) {
          return false;
        }
      }
    }
    return true;
  }
}
