package dev.javabuilder;

import static dev.javabuilder.LocalWebserverConstants.DIRECTORY;
import static org.code.protocol.AllowedFileNames.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This sets up an HTTP server for local development when the client needs to be able to access
 * files generated by Javabuilder (such as when using Theater). In production, this is handled by
 * S3.
 */
@WebServlet(
    name = "FileServlet",
    urlPatterns = {"/" + DIRECTORY + "/*"})
public class HttpFileServer extends HttpServlet {
  /**
   * Returns the file requested by the client. Listens at http://localhost:8080/files/<filename>.
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    System.out.println("GET: " + request.getRequestURI());
    // https://docs.oracle.com/javaee/5/api/javax/servlet/ServletResponse.html
    // https://docs.oracle.com/javaee/5/api/javax/servlet/http/HttpServletResponse.html
    // NOTE: This is _NOT_ a safe method of handling requests from a client. We are serving
    // filesystem files to the user without authentication/authorization. This should _ONLY_ be used
    // for local development.
    final String fileName = this.getFileName(request);
    if (false && !this.getAllowed(fileName)) {
      response.sendError(
          403,
          String.format(
              "Only %s, %s, and %s files can be accessed.",
              THEATER_IMAGE_NAME, THEATER_AUDIO_NAME, PROMPTER_FILE_NAME_PREFIX));
      return;
    }
    OutputStream out = response.getOutputStream();
    Files.copy(
        Paths.get(System.getProperty("java.io.tmpdir"), request.getRequestURI().substring(1)), out);
    out.flush();
  }

  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    System.out.println("PUT: " + request.getRequestURI());
    final String fileName = this.getFileName(request);
    if (!this.putAllowed(fileName)) {
      response.sendError(
          403,
          String.format("Only files prefixed with %s can be uploaded.", PROMPTER_FILE_NAME_PREFIX));
      return;
    }
    Files.copy(
        request.getInputStream(),
        Paths.get(System.getProperty("java.io.tmpdir"), request.getRequestURI().substring(1)),
        StandardCopyOption.REPLACE_EXISTING);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    System.out.println("POST: " + request.getRequestURI());
    File directory =
        Paths.get(System.getProperty("java.io.tmpdir"), request.getRequestURI().substring(1))
            .toFile();
    if (!directory.exists()) {
      directory.mkdirs();
    }
    System.out.println(
        Paths.get(System.getProperty("java.io.tmpdir"), request.getRequestURI().substring(1)));
    Files.copy(
        request.getInputStream(),
        Paths.get(System.getProperty("java.io.tmpdir"), request.getRequestURI().substring(1)),
        StandardCopyOption.REPLACE_EXISTING);
  }

  private boolean getAllowed(String fileName) {
    return fileName.equals(THEATER_IMAGE_NAME)
        || fileName.equals(THEATER_AUDIO_NAME)
        || fileName.indexOf(PROMPTER_FILE_NAME_PREFIX) == 0;
  }

  private boolean putAllowed(String fileName) {
    return fileName.indexOf(PROMPTER_FILE_NAME_PREFIX) == 0;
  }

  private String getFileName(HttpServletRequest request) {
    final String[] urlParts = request.getRequestURI().split("/");
    return urlParts[urlParts.length - 1];
  }
}
