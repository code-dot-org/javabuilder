package org.code.protocol;

/** A throwable that can be logged and produce a client-facing error message */
public interface JavabuilderThrowableProtocol extends LoggableProtocol {
  JavabuilderThrowableMessage getExceptionMessage();
}
