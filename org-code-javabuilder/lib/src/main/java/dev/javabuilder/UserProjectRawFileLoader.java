package dev.javabuilder;

import org.code.javabuilder.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Local dev-only file loader hack. This reads the "park.json" project in the 
 * resources/ folder, and loads all java files referenced there from the same
 * folder. This is mean to simplify local prototyping by skipping the need to 
 * convert a project into a json file.
 */
public class UserProjectRawFileLoader implements ProjectFileLoader {

    @Override
    public UserProjectFiles loadFiles() throws UserFacingException, UserInitiatedException {
        try {
            String mainJson =
                    Files.readString(
                            Paths.get(getClass().getClassLoader().getResource("park.json").toURI()));
            UserProjectFiles placeholders = new UserProjectFileParser().parseFileJson(mainJson);
            UserProjectFiles result = new UserProjectFiles();
            for (JavaProjectFile file : placeholders.getJavaFiles()) {
                result.addJavaFile(new LocalJavaProjectFile(file.getFileName()));
            }
            return result;
        } catch (IOException | URISyntaxException e) {
            throw new UserFacingException("We could not parse your files", e);
        }
    }
}
