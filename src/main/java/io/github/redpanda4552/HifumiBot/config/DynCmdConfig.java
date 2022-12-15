// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.config;

import io.github.redpanda4552.HifumiBot.command.dynamic.DynamicCommand;
import java.util.HashMap;

public class DynCmdConfig implements IConfig {

  @Override
  public ConfigType getConfigType() {
    return ConfigType.DYNCMD;
  }

  @Override
  public boolean usePrettyPrint() {
    return true;
  }

  public HashMap<String, DynamicCommand> dynamicCommands;

  public DynCmdConfig() {
    dynamicCommands = new HashMap<String, DynamicCommand>();
  }
}
