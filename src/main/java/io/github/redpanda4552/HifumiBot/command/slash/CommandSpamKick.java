// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.command.slash;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class CommandSpamKick extends AbstractSlashCommand {

  @Override
  protected void onExecute(SlashCommandInteractionEvent event) {
    OptionMapping opt = event.getOption("user");

    if (opt == null) {
      event.reply("Required option `user` missing").setEphemeral(true);
      return;
    }

    Member member = opt.getAsMember();

    try {
      HifumiBot.getSelf().getKickHandler().doKick(member);
      event
          .reply("Successfully messaged and kicked " + member.getUser().getAsMention())
          .setEphemeral(true)
          .queue();
    } catch (Exception e) {
      Messaging.logException("CommandSpamKick", "onExecute", e);
      event
          .reply("An internal error occurred, check the bot logging channel")
          .setEphemeral(true)
          .queue();
    }
  }

  @Override
  protected CommandData defineSlashCommand() {
    return Commands.slash(
            "spamkick",
            "Send a user a DM telling them their account is compromised and spamming, then kick the"
                + " user")
        .addOption(OptionType.USER, "user", "User to DM and kick", true)
        .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
  }
}
