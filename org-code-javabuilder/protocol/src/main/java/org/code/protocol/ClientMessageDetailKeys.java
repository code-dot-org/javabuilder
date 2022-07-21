package org.code.protocol;

/** Expected keys in the optional detail object of {@link ClientMessage}s */
public class ClientMessageDetailKeys {
  public static final String FILENAME = "filename";
  public static final String HEIGHT = "height";
  public static final String WIDTH = "width";
  public static final String X = "x";
  public static final String Y = "y";
  public static final String ID = "id";

  // Theater specific
  public static final String PROMPT = "prompt";
  public static final String UPLOAD_URL = "uploadUrl";
  public static final String PROGRESS_TIME = "progressTime";
  public static final String TOTAL_TIME = "totalTime";
  public static final String URL = "url";

  // Exception specific
  public static final String CONNECTION_ID = "connectionId";
  public static final String CAUSE = "cause";
  public static final String CAUSE_MESSAGE = "causeMessage";
  public static final String STACK_TRACE = "stackTrace";
  public static final String EXCEPTION_MESSAGE = "exceptionMessage";

  // Message used until translation is set up on the front end
  public static final String FALLBACK_MESSAGE = "fallbackMessage";

  // User test result specific
  public static final String STATUS = "status";
  public static final String CLASS_NAME = "className";
  public static final String METHOD_NAME = "methodName";
  public static final String ASSERTION_ERROR = "assertionError";
  public static final String FILE_NAME = "fileName";
  public static final String ERROR_LINE = "errorLine";
  public static final String IS_VALIDATION = "isValidation";
  public static final String EXCEPTION_NAME = "exceptionName";
  public static final String TYPE = "type";

  // Neighborhood specific
  public static final String DIRECTION = "direction";
  public static final String COLOR = "color";
  public static final String PAINT = "paint";
}
