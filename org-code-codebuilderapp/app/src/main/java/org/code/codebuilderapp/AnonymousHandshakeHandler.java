package org.code.codebuilderapp;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

/**
 * Assignes a unique id to each handshake to allow messages to be directed
 * to specific anonymous users.
 */
public class AnonymousHandshakeHandler extends DefaultHandshakeHandler {

  /**
   * Set anonymous user (Principal) in WebSocket messages by using UUID. This is
   * necessary to avoid broadcasting messages to all users and instead send them
   * to specific user sessions.
   */
  @Override
  protected Principal determineUser(
    ServerHttpRequest request,
    WebSocketHandler wsHandler,
    Map<String, Object> attributes
  ) {
    // Assign UUID to a user (Principal)
    return new AnonymousUser(UUID.randomUUID().toString());
  }
}
