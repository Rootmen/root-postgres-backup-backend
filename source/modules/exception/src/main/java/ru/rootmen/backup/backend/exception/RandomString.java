package ru.rootmen.backup.backend.exception;

import java.security.SecureRandom;

public class RandomString {

  static SecureRandom random = null;
  static String group = "0123456789abcdef";

  public static String getRandomString(int size) {
    return getRandomString(size, group);
  }

  public static String getRandomString(int size, String group) {
    if (random == null) random = new SecureRandom();
    StringBuilder result = new StringBuilder(size);
    for (int g = 0; g < size; g++) {
      int ch = random.nextInt(0, group.length());
      result.append(group.charAt(ch));
    }
    return result.toString();
  }
}
