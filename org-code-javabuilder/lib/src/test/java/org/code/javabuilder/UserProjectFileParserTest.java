package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
