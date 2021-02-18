package org.code.javabuilder;

public final class Destinations {
  private Destinations(){}

  /**
   * Allows us to use @MessageMapping methods in @Controller classes in Spring.
   * Details here: https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#websocket-stomp-enable
   */
  public static final String MAGIC_DESTINATION_HEADER = "/app";

  /**
   * Conventional prefix used to designate point-to-point messaging in Spring STOMP messages.
   */
  public static final String PTP_PREFIX = "/queue";

  /**
   * The endpoint name used for the initial websocket connection.
   */
  public static final String APP_ENDPOINT = "/codebuilder";

  /**
   *
   */
  public static final String OUTPUT_CHANNEL = "/output";

  /**
   *
   */
  public static final String EXECUTE_CODE = "/execute";
}
