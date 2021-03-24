# JavaBuilder App

## Testing
To test, run the following commands from the `org-code-javabuilder` folder.
```
gradle bootRun
open localhost:8080
```
<br/>
Simple program

```
public class Foo {
  public static void main(String[] args) {
    System.out.println("Hello World");
  }
}
```
<br/>
Simple I/O program

```
import java.util.Scanner;

public class Foo {
  public static void main(String[] args) {
    System.out.println("What's your name?");
    Scanner scanner = new Scanner(System.in);
    String input = scanner.nextLine();
    System.out.println("Hello " + input);
  }
}
```
<br/>
Simple long-running program

```
public class Foo {
  public static void main(String[] args) throws Exception {
    int x = 0;
    while(x < 10) {
      x++;
      Thread.sleep(500);
      System.out.println(x + " Hello World!");
    }
  }
}
```

To test the lambda function the first time, follow the instructions here:
https://docs.aws.amazon.com/toolkit-for-jetbrains/latest/userguide/setup-toolkit.html

This step next: https://docs.aws.amazon.com/toolkit-for-jetbrains/latest/userguide/key-tasks.html#key-tasks-first-connect