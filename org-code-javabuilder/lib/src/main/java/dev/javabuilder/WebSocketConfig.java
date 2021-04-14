package dev.javabuilder;

//import org.springframework.context.annotation.Configuration;
//import org.springframework.messaging.simp.config.MessageBrokerRegistry;
//import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
//import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
//import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import org.code.javabuilder.InputAdapter;
import org.code.javabuilder.OutputAdapter;

import javax.websocket.Session;

//@Configuration
//@EnableWebSocketMessageBroker
public class WebSocketConfig /*extends AbstractWebSocketMessageBrokerConfigurer*/ {
  private static WebSocketInputAdapter inputAdapter;
  private static OutputAdapter outputAdapter;
  private static Session session;

  public static void setInputAdapter(WebSocketInputAdapter inputAdapter) {
    WebSocketConfig.inputAdapter = inputAdapter;
  }

  public static WebSocketInputAdapter getInputAdapter() {
    return inputAdapter;
  }

  public static void setOutputAdapter(OutputAdapter outputAdapter) {
    WebSocketConfig.outputAdapter = outputAdapter;
  }

  public static OutputAdapter getOutputAdapter() {
    return outputAdapter;
  }

  public static void setSession(Session session) {
    WebSocketConfig.session = session;
  }

  public static Session getSession() {
    return session;
  }
//
//  @Override
//  public void configureMessageBroker(MessageBrokerRegistry config) {
//    config.enableSimpleBroker("/topic");
//    config.setApplicationDestinationPrefixes("/app");
//  }
//
//  @Override
//  public void registerStompEndpoints(StompEndpointRegistry registry) {
//    registry.addEndpoint("/chat");
//    registry.addEndpoint("/chat").withSockJS();
//  }
}
