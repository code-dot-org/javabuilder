package dev.javabuilder;

import static dev.javabuilder.LocalWebserverConstants.DIRECTORY;
import static org.code.protocol.AllowedFileNames.PROMPTER_FILE_NAME_PREFIX;

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
    System.out.println("In the cors filter");
    final String requestOrigin = request.getHeader("Origin");
    if (requestOrigin == null || !ALLOWED_ORIGINS.contains(requestOrigin)) {
      // May be an internal request or an unknown origin. Pass along the request without adding any
      // headers.
      chain.doFilter(request, response);
      return;
    }

    final String[] urlParts = request.getRequestURI().split("/");
    final String fileName = urlParts[urlParts.length - 1];
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
}
