package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserProjectFileParserTest {
  private UserProjectFileParser userProjectFileParser;

  @BeforeEach
  public void setUp() {
    this.userProjectFileParser = new UserProjectFileParser();
  }

  @Test
  public void canParseValidFileJson() throws UserFacingException, UserInitiatedException {
    String validJson =
        "{\"source\":{\"HelloWorld.java\":{\"text\":\"my code\",\"visible\":true}},\"animations\":{}}";
    String expectedCode = "my code";
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
        "{\"source\":{\"HelloWorld.java\":{\"text\":\"my code\",\"visible\":true},"
            + "\"HelloWorld2.java\":{\"text\":\"my code\",\"visible\":true}},\"animations\":{}}";
    List<ProjectFile> files = this.userProjectFileParser.parseFileJson(validJson);
    assertEquals(files.size(), 2);
    assertEquals(files.get(0).getFileName(), "HelloWorld.java");
    assertEquals(files.get(1).getFileName(), "HelloWorld2.java");
  }
}
