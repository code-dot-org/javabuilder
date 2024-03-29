package dev.javabuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.javabuilder.util.LocalStorageUtils;
import java.io.FileNotFoundException;
import org.code.javabuilder.InternalServerException;
import org.code.javabuilder.ProjectData;
import org.code.javabuilder.UserProjectFiles;
import org.code.protocol.JavabuilderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LocalContentManagerTest {

  private ProjectData projectData;
  private UserProjectFiles projectFiles;
  private LocalContentManager unitUnderTest;

  @BeforeEach
  public void setUp() {
    LocalStorageUtils.createLocalStorageIfNeeded();
    projectData = mock(ProjectData.class);
    projectFiles = mock(UserProjectFiles.class);
    unitUnderTest = new LocalContentManager(projectData);
  }

  @Test
  public void testGenerateAssetUrlReturnsUrlFromProjectData() {
    final String filename = "file";
    final String url = "url";
    when(projectData.getAssetUrl(filename)).thenReturn(url);
    assertEquals(url, unitUnderTest.getAssetUrl(filename));
  }

  @Test
  public void testGenerateUploadUrlCreatesUrlAndAddsToProjectData() throws InternalServerException {
    final String filename = "file";
    final String url = unitUnderTest.generateAssetUploadUrl(filename);
    assertTrue(url.contains(filename));
    verify(projectData).addNewAssetUrl(filename, url);
  }

  @Test
  public void testWriteToOutputFileReturnsUrlWithFileName() throws JavabuilderException {
    final String filename = "file";
    assertTrue(
        unitUnderTest.writeToOutputFile(filename, new byte[] {}, "image/png").contains(filename));
  }

  @Test
  public void testVerifyAssetFileNameThrowsExceptionIfFileNotFound() {
    final String filename = "file";
    when(projectData.doesAssetUrlExist(anyString())).thenReturn(false);
    final Exception actual =
        assertThrows(
            FileNotFoundException.class, () -> unitUnderTest.verifyAssetFilename(filename));
    assertEquals(filename, actual.getMessage());
  }
}
