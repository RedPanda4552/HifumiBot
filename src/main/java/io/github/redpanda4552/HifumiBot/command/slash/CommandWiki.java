// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.command.slash;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import io.github.redpanda4552.HifumiBot.event.SelectionInteractionElement;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import io.github.redpanda4552.HifumiBot.util.SimpleSearch;
import io.github.redpanda4552.HifumiBot.wiki.RegionSet;
import io.github.redpanda4552.HifumiBot.wiki.WikiPage;
import java.util.HashMap;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

public class CommandWiki extends AbstractSlashCommand {

  @Override
  protected void onExecute(SlashCommandInteractionEvent event) {
    event.deferReply().queue();
    String query = event.getOption("game").getAsString();
    HashMap<String, Float> results =
        SimpleSearch.search(HifumiBot.getSelf().getWikiIndex().getAllTitles(), query);

    if (results.size() > 0) {
      SelectionInteractionElement selection =
          HifumiBot.getSelf()
              .getSlashCommandListener()
              .newSelection(event.getUser().getId(), defineSlashCommand().getName());
      String highestName = null;
      float highestWeight = 0;

      for (int i = 0; i < 5; i++) {
        if (results.isEmpty()) {
          break;
        }

        for (String name : results.keySet()) {
          if (results.get(name) > highestWeight) {
            highestName = name;
            highestWeight = results.get(name);
          }
        }

        results.remove(highestName);
        selection.addOption(highestName, highestName);
        highestWeight = 0;
      }

      event
          .getHook()
          .editOriginal("Select your game from the list below.")
          .setActionRow(selection.getSelectionMenu())
          .queue();
    } else {
      event.getHook().editOriginal("No results matched your search for `" + query + "`").queue();
    }
  }

  @Override
  public void onSelectionEvent(SelectMenuInteractionEvent event) {
    try {
      List<SelectOption> options = event.getSelectedOptions();

      if (options.size() == 0) {
        event
            .getHook()
            .editOriginal(
                "An error occurred; did not recieve your selection. Try again in a few moments.")
            .queue();
        return;
      }

      String gameName = options.get(0).getValue();
      WikiPage wikiPage = new WikiPage(HifumiBot.getSelf().getWikiIndex().getWikiPageUrl(gameName));
      MessageBuilder mb = new MessageBuilder();
      EmbedBuilder eb = new EmbedBuilder();
      eb.setTitle(wikiPage.getTitle(), wikiPage.getWikiPageUrl());
      eb.setThumbnail(wikiPage.getCoverArtUrl());

      for (RegionSet regionSet : wikiPage.getRegionSets().values()) {
        StringBuilder regionBuilder = new StringBuilder();

        if (!regionSet.getCrc().isEmpty()) {
          regionBuilder.append("\n**CRC:\n**").append(regionSet.getCrc().replace(" ", "\n"));
        }

        if (!regionSet.getWindowsStatus().isEmpty()) {
          regionBuilder
              .append("\n**Windows Compatibility:\n**")
              .append(regionSet.getWindowsStatus());
        }

        if (!regionSet.getLinuxStatus().isEmpty()) {
          regionBuilder.append("\n**Linux Compatibility:\n**").append(regionSet.getLinuxStatus());
        }

        if (regionBuilder.toString().isEmpty())
          regionBuilder.append("No information on this release.");

        eb.addField("__" + regionSet.getRegion() + "__", regionBuilder.toString(), true);
      }

      mb.setEmbeds(eb.build());
      event
          .getHook()
          .editOriginal(mb.build())
          .setActionRow(Button.link(wikiPage.getWikiPageUrl(), "Go to PCSX2 Wiki"))
          .queue();
    } catch (Exception e) {
      event.getHook().editOriginal("An internal error occurred, aborting.").queue();
      Messaging.logException("CommandWiki", "onSelectionEvent", e);
    }
  }

  @Override
  protected CommandData defineSlashCommand() {
    return Commands.slash("wiki", "Search the PCSX2 wiki for a game")
        .addOption(OptionType.STRING, "game", "Title of the game to search for", true)
        .setDefaultPermissions(DefaultMemberPermissions.ENABLED);
  }
}
