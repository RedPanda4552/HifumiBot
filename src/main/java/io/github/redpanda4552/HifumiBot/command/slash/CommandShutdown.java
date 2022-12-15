// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.command.slash;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class CommandShutdown extends AbstractSlashCommand {

  @Override
  protected void onExecute(SlashCommandInteractionEvent event) {
    HifumiBot.getSelf()
        .getScheduler()
        .runOnce(
            () -> {
              if (HifumiBot.getSelf() != null) HifumiBot.getSelf().shutdown(false);
            });
    event.reply("Shutting down, bye bye!").queue();
  }

  @Override
  protected CommandData defineSlashCommand() {
    return Commands.slash("shutdown", "Shuts down the bot with no attempt to reload")
        .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
  }
}
