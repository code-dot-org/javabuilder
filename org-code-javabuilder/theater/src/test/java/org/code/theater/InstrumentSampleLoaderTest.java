package org.code.theater;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InstrumentSampleLoaderTest {
  private static final Instrument VALID_INSTRUMENT = Instrument.PIANO;
  private static final int VALID_NOTE = 60;
  private static final String VALID_FILE = "file.wav";

  private InstrumentSampleLoader unitUnderTest;

  @BeforeEach
  public void setUp() {
    final Map<Integer, String> instrumentMap = new HashMap<>();
    instrumentMap.put(VALID_NOTE, VALID_FILE);

    final Map<Instrument, Map<Integer, String>> testMap = new HashMap<>();
    testMap.put(VALID_INSTRUMENT, instrumentMap);

    unitUnderTest = new InstrumentSampleLoader(testMap);
  }

  @Test
  public void testGetSampleFileReturnsNullForMissingInstrument() {
    assertNull(unitUnderTest.getSampleFilePath(Instrument.BASS, VALID_NOTE));
  }

  @Test
  public void testGetSampleFileReturnsNullForMissingNote() {
    assertNull(unitUnderTest.getSampleFilePath(VALID_INSTRUMENT, 12));
  }
}
