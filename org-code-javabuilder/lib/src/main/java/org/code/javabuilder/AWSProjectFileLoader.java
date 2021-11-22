package org.code.javabuilder;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import java.nio.charset.StandardCharsets;

public class AWSProjectFileLoader implements ProjectFileLoader {

  private final AmazonS3 s3Client;
  private final String sourcesBucket;
  private final String sourcesPath;

  public AWSProjectFileLoader(AmazonS3 s3Client, String sourcesBucket, String sourcesPath) {
    this.s3Client = s3Client;
    this.sourcesBucket = sourcesBucket;
    this.sourcesPath = sourcesPath;
  }

  @Override
  public UserProjectFiles loadFiles() throws InternalServerError, UserInitiatedException {
    try {
      final S3Object object =
          this.s3Client.getObject(this.sourcesBucket, this.sourcesPath + "/main.json");
      final byte[] content = object.getObjectContent().readAllBytes();
      final String jsonString = new String(content, StandardCharsets.UTF_8);
      System.out.println(jsonString);
      return new UserProjectFileParser().parseFileJson(jsonString);
    } catch (Exception e) {
      System.out.println(e);
    }
    return null;
  }
}
