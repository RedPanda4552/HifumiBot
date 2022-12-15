// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.command.slash;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class CommandReload extends AbstractSlashCommand {

  @Override
  protected void onExecute(SlashCommandInteractionEvent event) {
    HifumiBot.getSelf()
        .getScheduler()
        .runOnce(
            () -> {
              if (HifumiBot.getSelf() != null) HifumiBot.getSelf().shutdown(true);
            });
    event.reply("Reloading, be right back!").queue();
  }

  @Override
  protected CommandData defineSlashCommand() {
    return Commands.slash("reload", "Shuts down the bot and immediately loads a new instance")
        .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
  }
}
