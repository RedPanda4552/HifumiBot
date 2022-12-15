// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.command.dynamic;

import java.util.HashMap;
import lombok.Getter;

@Getter
public class DynamicCommand {

  private final String name;
  private final String description;
  private HashMap<String, DynamicSubcommand> subcommands;

  public DynamicCommand(String name, String description) {
    this.name = name;
    this.description = description;
  }

  public DynamicSubcommand getSubcommand(String subcommandName) {
    return subcommands.get(subcommandName);
  }

  public void putSubcommand(DynamicSubcommand subcommand) {
    subcommands.put(subcommand.getName(), subcommand);
  }

  public void clearSubcommand(String subcommandName) {
    subcommands.remove(subcommandName);
  }
}
