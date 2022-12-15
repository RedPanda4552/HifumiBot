// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.command;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public abstract class AbstractSlashCommand {

  public AbstractSlashCommand() {}

  public void executeIfPermission(SlashCommandInteractionEvent event) {
    if (HifumiBot.getSelf()
        .getCommandIndex()
        .isNinja(event.getName(), event.getChannel().getId())) {
      event.reply("ninja").setEphemeral(true).queue();
    } else {
      onExecute(event);
    }
  }

  protected abstract void onExecute(SlashCommandInteractionEvent event);

  public void onButtonEvent(ButtonInteractionEvent event) {}

  public void onSelectionEvent(SelectMenuInteractionEvent event) {}

  protected abstract CommandData defineSlashCommand();
}
