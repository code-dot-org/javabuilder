package dev.javabuilder;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This sets up an HTTP server for local development when the client needs to be able to access
 * files generated by Javabuilder (such as when using Theater). In production, this is handled by
 * S3.
 */
@WebServlet(name = "FileServlet", urlPatterns = "/files/*")
public class HttpFileServer extends HttpServlet {
  /**
   * Returns the file requested by the client. Listens at http://localhost:8080/files/<filename>.
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    // https://docs.oracle.com/javaee/5/api/javax/servlet/ServletResponse.html
    // https://docs.oracle.com/javaee/5/api/javax/servlet/http/HttpServletResponse.html
    // NOTE: This is _NOT_ a safe method of handling requests from a client. We are naively serving
    // whatever file is asked for by the user. This should _ONLY_ be used for local development.
    String[] urlParts = request.getRequestURI().split("/");
    String fileName = urlParts[urlParts.length - 1];
    File file = new File(fileName);
    OutputStream out = response.getOutputStream();
    Path path = file.toPath();
    Files.copy(path, out);
    out.flush();
  }
}
