// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.command;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public abstract class AbstractMessageContextCommand {

  public void executeIfPermission(MessageContextInteractionEvent event) {
    onExecute(event);
  }

  protected abstract void onExecute(MessageContextInteractionEvent event);

  public void onButtonEvent(ButtonInteractionEvent event) {}

  public void onSelectionEvent(SelectMenuInteractionEvent event) {}

  protected abstract CommandData defineMessageContextCommand();
}
