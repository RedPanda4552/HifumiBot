// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.command.slash;

import io.github.redpanda4552.HifumiBot.GpuIndex;
import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import io.github.redpanda4552.HifumiBot.permissions.PermissionLevel;
import io.github.redpanda4552.HifumiBot.util.SimpleSearch;
import java.util.HashMap;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.apache.commons.lang3.StringUtils;

public class CommandGPU extends AbstractSlashCommand {

  @Getter
  private enum GPURating {
    x8NATIVE("8x Native (~5K)", 13030),
    x6NATIVE("6x Native (~4K)", 8660),
    x5NATIVE("5x Native (~3K)", 6700),
    x4NATIVE("4x Native (~2K)", 4890),
    x3NATIVE("3x Native (~1080p)", 3230),
    x2NATIVE("2x Native (~720p)", 1720),
    NATIVE("Native", 360),
    SLOW("Slow", 0);

    private final String displayName;
    private final int minimum;

    GPURating(String displayName, int minimum) {
      this.displayName = displayName;
      this.minimum = minimum;
    }
  }

  @Override
  protected void onExecute(SlashCommandInteractionEvent event) {
    boolean isEphemeral =
        !event
                .getChannel()
                .getId()
                .equals(HifumiBot.getSelf().getConfig().channels.restrictedCommandChannelId)
            && !HifumiBot.getSelf()
                .getPermissionManager()
                .hasPermission(PermissionLevel.MOD, event.getMember());

    EmbedBuilder eb = new EmbedBuilder();
    OptionMapping opt = event.getOption("name");

    if (opt == null) {
      event.reply("Missing required argument `name`").setEphemeral(isEphemeral).queue();
      return;
    }

    String name = opt.getAsString();
    GpuIndex gpuIndex = HifumiBot.getSelf().getGpuIndex();
    HashMap<String, Float> results =
        SimpleSearch.search(gpuIndex.getAllGpus(), StringUtils.join(name, " "));

    if (results.size() > 0) {
      eb.setAuthor("Passmark GPU Performance", "https://www.videocardbenchmark.net/");
      eb.setTitle("Search results for '" + StringUtils.join(name, " ").trim() + "'");
      eb.setDescription(
          ":warning: Some games may have unusually high GPU requirements! If in doubt, ask!");
      String highestName = null;
      float highestWeight = 0;

      while (!results.isEmpty() && eb.getFields().size() < 5) {
        for (String gpuName : results.keySet()) {
          if (results.get(gpuName) > highestWeight) {
            highestName = gpuName;
            highestWeight = results.get(gpuName);
          }
        }

        results.remove(highestName);
        highestWeight = 0;
        int highestScore = -1;

        try {
          highestScore =
              Integer.parseInt(gpuIndex.getGpuRating(highestName).replaceAll("[,. ]", ""));
        } catch (NumberFormatException ignored) {
        }

        String highestScoreDescription = "";

        for (int i = 0; i < GPURating.values().length; i++) {
          if (highestScore >= GPURating.values()[i].getMinimum()) {
            highestScoreDescription = GPURating.values()[i].getDisplayName();
            break;
          }
        }

        eb.addField(highestName, highestScore + " - " + highestScoreDescription, false);
      }

      eb.setColor(0x00ff00);
    } else {
      eb.setTitle("No results matched your query!");
      eb.setColor(0xff0000);
    }

    event.replyEmbeds(eb.build()).queue();
  }

  @Override
  protected CommandData defineSlashCommand() {
    return Commands.slash("gpu", "Look up the rating of a GPU")
        .addOption(OptionType.STRING, "name", "Name of the GPU to look up", true)
        .setDefaultPermissions(DefaultMemberPermissions.ENABLED);
  }
}
