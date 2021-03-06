package org.code.lambda.javabuilder;

public class OutputHandler extends Thread {
  public OutputHandler(){}
  public String processMessage(String message) {
    return "you wrote " + message;
  }
  // public void run() {
  //   while(true) {
  //     try {
  //       sleep(30000);
  //     } catch (InterruptedException e) {
  //       // TODO Auto-generated catch block
  //       e.printStackTrace();
  //     }
  //     System.out.println("sending output");
  //     // Send output to API Gateway
  //   }
  // }
}
