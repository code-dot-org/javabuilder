package dev.javabuilder;

import static dev.javabuilder.LocalWebserverConstants.DIRECTORY;
import static org.code.protocol.AllowedFileNames.*;

import java.io.IOException;
import java.util.List;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A {@link HttpFilter} for local development that upgrades specific known and trusted requests by
 * adding necessary CORS headers. In production, this is handled by S3.
 *
 * <p>https://www.oracle.com/java/technologies/filters.html
 */
@WebFilter(urlPatterns = {"/" + DIRECTORY + "/*"})
public class CORSFilter extends HttpFilter {

  private static final List<String> ALLOWED_ORIGINS =
      List.of("http://localhost-studio.code.org:3000", "http://127.0.0.1:3000");

  @Override
  public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    final String requestOrigin = request.getHeader("Origin");

    // For all internal get requests, pass along the request.
    if (requestOrigin == null && request.getMethod().equals("GET")) {
      chain.doFilter(request, response);
      return;
    }

    // if not one of three allowed files, reject
    final String[] urlParts = request.getRequestURI().split("/");
    final String fileName = urlParts[urlParts.length - 1];
    if (!this.getAllowedExternal(fileName)) {
      response.sendError(
          403,
          String.format(
              "Only %s, %s, and %s files can be accessed.",
              THEATER_IMAGE_NAME, THEATER_AUDIO_NAME, PROMPTER_FILE_NAME_PREFIX));
      return;
    }

    // if from unknown origin, can still get theater image and audio
    if (requestOrigin != null && !ALLOWED_ORIGINS.contains(requestOrigin)) {
      chain.doFilter(request, response);
      return;
    }

    if (fileName.indexOf(PROMPTER_FILE_NAME_PREFIX) == 0) {
      // Add CORS headers only if the request is for a known file
      response.addHeader("Access-Control-Allow-Origin", requestOrigin);
      response.addHeader("Access-Control-Allow-Headers", "*");
      response.addHeader("Access-Control-Allow-Methods", "GET, OPTIONS, PUT");
    }

    if (request.getMethod().equals("OPTIONS")) {
      response.setStatus(HttpServletResponse.SC_ACCEPTED);
      return;
    }

    chain.doFilter(request, response);
  }

  private boolean getAllowedExternal(String fileName) {
    return fileName.equals(THEATER_IMAGE_NAME)
        || fileName.equals(THEATER_AUDIO_NAME)
        || fileName.indexOf(PROMPTER_FILE_NAME_PREFIX) == 0;
  }
}
