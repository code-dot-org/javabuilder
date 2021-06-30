package org.code.theater;

import java.util.HashMap;
import java.util.Map;

class InstrumentSampleLoader {

  private static Map<Instrument, Map<Integer, String>> generateInstrumentFileMap() {
    // TODO: Populate map with file paths once created
    return new HashMap<>();
  }

  // Map of Instrument -> Map of note value (int) -> file path / name
  private final Map<Instrument, Map<Integer, String>> instrumentFileMap;

  public InstrumentSampleLoader() {
    this(InstrumentSampleLoader.generateInstrumentFileMap());
  }

  InstrumentSampleLoader(Map<Instrument, Map<Integer, String>> instrumentFileMap) {
    this.instrumentFileMap = instrumentFileMap;
  }

  /**
   * Retrieves the sample file for the given instrument and note value. Returns null if no sample is
   * found.
   *
   * @param instrument
   * @param note
   * @return filename of sample, or null if no sample is found.
   */
  public String getSampleFile(Instrument instrument, int note) {
    if (!instrumentFileMap.containsKey(instrument)) {
      System.out.printf("No notes available for instrument %s%n", instrument);
      return null;
    }

    final Map<Integer, String> noteToFileMap = instrumentFileMap.get(instrument);
    if (!noteToFileMap.containsKey(note)) {
      System.out.printf("Can't play note %s on instrument %s%n", note, instrument);
      return null;
    }

    return noteToFileMap.get(note);
  }
}
