package org.code.protocol;

import java.io.FileNotFoundException;

/**
 * Manages content (such as code sources and assets) that are used and generated during a
 * Javabuilder session.
 */
public interface ContentManager {
  /**
   * Retrieves the URL pointing to the asset file referenced by the asset file name.
   *
   * @param filename asset file name to retrieve a URL for
   * @return asset URL
   */
  String getAssetUrl(String filename);

  /**
   * Generates a URL that a client may use to upload a specific asset named by the provided file
   * name, using a PUT request.
   *
   * @param filename file to generate an asset for
   * @return upload URL
   * @throws JavabuilderException if the URL cannot be generated
   */
  String generateAssetUploadUrl(String filename) throws JavabuilderException;

  /**
   * Writes the content in bytes to a file in the current session's storage location.
   *
   * @param filename name of the file to create
   * @param inputBytes content of the file
   * @param contentType content MIME type
   * @return a URL pointing to the written file
   * @throws JavabuilderException if the file cannot be written
   */
  String writeToOutputFile(String filename, byte[] inputBytes, String contentType)
      throws JavabuilderException;

  /**
   * Verifies that the given asset exists in the session's storage location, and throws an exception
   * if not.
   *
   * @param filename file to verify
   * @throws FileNotFoundException if the file cannot be found
   */
  void verifyAssetFilename(String filename) throws FileNotFoundException;
}
