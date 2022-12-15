// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.command.slash;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class CommandPanic extends AbstractSlashCommand {

  @Override
  protected void onExecute(SlashCommandInteractionEvent event) {
    OptionMapping enableOpt = event.getOption("enable");

    if (enableOpt == null) {
      event.reply("Missing required argument 'enable'").setEphemeral(true).queue();
      return;
    }

    event.deferReply().queue();

    if (enableOpt.getAsBoolean()) {
      enable(event);
    } else {
      disable(event);
    }
  }

  @Override
  protected CommandData defineSlashCommand() {
    return Commands.slash("panic", "Panic mode to restrict messaging and server joins")
        .addOption(OptionType.BOOLEAN, "enable", "Enable or disable panic mode", true)
        .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
  }

  private void enable(SlashCommandInteractionEvent event) {
    if (HifumiBot.getSelf().getEventListener().isLockdown()) {
      event.getHook().editOriginal("Panic mode is already enabled").queue();
      return;
    }

    event
        .getGuild()
        .getTextChannels()
        .forEach(
            (channel) -> {
              channel.getManager().setSlowmode(1).queue();
            });

    HifumiBot.getSelf().getEventListener().setLockdown(true);
    event
        .getHook()
        .editOriginal(
            "Panic mode activated.\n"
                + "- 1 second slow mode is applied to all channels (including restricted"
                + " channels)\n"
                + "- New users are being instantly kicked but will receive a PM explaining why\n"
                + "- Any users without roles are having messages automatically deleted.")
        .queue();
  }

  private void disable(SlashCommandInteractionEvent event) {
    event
        .getGuild()
        .getTextChannels()
        .forEach(
            (channel) -> {
              channel.getManager().setSlowmode(0).queue();
            });

    HifumiBot.getSelf().getEventListener().setLockdown(false);
    event
        .getHook()
        .editOriginal("Panic mode deactivated. All previous changes have been reverted.")
        .queue();
  }
}
