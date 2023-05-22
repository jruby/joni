package org.joni;

public class ConfigSupport {
    static boolean getBoolean(String property, boolean def) {
        String value = System.getProperty(property, def ? "true" : "false");
        return !value.equals("false");
    }

  static int getInt(String property, int def) {
    String value = System.getProperty(property);
    return value != null ? Integer.parseInt(value) : def;
  }
}
