package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.code.protocol.InternalExceptionKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ProjectDataTest {
  private static final String MAIN_JSON_CONTENT = "{ main json }";
  private static final String MAZE_FILE_CONTENT = "{ maze file }";
  private static final String ASSET_FILE_1 = "assetFile1.png";
  private static final String ASSET_URL_1 = "assetUrl1.com";
  private static final String SOURCE_JSON =
      "{ 'sources': { 'main.json': '"
          + MAIN_JSON_CONTENT
          + "', 'grid.txt': '"
          + MAZE_FILE_CONTENT
          + "' }, 'assetUrls': { '"
          + ASSET_FILE_1
          + "': '"
          + ASSET_URL_1
          + "' } }";

  private UserProjectFileParser projectFileParser;
  private UserProjectFiles projectFiles;
  private ArgumentCaptor<TextProjectFile> textProjectFileCaptor;
  private ProjectData unitUnderTest;

  @BeforeEach
  public void setUp() {
    projectFileParser = mock(UserProjectFileParser.class);
    projectFiles = mock(UserProjectFiles.class);
    textProjectFileCaptor = ArgumentCaptor.forClass(TextProjectFile.class);
    unitUnderTest = new ProjectData(SOURCE_JSON, projectFileParser);
  }

  @Test
  public void testGetSourcesThrowsExceptionIfSourcesOrMainJsonMissing() {
    unitUnderTest = new ProjectData("{}", projectFileParser);
    Exception actual = assertThrows(InternalServerException.class, () -> unitUnderTest.loadFiles());
    assertEquals(InternalExceptionKey.INTERNAL_EXCEPTION.toString(), actual.getMessage());

    unitUnderTest = new ProjectData("{ 'sources': {} }", projectFileParser);
    actual = assertThrows(InternalServerException.class, () -> unitUnderTest.loadFiles());
    assertEquals(InternalExceptionKey.INTERNAL_EXCEPTION.toString(), actual.getMessage());
  }

  @Test
  public void testGetSourcesParsesAndReturnsSourceFiles()
      throws UserInitiatedException, InternalServerException {
    when(projectFileParser.parseFileJson(MAIN_JSON_CONTENT)).thenReturn(projectFiles);
    doNothing().when(projectFiles).addTextFile(textProjectFileCaptor.capture());

    assertSame(projectFiles, unitUnderTest.loadFiles());
    verify(projectFileParser).parseFileJson(MAIN_JSON_CONTENT);
    assertEquals(MAZE_FILE_CONTENT, textProjectFileCaptor.getValue().getFileContents());
  }

  @Test
  public void testGetAssetUrlReturnsNullIfMissing() {
    assertNull(unitUnderTest.getAssetUrl("otherFile.png"));
  }

  @Test
  public void testGetAssetUrlReturnsUrlIfPresent() {
    assertEquals(ASSET_URL_1, unitUnderTest.getAssetUrl(ASSET_FILE_1));
  }

  @Test
  public void testDoesAssetUrlExistReturnsTrueIfAssetExists() {
    assertTrue(unitUnderTest.doesAssetUrlExist(ASSET_FILE_1));
    assertFalse(unitUnderTest.doesAssetUrlExist("otherFile.png"));
  }

  @Test
  public void testAddAssetUrlAddsToJsonData() {
    final String newAssetFile = "newAsset.wav";
    final String newAssetUrl = "newAsset.com";

    unitUnderTest.addNewAssetUrl(newAssetFile, newAssetUrl);

    assertTrue(unitUnderTest.doesAssetUrlExist(newAssetFile));
    assertEquals(newAssetUrl, unitUnderTest.getAssetUrl(newAssetFile));
  }
}
