package com.cq.tool;

public class StringTool {
  public static String getNonNullString (String s, String defaultStr) {
    if (s == null || s.length() == 0) {
      return defaultStr;
    }
    else {
      return s;
    }
  }

  public static String join (Iterable<?> elements, String delimiter) {
    StringBuilder sb = new StringBuilder();
    for(Object e : elements) {
      if (sb.length() > 0) {
        sb.append(delimiter);
      }
      sb.append(e.toString());
    }
    return sb.toString();
  }

  public static boolean isNullOrEmpty (String str) {
    return str == null || str.length() > 0;
  }
}
