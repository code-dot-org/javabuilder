package org.code.lambda.javabuilder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * An app that compiles and runs Java passed from a client via a websocket or similar connection.
 */
@SpringBootApplication
public class JavaBuilderApplication {

  /**
   * The entrypoint for the spring application. Built using this guide:
   * https://spring.io/guides/gs/messaging-stomp-websocket/
   */
  public static void main(String[] args) {
    SpringApplication.run(JavaBuilderApplication.class, args);
  }
}
