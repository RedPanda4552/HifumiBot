// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.command.slash;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.Scheduler.NoSuchRunnableException;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import io.github.redpanda4552.HifumiBot.config.ConfigManager;
import io.github.redpanda4552.HifumiBot.config.ConfigType;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class CommandAbout extends AbstractSlashCommand {

  @Override
  protected void onExecute(SlashCommandInteractionEvent event) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("About " + HifumiBot.getSelf().getJDA().getSelfUser().getName());
    eb.setDescription("A helper bot created for the PCSX2 Discord server.");
    eb.addField("Created By", "pandubz", true);
    String version = HifumiBot.getSelf().getVersion();
    eb.addField("Version", version != null ? version : "[Debug Mode]", true);

    StringBuilder storageBuilder = new StringBuilder("| ");
    storageBuilder
        .append("Config: ")
        .append((ConfigManager.getSizeBytes(ConfigType.CORE) / 1024) + " KB | ");
    storageBuilder
        .append("Warez: ")
        .append((ConfigManager.getSizeBytes(ConfigType.WAREZ) / 1024) + " KB | ");
    storageBuilder
        .append("DynCmd: ")
        .append((ConfigManager.getSizeBytes(ConfigType.DYNCMD) / 1024) + " KB | ");
    storageBuilder
        .append("BuildMap: ")
        .append((ConfigManager.getSizeBytes(ConfigType.BUILDMAP) / 1024) + " KB | ");
    storageBuilder
        .append("Emulog: ")
        .append((ConfigManager.getSizeBytes(ConfigType.EMULOG_PARSER) / 1024) + " KB |");
    eb.addField("Storage Size", storageBuilder.toString(), false);
    StringBuilder runnableBuilder = new StringBuilder("| ");

    for (String runnableName : HifumiBot.getSelf().getScheduler().getRunnableNames()) {
      try {
        runnableBuilder.append(
            runnableName
                + ": "
                + (HifumiBot.getSelf().getScheduler().isRunnableAlive(runnableName)
                    ? "alive"
                    : "stopped")
                + " | ");
      } catch (NoSuchRunnableException e) {
        Messaging.logException("CommandAbout", "onExecute", e);
      }
    }

    eb.addField("Runnable Statuses", runnableBuilder.toString().trim(), false);
    event.replyEmbeds(eb.build()).queue();
  }

  @Override
  protected CommandData defineSlashCommand() {
    return Commands.slash("about", "View general information about the bot and its health")
        .setDefaultPermissions(DefaultMemberPermissions.ENABLED);
  }
}
