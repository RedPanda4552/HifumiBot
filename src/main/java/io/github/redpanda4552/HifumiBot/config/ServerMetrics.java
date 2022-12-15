// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.config;

import java.util.HashMap;

public class ServerMetrics implements IConfig {

  @Override
  public ConfigType getConfigType() {
    return ConfigType.SERVER_METRICS;
  }

  @Override
  public boolean usePrettyPrint() {
    return false;
  }

  public HashMap<String, Integer> populationSnaps;

  public ServerMetrics() {
    populationSnaps = new HashMap<>();
  }
}
