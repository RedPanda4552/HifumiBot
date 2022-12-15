// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.config;

import java.time.OffsetDateTime;
import java.util.HashMap;

public class WarezTracking implements IConfig {

  @Override
  public ConfigType getConfigType() {
    return ConfigType.WAREZ;
  }

  @Override
  public boolean usePrettyPrint() {
    return false;
  }

  public HashMap<String, OffsetDateTime> warezUsers;

  public WarezTracking() {
    warezUsers = new HashMap<>();
  }
}
