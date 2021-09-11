/**
 * This file is part of HifumiBot, licensed under the MIT License (MIT)
 * 
 * Copyright (c) 2020 RedPanda4552 (https://github.com/RedPanda4552)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.redpanda4552.HifumiBot.command.slash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.CRC32;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import io.github.redpanda4552.HifumiBot.permissions.PermissionLevel;
import io.github.redpanda4552.HifumiBot.util.SimpleSearch;
import io.github.redpanda4552.HifumiBot.wiki.RegionSet;
import io.github.redpanda4552.HifumiBot.wiki.WikiPage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.Button;

public class CommandWiki extends AbstractSlashCommand {

    public CommandWiki() {
        super(PermissionLevel.GUEST);
    }

    @Override
    protected void onExecute(SlashCommandEvent event) {
        int i = 0;

        HashMap<String, Float> results = SimpleSearch.search(HifumiBot.getSelf().getWikiIndex().getAllTitles(), event.getOption("game").getAsString());

        if (results.size() > 0) {
            ArrayList<Button> buttons = new ArrayList<Button>();
            String highestName = null;
            float highestWeight = 0;

            while (!results.isEmpty() && i < 5) {
                for (String name : results.keySet()) {
                    if (results.get(name) > highestWeight) {
                        highestName = name;
                        highestWeight = results.get(name);
                    }
                }

                results.remove(highestName);
                CRC32 crc = new CRC32();
                crc.update(highestName.getBytes());
                buttons.add(i++ % 2 == 0 ? 
                        Button.primary(event.getUser().getId() + ":" + defineSlashCommand().getName() + ":" + crc.getValue(), highestName) : 
                        Button.secondary(event.getUser().getId() + ":" + defineSlashCommand().getName() + ":" + crc.getValue(), highestName));
                highestWeight = 0;
            }
            
            event.reply("Choose your game below").addActionRow(buttons).queue();
        } else {
            event.reply("No results matched your query!").queue();
        }
    }
    
    @Override
    public void onButtonEvent(ButtonClickEvent event, String payload) {
        try {
            String gameName = payload;
            WikiPage wikiPage = new WikiPage(HifumiBot.getSelf().getWikiIndex().getWikiPageUrl(gameName));
            MessageBuilder mb = new MessageBuilder();
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle(wikiPage.getTitle(), wikiPage.getWikiPageUrl());
            eb.setThumbnail(wikiPage.getCoverArtUrl());

            for (RegionSet regionSet : wikiPage.getRegionSets().values()) {
                StringBuilder regionBuilder = new StringBuilder();

                if (!regionSet.getCRC().isEmpty()) {
                    regionBuilder.append("\n**CRC:\n**").append(regionSet.getCRC().replace(" ", "\n"));
                }

                if (!regionSet.getWindowsStatus().isEmpty()) {
                    regionBuilder.append("\n**Windows Compatibility:\n**").append(regionSet.getWindowsStatus());
                }

                if (!regionSet.getLinuxStatus().isEmpty()) {
                    regionBuilder.append("\n**Linux Compatibility:\n**").append(regionSet.getLinuxStatus());
                }

                if (regionBuilder.toString().isEmpty())
                    regionBuilder.append("No information on this release.");

                eb.addField("__" + regionSet.getRegion() + "__", regionBuilder.toString(), true);
            }
            
            mb.setEmbeds(eb.build());
            event.getHook().editOriginal(mb.build()).setActionRow(Button.link(wikiPage.getWikiPageUrl(), "Go to PCSX2 Wiki")).queue();
        } catch (IllegalArgumentException e) {
            event.getHook().sendMessage("Detected a damaged event payload, aborting.").setEphemeral(true).queue();
        }
    }

    @Override
    protected CommandData defineSlashCommand() {
        return new CommandData("wiki", "Search the PCSX2 wiki for a game")
                .addOption(OptionType.STRING, "game", "Title of the game to search for", true);
    }
}
