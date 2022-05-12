package org.code.javabuilder;

import static org.code.javabuilder.DashboardConstants.DASHBOARD_DOMAIN_SUFFIX;

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
import org.code.protocol.InternalExceptionKey;
import org.code.protocol.JavabuilderException;
import org.code.protocol.Properties;
import org.json.JSONException;

public class AWSContentManager implements ContentManager {
  // Temporary limit on writes to S3 per session until we can more fully limit usage.
  // The only file writing should be during Theater, and there should only be two per session
  // (theater image and theater audio).
  private static final int WRITES_PER_SESSION = 2;
  // Limit on the amount of S3 file uploads per session to avoid spamming. S3 file uploads should
  // only happen in projects using the Prompter feature. This will cap the total amount of prompter
  // calls per session.
  private static final int UPLOADS_PER_SESSION = 20;

  private final String bucketName;
  private final AmazonS3 s3Client;
  private final String javabuilderSessionId;
  private final String contentBucketUrl;
  private final Context context;
  private final ProjectData projectData;
  private final AssetFileStubber assetFileStubber;
  private int writes;
  private int uploads;

  public AWSContentManager(
      AmazonS3 s3Client,
      String bucketName,
      String javabuilderSessionId,
      String contentBucketUrl,
      Context context)
      throws InternalServerException {
    this.bucketName = bucketName;
    this.s3Client = s3Client;
    this.javabuilderSessionId = javabuilderSessionId;
    this.contentBucketUrl = contentBucketUrl;
    this.context = context;
    this.projectData = this.loadProjectData();
    this.assetFileStubber = new AssetFileStubber();
    this.writes = 0;
    this.uploads = 0;
  }

  AWSContentManager(
      AmazonS3 s3Client,
      String bucketName,
      String javabuilderSessionId,
      String contentBucketUrl,
      Context context,
      ProjectData projectData,
      AssetFileStubber assetFileStubber) {
    this.bucketName = bucketName;
    this.s3Client = s3Client;
    this.javabuilderSessionId = javabuilderSessionId;
    this.contentBucketUrl = contentBucketUrl;
    this.context = context;
    this.projectData = projectData;
    this.assetFileStubber = assetFileStubber;
    this.writes = 0;
    this.uploads = 0;
  }

  public ProjectFileLoader getProjectFileLoader() {
    return this.projectData;
  }

  @Override
  public String getAssetUrl(String filename) {
    final String url = this.projectData.getAssetUrl(filename);
    if (url == null) {
      return null;
    }

    // If this asset file refers to a Dashboard URL, and Javabuilder cannot access Dashboard for
    // assets, Javabuilder won't be able access this file. Use a stubbed file URL instead.
    if (this.isUrlFromDashboard(url) && !Properties.canAccessDashboardAssets()) {
      return this.assetFileStubber.getStubAssetUrl(filename);
    }

    return url;
  }

  @Override
  public String generateAssetUploadUrl(String filename) throws JavabuilderException {
    if (this.uploads >= UPLOADS_PER_SESSION) {
      throw new UserInitiatedException(
          UserInitiatedExceptionKey.TOO_MANY_UPLOADS,
          String.format(
              "Too many Prompter images. We currently support up to %s Prompter images per project.\n",
              UPLOADS_PER_SESSION));
    }
    final String key = this.generateKey(filename);
    final long expirationTimeMs = System.currentTimeMillis() + context.getRemainingTimeInMillis();

    try {
      final URL presignedUrl =
          s3Client.generatePresignedUrl(
              this.bucketName, key, new Date(expirationTimeMs), HttpMethod.PUT);
      this.uploads++;
      // Add the GET url for this file to the asset map so it can be referenced later.
      this.projectData.addNewAssetUrl(filename, this.contentBucketUrl + "/" + key);
      return this.contentBucketUrl + presignedUrl.getFile();
    } catch (AbortedException e) {
      // this is most likely because the end user interrupted program execution. We can safely
      // ignore this.
    } catch (SdkClientException e) {
      throw new InternalServerException(InternalExceptionKey.INTERNAL_RUNTIME_EXCEPTION, e);
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
      throw new InternalServerException(InternalExceptionKey.INTERNAL_RUNTIME_EXCEPTION, e);
    }

    this.writes++;
    return this.contentBucketUrl + "/" + filePath;
  }

  @Override
  public void verifyAssetFilename(String filename) throws FileNotFoundException {
    if (!this.projectData.doesAssetUrlExist(filename)) {
      throw new FileNotFoundException(filename);
    }
  }

  /**
   * Generates the S3 key for this file. All files are stored in the Javabuilder S3 bucket under a
   * sub-folder named after the current session's ID, so this returns "<session ID>/<file name>"
   */
  private String generateKey(String filename) {
    return this.javabuilderSessionId + "/" + filename;
  }

  private boolean isUrlFromDashboard(String url) {
    return url.contains(DASHBOARD_DOMAIN_SUFFIX);
  }

  private ProjectData loadProjectData() throws InternalServerException {
    final String key = this.generateKey(ProjectData.PROJECT_DATA_FILE_NAME);
    final S3Object sourcesS3Object;

    try {
      sourcesS3Object = this.s3Client.getObject(this.bucketName, key);
    } catch (SdkClientException e) {
      throw new InternalServerException(InternalExceptionKey.INTERNAL_EXCEPTION, e);
    }

    try (final S3ObjectInputStream inputStream = sourcesS3Object.getObjectContent()) {
      final String jsonString = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
      return new ProjectData(jsonString);
    } catch (JSONException | IOException e) {
      // Error reading JSON file from S3
      throw new InternalServerException(InternalExceptionKey.INTERNAL_EXCEPTION, e);
    }
  }
}
