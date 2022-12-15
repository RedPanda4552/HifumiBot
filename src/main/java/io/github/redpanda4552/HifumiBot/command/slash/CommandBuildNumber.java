// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.command.slash;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import java.util.Optional;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.apache.commons.lang3.StringUtils;

public class CommandBuildNumber extends AbstractSlashCommand {

  @Override
  protected void onExecute(SlashCommandInteractionEvent event) {
    OptionMapping option = event.getOption("buildid");
    if (option == null) {
      event.reply("Build ID not provided or invalid!").setEphemeral(true).queue();
      return;
    }
    long buildId = option.getAsLong();
    Optional<String> commitSha = HifumiBot.getSelf().getBuildCommitMap().getCommitSha(buildId);
    commitSha.ifPresentOrElse(
        (sha) -> {
          EmbedBuilder eb = new EmbedBuilder();
          eb.setTitle("Build Id to Commit");
          eb.addField("Build Id", String.valueOf(buildId), true);
          eb.addField(
              "Commit",
              String.format(
                  "[%s](https://github.com/PCSX2/pcsx2/commit/%s)",
                  StringUtils.abbreviate(sha, 10), sha),
              true);
          event.replyEmbeds(eb.build()).setEphemeral(true).queue();
        },
        () -> {
          event
              .reply("Could not find that build id in the current development cycle (v1.7)!")
              .setEphemeral(true)
              .queue();
        });
  }

  @Override
  protected CommandData defineSlashCommand() {
    return Commands.slash(
            "commitfrombuild", "Determine the git commit a build id is associated with")
        .addOption(OptionType.INTEGER, "buildid", "The build id", true)
        .setDefaultPermissions(DefaultMemberPermissions.ENABLED);
  }
}
