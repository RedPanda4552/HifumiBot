// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.command.dynamic;

import java.util.HashMap;
import lombok.Getter;

@Getter
public class DynamicSubcommand {

  private final String name;
  private final String description;
  private final HashMap<String, DynamicChoice> choices;

  public DynamicSubcommand(String name, String description) {
    this.name = name;
    this.description = description;
    this.choices = new HashMap<String, DynamicChoice>();
  }

  public DynamicChoice getChoice(String choiceName) {
    return choices.get(choiceName);
  }

  public void putChoice(DynamicChoice choice) {
    choices.put(choice.getName(), choice);
  }

  public void clearChoice(String choiceName) {
    choices.remove(choiceName);
  }
}
