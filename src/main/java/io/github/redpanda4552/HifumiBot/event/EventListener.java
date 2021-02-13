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
package io.github.redpanda4552.HifumiBot.event;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

import io.github.redpanda4552.HifumiBot.ChatFilter;
import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.commands.CommandWarez;
import io.github.redpanda4552.HifumiBot.config.ConfigManager;
import io.github.redpanda4552.HifumiBot.util.EmbedUtil;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import io.github.redpanda4552.HifumiBot.wiki.Emotes;
import io.github.redpanda4552.HifumiBot.wiki.RegionSet;
import io.github.redpanda4552.HifumiBot.wiki.WikiPage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EventListener extends ListenerAdapter {
    
//    private static final String BOT_TALK_CHANNEL_ID = "352232087736025090";
    
    private HifumiBot hifumiBot;
    private HashMap<String, Message> messages = new HashMap<String, Message>();
    
    public EventListener(HifumiBot hifumiBot) {
        this.hifumiBot = hifumiBot;
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        ChatFilter.applyFilters(event);
        HifumiBot.getSelf().getCommandInterpreter().execute(event);
    }
    
    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        for (Role role : event.getRoles()) {
            if (role.getId().equals(CommandWarez.WAREZ_ROLE_ID) && !HifumiBot.getSelf().getConfig().warezUsers.containsKey(event.getUser().getId())) {
                HifumiBot.getSelf().getConfig().warezUsers.put(event.getUser().getId(), OffsetDateTime.now());
                ConfigManager.write(HifumiBot.getSelf().getConfig());
                return;
            }
        }
    }
    
    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        for (Role role : event.getRoles()) {
            if (role.getId().equals(CommandWarez.WAREZ_ROLE_ID)) {
                HifumiBot.getSelf().getConfig().warezUsers.remove(event.getUser().getId());
                ConfigManager.write(HifumiBot.getSelf().getConfig());
                return;
            }
        }
    }
    
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (HifumiBot.getSelf().getConfig().warezUsers.containsKey(event.getUser().getId())) {
            // First assign the warez role
            Role role = event.getGuild().getRoleById(CommandWarez.WAREZ_ROLE_ID);
            event.getGuild().addRoleToMember(event.getMember(), role).complete();
            
            // Then send a notification 
            EmbedBuilder eb = EmbedUtil.newFootedEmbedBuilder(HifumiBot.getSelf().getJDA().getSelfUser());
            eb.setTitle("Warez Member Rejoined");
            eb.setDescription("A user who was previously warez'd has rejoined the server.");
            eb.addField("User Name", event.getUser().getName(), true);
            eb.addField("Display Name", event.getMember().getEffectiveName(), true);
            String dateStr = HifumiBot.getSelf().getConfig().warezUsers.get(event.getUser().getId()).format(DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm:ss")) + " UTC";
            eb.addField("Warez Date", dateStr, true);
            Messaging.sendMessage(event.getGuild().getTextChannelById(HifumiBot.getSelf().getConfig().systemOutputChannelId), eb.build());
        }
    }
    
    @Override
    public void onPrivateMessageReactionAdd(PrivateMessageReactionAddEvent event) {
        onMessageReactionAdd(event.getUser().getId(), event.getMessageId(), event.getReactionEmote().getName().toLowerCase());
    }
    
    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        onMessageReactionAdd(event.getUser().getId(), event.getMessageId(), event.getReactionEmote().getName().toLowerCase());
    }
    
    private void onMessageReactionAdd(String userId, String messageId, String reactionEmoteName) {
        Message msg = messages.get(userId);
        
        if (msg == null)
            return;
        
        if (msg.getId().equals(messageId)) {
            String gameName = null;
            
            List<Field> fields = msg.getEmbeds().get(0).getFields();
            
            switch (reactionEmoteName) {
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
            
            finalizeMessage(msg, gameName, userId);
        }
    }
    
    public void waitForMessage(String userId, Message msg) {
        if (messages.containsKey(userId))
            messages.get(userId).delete().complete();
        
        messages.put(userId, msg);
    }
    
    public void finalizeMessage(Message msg, String gameName, String userId) {
        WikiPage wikiPage = new WikiPage(hifumiBot.getWikiIndex().getWikiPageUrl(gameName));
        
        if (msg.getChannel() instanceof TextChannel) {
            msg.clearReactions().complete();
        }
        
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(wikiPage.getTitle(), wikiPage.getWikiPageUrl());
        eb.setThumbnail(wikiPage.getCoverArtUrl());
        
        for (RegionSet regionSet : wikiPage.getRegionSets().values()) {
            StringBuilder regionBuilder = new StringBuilder();
            
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
        
        StringBuilder fixedList = new StringBuilder();
        
        for (String fixedIssue : wikiPage.getFixedIssues())
            fixedList.append(fixedIssue).append("\n");
        
        if (!fixedList.toString().isEmpty())
            eb.addField("__Fixed Issues:__", fixedList.toString(), true);
        
        msg.editMessage(eb.build()).complete();
        messages.remove(userId);
    }
}
