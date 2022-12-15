// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.event;

import lombok.Getter;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;

public class SelectionInteractionElement extends AbstractInteractionElement {

  private final SelectMenu.Builder selectionMenu;

  public SelectionInteractionElement(String userId, String commandName) {
    super(userId, commandName);
    this.selectionMenu = SelectMenu.create(uuid.toString());
  }

  public void addOption(String label, String value) {
    this.selectionMenu.addOption(label, value);
  }

  public SelectMenu getSelectionMenu() {
    return selectionMenu.build();
  }
}
