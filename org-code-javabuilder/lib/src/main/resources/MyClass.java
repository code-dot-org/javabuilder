import java.util.Scanner;

public class MyClass {
  public static void main(String[] args) {
    System.out.println("What's your name???");
    Scanner scanner = new Scanner(System.in);
    String input = scanner.nextLine();
    System.out.println("Hello " + input + "!!");
  }
}
