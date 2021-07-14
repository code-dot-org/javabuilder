package org.code.theater;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

class InstrumentSampleLoader {

  private static Map<Instrument, Map<Integer, String>> generateInstrumentFileMap() {
    final Map<Integer, String> pianoMap = new HashMap<>();

    // Piano note values: 48 - 84 (C3 - C6)
    for (int note = 48; note <= 84; note++) {
      pianoMap.put(note, "instruments/javalab-piano-" + note + ".wav");
    }

    final Map<Integer, String> bassMap = new HashMap<>();

    // Bass note values: 24 - 60 (C1 - C4)
    for (int note = 24; note <= 60; note++) {
      bassMap.put(note, "instruments/javalab-bass-" + note + ".wav");
    }

    return Map.of(
        Instrument.PIANO, pianoMap,
        Instrument.BASS, bassMap);
  }

  // Map of Instrument -> Map of note value (int) -> file path / name
  private final Map<Instrument, Map<Integer, String>> instrumentFileMap;

  protected InstrumentSampleLoader() {
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
  String getSampleFilePath(Instrument instrument, int note) {
    if (!instrumentFileMap.containsKey(instrument)) {
      System.out.printf("No notes available for instrument %s%n", instrument);
      return null;
    }

    final Map<Integer, String> noteToFileMap = instrumentFileMap.get(instrument);
    if (!noteToFileMap.containsKey(note)) {
      System.out.printf("Can't play note %s on instrument %s%n", note, instrument);
      return null;
    }

    final URL resourceUrl = getClass().getClassLoader().getResource(noteToFileMap.get(note));
    if (resourceUrl == null) {
      return null;
    }

    try {
      return Paths.get(resourceUrl.toURI()).toString();
    } catch (URISyntaxException e) {
      return null;
    }
  }
}
