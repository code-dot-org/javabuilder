package org.code.javabuilder;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3;
import java.net.URL;
import java.util.Date;
import org.code.protocol.JavabuilderUploadUrlGenerator;

public class AWSS3UrlGenerator implements JavabuilderUploadUrlGenerator {
  private final Context context;
  private final AmazonS3 s3Client;
  private final String bucketName;
  private final String javabuilderSessionId;
  private final String bucketUrl;

  public AWSS3UrlGenerator(
      Context context,
      AmazonS3 s3client,
      String bucketName,
      String javabuilderSessionId,
      String bucketUrl) {
    this.context = context;
    this.s3Client = s3client;
    this.bucketName = bucketName;
    this.javabuilderSessionId = javabuilderSessionId;
    this.bucketUrl = bucketUrl;
  }

  public String getSignedUrl(String filename) {
    final String key = this.javabuilderSessionId + '/' + filename;
    final long expirationTimeMs =
        System.currentTimeMillis() + this.context.getRemainingTimeInMillis();
    final URL url =
        s3Client.generatePresignedUrl(
            this.bucketName, key, new Date(expirationTimeMs), HttpMethod.PUT);
    return this.bucketUrl + '/' + url.getPath();
  }

  public String getFileUrl(String filename) {
    return this.bucketUrl + '/' + this.javabuilderSessionId + '/' + filename;
  }
}
