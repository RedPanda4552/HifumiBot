// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.util;

public class Strings {

  public static String unescapeNewlines(String input) {
    return input.replace("\\n", "\n");
  }
}
