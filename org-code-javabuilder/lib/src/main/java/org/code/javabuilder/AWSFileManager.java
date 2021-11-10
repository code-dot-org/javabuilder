package org.code.javabuilder;

import com.amazonaws.AbortedException;
import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Date;
import org.code.protocol.*;

public class AWSFileManager implements JavabuilderFileManager {
  private final String outputBucketName;
  private final AmazonS3 s3Client;
  private final String javabuilderSessionId;
  private final String getOutputURL;
  private final Context context;
  private int writes;
  private int uploads;

  // Temporary limit on writes per session until we can more fully limit usage.
  private static final int WRITES_PER_SESSION = 2;
  // Limit on the amount of S3 file uploads per session to avoid spamming
  private static final int UPLOADS_PER_SESSION = 20;

  public AWSFileManager(
      AmazonS3 s3Client,
      String outputBucketName,
      String javabuilderSessionId,
      String getOutputURL,
      Context context) {
    this.outputBucketName = outputBucketName;
    this.s3Client = s3Client;
    this.javabuilderSessionId = javabuilderSessionId;
    this.getOutputURL = getOutputURL;
    this.context = context;
    this.writes = 0;
    this.uploads = 0;
  }

  @Override
  public String writeToFile(String filename, byte[] inputBytes, String contentType)
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
      this.s3Client.putObject(this.outputBucketName, filePath, inputStream, metadata);
    } catch (AbortedException e) {
      // this is most likely because the end user interrupted program execution. We can safely
      // ignore this.
    } catch (SdkClientException e) {
      // We couldn't write to S3, send a message to the user and fail. The S3 SDK includes retries.
      throw new InternalServerError(InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION, e);
    }

    this.writes++;
    return this.getOutputURL + "/" + filePath;
  }

  @Override
  public String getUploadUrl(String filename) throws JavabuilderException {
    if (this.uploads >= UPLOADS_PER_SESSION) {
      throw new UserInitiatedException(UserInitiatedExceptionKey.TOO_MANY_UPLOADS);
    }
    final String key = this.generateKey(filename);
    final long expirationTimeMs = System.currentTimeMillis() + context.getRemainingTimeInMillis();

    try {
      final URL uploadUrl =
          s3Client.generatePresignedUrl(
              this.outputBucketName, key, new Date(expirationTimeMs), HttpMethod.PUT);
      this.uploads++;
      return this.getOutputURL + uploadUrl.getFile();
    } catch (AbortedException e) {
      // this is most likely because the end user interrupted program execution. We can safely
      // ignore this.
    } catch (SdkClientException e) {
      throw new InternalServerError(InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION, e);
    }

    return null;
  }

  private String generateKey(String filename) {
    return this.javabuilderSessionId + "/" + filename;
  }
}
