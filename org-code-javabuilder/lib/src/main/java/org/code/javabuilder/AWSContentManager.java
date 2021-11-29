package org.code.javabuilder;

import com.amazonaws.AbortedException;
import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.code.protocol.InternalErrorKey;
import org.code.protocol.InternalServerRuntimeError;

public class AWSContentManager implements ContentManager, ProjectFileLoader {
  private static final String SOURCES_DIRECTORY_FORMAT = "%s/sources/%s";
  private static final String ASSETS_DIRECTORY_FORMAT = "%s/assets/%s";
  private static final String GENERATED_INPUT_DIRECTORY_FORMAT = "%s/generated/input/%s";
  private static final String GENERATED_OUTPUT_DIRECTORY_FORMAT = "%s/generated/output/%s";

  private static final String MAIN_JSON_FILE = "main.json";
  private static final String MAZE_FILE = "grid.txt";

  // Temporary limit on writes per session until we can more fully limit usage.
  private static final int WRITES_PER_SESSION = 2;
  // Limit on the amount of S3 file uploads per session to avoid spamming
  private static final int UPLOADS_PER_SESSION = 20;

  private final AmazonS3 s3Client;
  private final String contentBucketName;
  private final String javabuilderSessionId;
  private final String contentOutputUrl;
  private final Context context;
  private final UserProjectFileParser projectFileParser;
  private int writes;
  private int uploads;

  public AWSContentManager(
      AmazonS3 s3Client,
      String contentBucketName,
      String javabuilderSessionId,
      String contentBucketPublicUrl,
      Context context) {
    this(
        s3Client,
        contentBucketName,
        javabuilderSessionId,
        contentBucketPublicUrl,
        context,
        new UserProjectFileParser());
  }

  AWSContentManager(
      AmazonS3 s3Client,
      String contentBucketName,
      String javabuilderSessionId,
      String contentOutputUrl,
      Context context,
      UserProjectFileParser projectFileParser) {
    this.s3Client = s3Client;
    this.contentBucketName = contentBucketName;
    this.javabuilderSessionId = javabuilderSessionId;
    this.contentOutputUrl = contentOutputUrl;
    this.context = context;
    this.projectFileParser = projectFileParser;
    this.writes = 0;
    this.uploads = 0;
  }

  @Override
  public UserProjectFiles loadFiles() throws UserInitiatedException, InternalServerError {
    try {
      final byte[] mainJsonBytes =
          this.getFileContents(this.generateKey(SOURCES_DIRECTORY_FORMAT, MAIN_JSON_FILE));
      if (mainJsonBytes == null) {
        throw new InternalServerError(InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION);
      }
      final String mainJsonString = new String(mainJsonBytes, StandardCharsets.UTF_8);
      System.out.println(mainJsonString);
      final UserProjectFiles projectFiles = this.projectFileParser.parseFileJson(mainJsonString);
      final byte[] mazeBytes =
          this.getFileContents(this.generateKey(SOURCES_DIRECTORY_FORMAT, MAZE_FILE));
      if (mazeBytes != null) {
        projectFiles.addTextFile(
            new TextProjectFile(MAZE_FILE, new String(mazeBytes, StandardCharsets.UTF_8)));
      }
      return projectFiles;
    } catch (IOException e) {
      System.out.println(e);
      throw new InternalServerRuntimeError(InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION, e);
    }
  }

  @Override
  public String getAssetUrl(String filename) {
    return this.contentOutputUrl + "/" + this.generateKey(ASSETS_DIRECTORY_FORMAT, filename);
  }

  @Override
  public InputStream getFileInputStream(String filename) {
    final String key = this.generateKey(ASSETS_DIRECTORY_FORMAT, filename);
    if (!this.s3Client.doesObjectExist(this.contentBucketName, key)) {
      return null;
    }
    return this.s3Client.getObject(this.contentBucketName, key).getObjectContent();
  }

  @Override
  public String getGeneratedInputFileUrl(String filename) {
    return this.contentOutputUrl
        + "/"
        + this.generateKey(GENERATED_INPUT_DIRECTORY_FORMAT, filename);
  }

  @Override
  public String generateUploadUrl(String filename)
      throws UserInitiatedException, InternalServerError {
    if (this.uploads >= UPLOADS_PER_SESSION) {
      throw new UserInitiatedException(UserInitiatedExceptionKey.TOO_MANY_UPLOADS);
    }
    final String key = this.generateKey(GENERATED_INPUT_DIRECTORY_FORMAT, filename);
    final long expirationTimeMs = System.currentTimeMillis() + context.getRemainingTimeInMillis();

    try {
      final URL uploadUrl =
          s3Client.generatePresignedUrl(
              this.contentBucketName, key, new Date(expirationTimeMs), HttpMethod.PUT);
      this.uploads++;
      return this.contentOutputUrl + uploadUrl.getFile();
    } catch (AbortedException e) {
      // this is most likely because the end user interrupted program execution. We can safely
      // ignore this.
    } catch (SdkClientException e) {
      throw new InternalServerError(InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION, e);
    }

    return null;
  }

  @Override
  public String writeToFile(String filename, byte[] inputBytes, String contentType)
      throws UserInitiatedException, InternalServerError {
    if (this.writes >= WRITES_PER_SESSION) {
      throw new UserInitiatedException(UserInitiatedExceptionKey.TOO_MANY_WRITES);
    }
    String filePath = this.generateKey(GENERATED_OUTPUT_DIRECTORY_FORMAT, filename);
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType(contentType);
    metadata.setContentLength(inputBytes.length);
    ByteArrayInputStream inputStream = new ByteArrayInputStream(inputBytes);

    try {
      this.s3Client.putObject(this.contentBucketName, filePath, inputStream, metadata);
    } catch (AbortedException e) {
      // this is most likely because the end user interrupted program execution. We can safely
      // ignore this.
    } catch (SdkClientException e) {
      // We couldn't write to S3, send a message to the user and fail. The S3 SDK includes retries.
      throw new InternalServerError(InternalErrorKey.INTERNAL_RUNTIME_EXCEPTION, e);
    }

    this.writes++;
    return this.contentOutputUrl + "/" + filePath;
  }

  private String generateKey(String pathFormat, String filename) {
    return String.format(pathFormat, this.javabuilderSessionId, filename);
  }

  private byte[] getFileContents(String key) throws IOException {
    if (!this.s3Client.doesObjectExist(this.contentBucketName, key)) {
      return null;
    }

    final S3Object mainJsonObject = this.s3Client.getObject(this.contentBucketName, key);
    final byte[] content = mainJsonObject.getObjectContent().readAllBytes();
    mainJsonObject.close();
    return content;
  }
}
