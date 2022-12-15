// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.command.slash;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandRun extends AbstractSlashCommand {

  @Override
  protected void onExecute(SlashCommandInteractionEvent event) {
    String runnableName = event.getOption("runnable").getAsString();
    boolean result = HifumiBot.getSelf().getScheduler().runScheduledNow(runnableName);

    if (result) {
      event
          .reply(
              "Sent an execute request for runnable '"
                  + runnableName
                  + "' to the thread pool; it will run whenever a thread is available to host it.")
          .queue();
    } else {
      event
          .reply(
              "Something went wrong while trying to add the runnable to the thread pool; try again"
                  + " in a few moments.")
          .setEphemeral(true)
          .queue();
    }
  }

  @Override
  protected CommandData defineSlashCommand() {
    OptionData runnable =
        new OptionData(OptionType.STRING, "runnable", "Name of the runnable to execute")
            .setRequired(true);

    for (String runnableName : HifumiBot.getSelf().getScheduler().getRunnableNames()) {
      runnable.addChoice(runnableName, runnableName);
    }

    return Commands.slash("run", "Execute a scheduled runnable immediately")
        .addOptions(runnable)
        .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
  }
}
