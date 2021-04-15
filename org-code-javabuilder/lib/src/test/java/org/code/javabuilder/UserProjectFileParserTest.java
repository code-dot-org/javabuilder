package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserProjectFileParserTest {
  private UserProjectFileParser userProjectFileParser;

  @BeforeEach
  public void setup() {
    this.userProjectFileParser = new UserProjectFileParser();
  }

  @Test
  public void canParseValidFileJson() throws UserFacingException, UserInitiatedException {
    String validJson =
        "{\"source\":{\"HelloWorld.java\":{\"text\":\"public class HelloWorld {\\n"
            + "  public static void main(String[] args) {\\n    System.out.println(\\\"Hello World\\\");"
            + "\\n  }\\n}\",\"visible\":true}},\"animations\":{}}";
    String expectedCode =
        "public class HelloWorld {\n"
            + "  public static void main(String[] args) {\n    System.out.println(\"Hello World\");"
            + "\n  }\n}";
    List<ProjectFile> files = this.userProjectFileParser.parseFileJson(validJson);
    assertEquals(files.size(), 1);
    ProjectFile firstFile = files.get(0);
    assertEquals(firstFile.getFileName(), "HelloWorld.java");
    assertEquals(firstFile.getCode(), expectedCode);
  }

  @Test
  public void throwsExceptionOnInvalidJson() throws UserFacingException, UserInitiatedException {
    String invalidJson =
        "{\"source\":{\"HelloWorld.java\":{\"text\":\"public class HelloWorld {\\n";
    assertThrows(
        UserFacingException.class, () -> this.userProjectFileParser.parseFileJson(invalidJson));
  }

  @Test
  public void canParseMultipleFiles() throws UserFacingException, UserInitiatedException {
    String validJson =
        "{\n"
            + "  \"source\": {\n"
            + "    \"ScannerSample.java\": {\n"
            + "      \"text\": \"import java.util.Scanner;\\n\\npublic class ScannerSample {\\n\\tpublic static void main(String[] args) {\\n\\t\\tHelperClass.printWelcomeMessage();\\n\\t\\tScanner console = new Scanner(System.in);\\n\\t\\tSystem.out.println(\\\"Enter your name: \\\");\\n\\t\\tString name = console.next();\\n\\t\\tSystem.out.println(\\\"Hello \\\" + name);\\n\\t}\\n}\\n\",\n"
            + "      \"visible\": true\n"
            + "    },\n"
            + "    \"HelperClass.java\": {\n"
            + "      \"text\": \"\\npublic class HelperClass {\\n\\tpublic static void printWelcomeMessage() {\\n\\t\\tSystem.out.println(\\\"Hello!\\\");\\n\\t}\\n}\\n\",\n"
            + "      \"visible\": true\n"
            + "    }\n"
            + "  },\n"
            + "  \"animations\": {}\n"
            + "}";
    List<ProjectFile> files = this.userProjectFileParser.parseFileJson(validJson);
    assertEquals(files.size(), 2);
    assertEquals(files.get(0).getFileName(), "ScannerSample.java");
    assertEquals(files.get(1).getFileName(), "HelperClass.java");
  }
}
