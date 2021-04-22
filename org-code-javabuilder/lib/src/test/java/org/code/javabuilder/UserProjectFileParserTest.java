package org.code.javabuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
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
    List<JavaProjectFile> javaFiles = new ArrayList<>();
    List<TextProjectFile> textFiles = new ArrayList<>();
    this.userProjectFileParser.parseFileJson(validJson, javaFiles, textFiles);
    assertEquals(textFiles.size(), 0);
    assertEquals(javaFiles.size(), 1);
    ProjectFile firstFile = javaFiles.get(0);
    assertEquals(firstFile.getFileName(), "HelloWorld.java");
    assertEquals(firstFile.getFileContents(), expectedCode);
  }

  @Test
  public void throwsExceptionOnInvalidJson() throws UserFacingException, UserInitiatedException {
    String invalidJson =
        "{\"source\":{\"HelloWorld.java\":{\"text\":\"public class HelloWorld {\\n";

    List<JavaProjectFile> javaFiles = new ArrayList<>();
    List<TextProjectFile> textFiles = new ArrayList<>();
    assertThrows(
        UserFacingException.class,
        () -> this.userProjectFileParser.parseFileJson(invalidJson, javaFiles, textFiles));
  }

  @Test
  public void canParseMultipleFiles() throws UserFacingException, UserInitiatedException {
    String validJson =
        "{\"source\":{\"HelloWorld.java\":{\"text\":\"my code\",\"visible\":true},"
            + "\"HelloWorld2.java\":{\"text\":\"my code\",\"visible\":true},"
            + "\"test.txt\":{\"text\":\"my text\",\"visible\":true}},\"animations\":{}}";

    List<JavaProjectFile> javaFiles = new ArrayList<>();
    List<TextProjectFile> textFiles = new ArrayList<>();
    this.userProjectFileParser.parseFileJson(validJson, javaFiles, textFiles);
    assertEquals(javaFiles.size(), 2);
    assertEquals(javaFiles.get(0).getFileName(), "HelloWorld.java");
    assertEquals(javaFiles.get(1).getFileName(), "HelloWorld2.java");
    assertEquals(textFiles.size(), 1);
    assertEquals(textFiles.get(0).getFileName(), "test.txt");
  }
}
