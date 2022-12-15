// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.command.slash;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class CommandGameDB extends AbstractSlashCommand {

  private static final Pattern GAMEDB_SERIAL_PATTERN = Pattern.compile("^[A-Z]{4}-[0-9]{5}$");

  @Override
  protected void onExecute(SlashCommandInteractionEvent event) {
    OptionMapping opt = event.getOption("serial");

    if (opt == null) {
      Messaging.logInfo(
          "CommandGameDB",
          "onExecute",
          "Command tampering? Missing option 'serial' (user = "
              + event.getUser().getAsMention()
              + ")");
      event.reply("Invalid option detected, admins have been alerted.").setEphemeral(true).queue();
      return;
    }

    String normalized = opt.getAsString().toUpperCase();
    Matcher m = GAMEDB_SERIAL_PATTERN.matcher(normalized);

    if (!m.matches()) {
      event
          .reply("Invalid serial detected; serial numbers follow this format: `SLUS-12345`")
          .setEphemeral(true)
          .queue();
      return;
    }

    event.deferReply().queue();
    event
        .getHook()
        .editOriginal(
            ":information_source: Checking GameDB for serial `"
                + normalized
                + "`, this might take a moment...")
        .queue();
    MessageEmbed embed = HifumiBot.getSelf().getGameDB().present(normalized);
    event.getHook().editOriginalEmbeds(embed).queue();
  }

  @Override
  protected CommandData defineSlashCommand() {
    return Commands.slash(
            "gamedb", "Look up information stored in GameIndex.yaml (otherwise known as 'GameDB')")
        .addOption(
            OptionType.STRING, "serial", "Serial number to search for (e.g. 'SLUS-12345')", true)
        .setDefaultPermissions(DefaultMemberPermissions.ENABLED);
  }
}
