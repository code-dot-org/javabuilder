package org.code.javabuilder;

/** String constants representing websocket channels and destinations. */
public final class Destinations {
  private Destinations() {}

  /**
   * Allows us to use @MessageMapping methods in @Controller classes in Spring. Details here:
   * https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#websocket-stomp-enable
   */
  public static final String MAGIC_DESTINATION_HEADER = "/app";

  /** Conventional prefix used to designate point-to-point messaging in Spring STOMP messages. */
  public static final String PTP_PREFIX = "/queue";

  /** The endpoint name used for the initial websocket connection. */
  public static final String APP_ENDPOINT = "/codebuilder";

  /** The channel where the client should listen to for output. */
  public static final String OUTPUT_CHANNEL = "/output";

  /** The channel where the client should send code to be compiled/run. */
  public static final String EXECUTE_CODE = "/execute";

  /** The channel where the client should send input to be passed to a running program */
  public static final String PROCESS_INPUT = "/userInput";
}
