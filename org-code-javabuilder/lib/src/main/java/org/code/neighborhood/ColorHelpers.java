package org.code.neighborhood;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorHelpers {
  private static final String HEX_WEB_COLOR_PATTERN = "^#([a-fA-F0-9]{6}|[a-fA-F0-9]{3})$";
  private static final Pattern PATTERN = Pattern.compile(HEX_WEB_COLOR_PATTERN);
  // TODO: add the rest of the web colors
  private static final Set<String> WEB_COLORS =
      Set.of(
          "white", "silver", "gray", "black", "red", "maroon", "yellow", "olive", "lime", "green",
          "aqua", "teal", "blue", "navy", "fuchsia", "purple");

  public static boolean isColor(String color) {
    Matcher matcher = PATTERN.matcher(color);
    return matcher.matches() || WEB_COLORS.contains(color);
  }
}
