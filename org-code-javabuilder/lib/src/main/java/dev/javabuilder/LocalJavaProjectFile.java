package dev.javabuilder;

import org.code.javabuilder.JavaProjectFile;
import org.code.javabuilder.UserInitiatedException;
import java.io.*;
import java.nio.charset.StandardCharsets;

/** 
 * Local development-only project file that loads itself from the filesystem
 */
public class LocalJavaProjectFile extends JavaProjectFile {
    public LocalJavaProjectFile(String filename) throws UserInitiatedException, FileNotFoundException, IOException  {
        super(filename);
        File f = new File("src/main/resources/" + filename);
        System.out.println(f.getAbsolutePath());
        InputStream stream = new FileInputStream(f);
        setFileContents(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
        stream.close();
    }


}
