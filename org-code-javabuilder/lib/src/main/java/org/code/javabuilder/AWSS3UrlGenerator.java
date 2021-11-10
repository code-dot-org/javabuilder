package org.code.javabuilder;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import java.net.URL;
import org.code.protocol.JavabuilderUploadUrlGenerator;

public class AWSS3UrlGenerator implements JavabuilderUploadUrlGenerator {
  private final AmazonS3 s3Client;
  private final String bucketName;
  private final String javabuilderSessionId;
  private final String bucketUrl;

  public AWSS3UrlGenerator(
      AmazonS3 s3client, String bucketName, String javabuilderSessionId, String bucketUrl) {
    this.s3Client = s3client;
    this.bucketName = bucketName;
    this.javabuilderSessionId = javabuilderSessionId;
    this.bucketUrl = bucketUrl;
  }

  public String getSignedUrl(String filename) {
    final String key = this.javabuilderSessionId + '/' + filename;
    final URL url = s3Client.generatePresignedUrl(this.bucketName, key, null, HttpMethod.PUT);
    return this.bucketUrl + '/' + url.getPath();
  }
}
