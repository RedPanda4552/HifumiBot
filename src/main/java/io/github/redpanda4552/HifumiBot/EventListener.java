/**
 * This file is part of HifumiBot, licensed under the MIT License (MIT)
 * 
 * Copyright (c) 2017 Brian Wood
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
package io.github.redpanda4552.HifumiBot;

import java.util.HashMap;
import java.util.List;

import io.github.redpanda4552.HifumiBot.wiki.Emotes;
import io.github.redpanda4552.HifumiBot.wiki.RegionSet;
import io.github.redpanda4552.HifumiBot.wiki.WikiPage;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class EventListener extends ListenerAdapter {
    
    private HifumiBot hifumiBot;
    private HashMap<String, Message> messages = new HashMap<String, Message>();
    
    public EventListener(HifumiBot hifumiBot) {
        this.hifumiBot = hifumiBot;
    }
    
    public void waitForMessage(String userId, Message msg) {
        if (messages.containsKey(userId))
            messages.get(userId).delete().complete();
        
        messages.put(userId, msg);
    }
    
    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        Message msg = messages.get(event.getUser().getId());
        
        if (msg == null)
            return;
        
        if (msg.getId().equals(event.getMessageId())) {
            String gameName = null;
            
            List<Field> fields = msg.getEmbeds().get(0).getFields();
            
            switch (event.getReactionEmote().getName().toLowerCase()) {
            case Emotes.ONE:
                gameName = fields.get(0).getValue();
                break;
            case Emotes.TWO:
                gameName = fields.get(1).getValue();
                break;
            case Emotes.THREE:
                gameName = fields.get(2).getValue();
                break;
            case Emotes.FOUR:
                gameName = fields.get(3).getValue();
                break;
            case Emotes.FIVE:
                gameName = fields.get(4).getValue();
                break;
            case Emotes.SIX:
                gameName = fields.get(5).getValue();
                break;
            }
            
            WikiPage wikiPage = new WikiPage(hifumiBot.getFullGamesMap().get(gameName));
            msg.clearReactions().complete();
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle(wikiPage.getTitle());
            eb.setDescription(wikiPage.getWikiPageUrl());
            eb.setThumbnail(wikiPage.getCoverArtUrl());
            
            for (RegionSet regionSet : wikiPage.getRegionSets().values()) {
                StringBuilder regionBuilder = new StringBuilder();
                
                /*
                if (!regionSet.getSerial().isEmpty()) {
                    regionBuilder.append("**Serial:\n**")
                                 .append(regionSet.getSerial().replace(" ", "\n"));
                }
                */
                
                if (!regionSet.getCRC().isEmpty()) {
                    regionBuilder.append("\n**CRC:\n**")
                                 .append(regionSet.getCRC().replace(" ", "\n"));
                }
                
                if (!regionSet.getWindowsStatus().isEmpty()) {
                    regionBuilder.append("\n**Windows Compatibility:\n**")
                                 .append(regionSet.getWindowsStatus());
                }
                
                if (!regionSet.getLinuxStatus().isEmpty()) {
                    regionBuilder.append("\n**Linux Compatibility:\n**")
                                 .append(regionSet.getLinuxStatus());
                }
                
                if (regionBuilder.toString().isEmpty())
                    regionBuilder.append("No information on this release.");
                
                eb.addField("__" + regionSet.getRegion() + "__", regionBuilder.toString(), true);
            }
            
            StringBuilder issueList = new StringBuilder();
            
            for (String knownIssue : wikiPage.getKnownIssues())
                issueList.append(knownIssue).append("\n");
            
            if (!issueList.toString().isEmpty())
                eb.addField("__Known Issues:__", issueList.toString(), true);
            
            msg.editMessage(eb.build()).complete();
            messages.remove(event.getUser().getId());
        }
    }
}
