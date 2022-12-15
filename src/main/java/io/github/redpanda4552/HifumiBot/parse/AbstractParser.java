// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.parse;

import okhttp3.MediaType;

public abstract class AbstractParser implements Runnable {

  protected static final int MAX_LINE_LENGTH = 80;
  protected static final String LINE_NUM_SEPARATOR = ", ";
  protected static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
}
