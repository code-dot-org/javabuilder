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
  public void canParseValidFileJson() throws InternalServerError, UserInitiatedException {
    String validJson =
        "{\"source\":{\"HelloWorld.java\":{\"text\":\"my code\",\"visible\":true}},\"animations\":{}}";
    String expectedCode = "my code";
    UserProjectFiles projectFiles = this.userProjectFileParser.parseFileJson(validJson);
    assertEquals(projectFiles.getTextFiles().size(), 0);
    assertEquals(projectFiles.getJavaFiles().size(), 1);
    ProjectFile firstFile = projectFiles.getJavaFiles().get(0);
    assertEquals(firstFile.getFileName(), "HelloWorld.java");
    assertEquals(firstFile.getFileContents(), expectedCode);
  }

  @Test
  public void throwsExceptionOnInvalidJson() throws InternalServerError, UserInitiatedException {
    String invalidJson =
        "{\"source\":{\"HelloWorld.java\":{\"text\":\"public class HelloWorld {\\n";

    assertThrows(
        InternalServerError.class, () -> this.userProjectFileParser.parseFileJson(invalidJson));
  }

  @Test
  public void canParseMultipleFiles() throws InternalServerError, UserInitiatedException {
    String validJson =
        "{\"source\":{\"HelloWorld.java\":{\"text\":\"my code\",\"visible\":true},"
            + "\"HelloWorld2.java\":{\"text\":\"my code\",\"visible\":true},"
            + "\"test.txt\":{\"text\":\"my text\",\"visible\":true}},\"animations\":{}}";

    UserProjectFiles projectFiles = this.userProjectFileParser.parseFileJson(validJson);
    List<JavaProjectFile> javaFiles = projectFiles.getJavaFiles();
    List<TextProjectFile> textFiles = projectFiles.getTextFiles();
    assertEquals(javaFiles.size(), 2);
    assertEquals(javaFiles.get(0).getFileName(), "HelloWorld.java");
    assertEquals(javaFiles.get(1).getFileName(), "HelloWorld2.java");
    assertEquals(textFiles.size(), 1);
    assertEquals(textFiles.get(0).getFileName(), "test.txt");
  }
}
