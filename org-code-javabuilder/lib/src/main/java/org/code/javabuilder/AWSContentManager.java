package org.code.javabuilder;

import com.amazonaws.AbortedException;
import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.code.protocol.ContentManager;
import org.code.protocol.InternalErrorKey;
import org.code.protocol.JavabuilderException;
import org.code.protocol.LoggerUtils;

public class AWSContentManager implements ContentManager, ProjectFileLoader {
  // Temporary limit on writes per session until we can more fully limit usage.
  private static final int WRITES_PER_SESSION = 2;
  // Limit on the amount of S3 file uploads per session to avoid spamming
  private static final int UPLOADS_PER_SESSION = 20;

  private final String bucketName;
  private final AmazonS3 s3Client;
  private final String javabuilderSessionId;
  private final String getContentUrl;
  private final Context context;
  private ProjectData projectData;
  private int writes;
  private int uploads;

  public AWSContentManager(
      AmazonS3 s3Client,
      String bucketName,
      String javabuilderSessionId,
      String getContentUrl,
      Context context) {
    this(s3Client, bucketName, javabuilderSessionId, getContentUrl, context, null);
  }

  AWSContentManager(
      AmazonS3 s3Client,
      String bucketName,
      String javabuilderSessionId,
      String getContentUrl,
      Context context,
      ProjectData projectData) {
    this.bucketName = bucketName;
    this.s3Client = s3Client;
    this.javabuilderSessionId = javabuilderSessionId;
    this.getContentUrl = getContentUrl;
    this.context = context;
    this.projectData = projectData;
    this.writes = 0;
    this.uploads = 0;
  }

  @Override
  public UserProjectFiles loadFiles() throws InternalServerError, UserInitiatedException {
    this.loadProjectDataIfNeeded();
    return this.projectData.getSources();
  }

  @Override
  public String getAssetUrl(String filename) {
    try {
      this.loadProjectDataIfNeeded();
    } catch (InternalServerError e) {
      // We should only hit this exception if we try to load an asset URL before source code has
      // been loaded, which should only be in the the case of manual testing. Log this exception but
      // don't throw to preserve the method contract.
      // Note / TODO: Once we fully migrate away from Dashboard sources, we can remove the
      // loadProjectDataIfNeeded() call here and this exception handling.
      LoggerUtils.logException(e);
      return null;
    }
    return this.projectData.getAssetUrl(filename);
  }

  @Override
  public String generateAssetUploadUrl(String filename) throws JavabuilderException {
    if (this.uploads >= UPLOADS_PER_SESSION) {
      throw new UserInitiatedException(UserInitiatedExceptionKey.TOO_MANY_UPLOADS);
    }
    final String key = this.generateKey(filename);
    final long expirationTimeMs = System.currentTimeMillis() + context.getRemainingTimeInMillis();

    try {
      final URL presignedUrl =
          s3Client.generatePresignedUrl(
              this.bucketName, key, new Date(expirationTimeMs), HttpMethod.PUT);
      this.uploads++;
      // Add the GET url for this file to the asset map so it can be referenced later.
      this.projectData.addNewAssetUrl(filename, this.getContentUrl + "/" + key);
      return this.getContentUrl + presignedUrl.getFile();
    } catch (AbortedException e) {
      // this is most likely because the end user interrupted program execution. We can safely
      // ignore this.
    } catch (SdkClientException e) {
      throw new InternalServerError(InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION, e);
    }

    return null;
  }

  @Override
  public String writeToOutputFile(String filename, byte[] inputBytes, String contentType)
      throws JavabuilderException {
    if (this.writes >= WRITES_PER_SESSION) {
      throw new UserInitiatedException(UserInitiatedExceptionKey.TOO_MANY_WRITES);
    }
    String filePath = this.generateKey(filename);
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType(contentType);
    metadata.setContentLength(inputBytes.length);
    ByteArrayInputStream inputStream = new ByteArrayInputStream(inputBytes);

    try {
      this.s3Client.putObject(this.bucketName, filePath, inputStream, metadata);
    } catch (AbortedException e) {
      // this is most likely because the end user interrupted program execution. We can safely
      // ignore this.
    } catch (SdkClientException e) {
      // We couldn't write to S3, send a message to the user and fail. The S3 SDK includes retries.
      throw new InternalServerError(InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION, e);
    }

    this.writes++;
    return this.getContentUrl + "/" + filePath;
  }

  @Override
  public void verifyAssetFilename(String filename) throws FileNotFoundException {
    try {
      // We should only hit this exception if we try to verify an asset before source code has
      // been loaded, which should only be in the the case of manual testing. Log this exception but
      // convert to a FileNotFoundException to preserve the method contract.
      // Note / TODO: Once we fully migrate away from Dashboard sources, we can remove the
      // loadProjectDataIfNeeded() call here and this exception handling.
      this.loadProjectDataIfNeeded();
    } catch (InternalServerError e) {
      throw new FileNotFoundException("Error loading data");
    }
    if (!this.projectData.doesAssetUrlExist(filename)) {
      throw new FileNotFoundException(filename);
    }
  }

  private String generateKey(String filename) {
    return this.javabuilderSessionId + "/" + filename;
  }

  // TODO: Project JSON data loading is deferred because it will not exist if we are
  // still using dashboard sources. Once we stop using dashboard sources, this step
  // can probably just happen immediately in the constructor.
  private void loadProjectDataIfNeeded() throws InternalServerError {
    if (this.projectData != null) {
      return;
    }

    final String key = this.generateKey(ProjectData.PROJECT_DATA_FILE_NAME);
    final S3Object sourcesS3Object;

    try {
      sourcesS3Object = this.s3Client.getObject(this.bucketName, key);
    } catch (SdkClientException e) {
      throw new InternalServerError(InternalErrorKey.INTERNAL_EXCEPTION, e);
    }

    try (final S3ObjectInputStream inputStream = sourcesS3Object.getObjectContent()) {
      final String jsonString = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
      this.projectData = new ProjectData(jsonString);
    } catch (IOException e) {
      // Error reading JSON file from S3
      throw new InternalServerError(InternalErrorKey.INTERNAL_EXCEPTION, e);
    }
  }
}
