// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.filter;

import java.util.HashMap;

public class Filter {
  public String name;
  public HashMap<String, String> regexes;
  public String replyMessage;

  public Filter() {
    this.name = new String("");
    this.regexes = new HashMap<String, String>();
    this.replyMessage = new String("");
  }
}
