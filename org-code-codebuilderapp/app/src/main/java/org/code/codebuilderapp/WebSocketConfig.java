package org.code.codebuilderapp;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configures websocket channels to send messages to specific users
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  /**
   * Sets up the channels that a client can subscribe to. Specifically, a client
   * would subscribe to /topic/destination and would send messages to
   * /app/destination.
   */
  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/topic");
    config.setApplicationDestinationPrefixes("/app");
  }

  /**
   * Sets up a custom endpoint where users will initially connect. Overrides
   * the HandshakeHandler in order to return messages to specific users rather
   * than the entire channel.
   */
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry
      .addEndpoint("/codebuilder")
      .setHandshakeHandler(new AnonymousHandshakeHandler());

    registry
      .addEndpoint("/codebuilder")
      .setHandshakeHandler(new AnonymousHandshakeHandler())
      .withSockJS();
  }
}
