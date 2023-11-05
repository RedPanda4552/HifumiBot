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

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

import io.github.redpanda4552.HifumiBot.EventLogging;
import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.config.ConfigManager;
import io.github.redpanda4552.HifumiBot.filter.FilterRunnable;
import io.github.redpanda4552.HifumiBot.filter.MessageHistoryEntry;
import io.github.redpanda4552.HifumiBot.parse.CrashParser;
import io.github.redpanda4552.HifumiBot.parse.EmulogParser;
import io.github.redpanda4552.HifumiBot.parse.PnachParser;
import io.github.redpanda4552.HifumiBot.permissions.PermissionLevel;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import io.github.redpanda4552.HifumiBot.util.PixivSourceFetcher;
import io.github.redpanda4552.HifumiBot.wiki.RegionSet;
import io.github.redpanda4552.HifumiBot.wiki.WikiPage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EventListener extends ListenerAdapter {

    private HifumiBot hifumiBot;
    private HashMap<String, Message> messages = new HashMap<String, Message>();
    
    public EventListener(HifumiBot hifumiBot) {
        this.hifumiBot = hifumiBot;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getChannelType() == ChannelType.PRIVATE) {
            if (!event.getAuthor().getId().equals(HifumiBot.getSelf().getJDA().getSelfUser().getId())) {
                Messaging.logInfo("EventListener", "onMessageReceived", "DM sent to Hifumi by user " + event.getAuthor().getAsMention() + " (" + event.getAuthor().getName() + ")\n\n```\n" + StringUtils.truncate(event.getMessage().getContentRaw(), 500) + "\n```\nMessage content displayed raw format, truncated to 500 chars. Original length: " + event.getMessage().getContentRaw().length());
                Messaging.sendMessage(event.getChannel(), "I am a bot. If you need something, please ask a human in the server.", event.getMessage(), false);
            }
            
            return;
        }
        
        Instant now = Instant.now();
        
        if (HifumiBot.getSelf().getPermissionManager().hasPermission(PermissionLevel.GUEST, event.getMember())) {
            if (Messaging.hasEmulog(event.getMessage())) {
                EmulogParser ep = new EmulogParser(event.getMessage());
                HifumiBot.getSelf().getScheduler().runOnce(ep);
            }

            if (Messaging.hasPnach(event.getMessage())) {
                PnachParser pp = new PnachParser(event.getMessage());
                HifumiBot.getSelf().getScheduler().runOnce(pp);
            }

            if (Messaging.hasCrashLog(event.getMessage())) {
                CrashParser crashp = new CrashParser(event.getMessage());
                HifumiBot.getSelf().getScheduler().runOnce(crashp);
            }
        }

        if (!HifumiBot.getSelf().getPermissionManager().hasPermission(PermissionLevel.MOD, event.getMember())) {
            HifumiBot.getSelf().getScheduler().runOnce(new FilterRunnable(event.getMessage(), now));
            
            if (Messaging.hasBotPing(event.getMessage())) {
                Messaging.sendMessage(event.getChannel(), "You are pinging a bot.", event.getMessage(), false);
            }
        } else {
            HifumiBot.getSelf().getMessageHistoryManager().store(event.getMessage());
        }

        if (!event.getAuthor().getId().equals(HifumiBot.getSelf().getJDA().getSelfUser().getId())) {
            if (Messaging.hasGhostPing(event.getMessage())) {
                Messaging.sendMessage(event.getChannel(), ":information_source: The user you tried to mention has left the server.", event.getMessage(), false);
            }
        }
        
        if (event.getMember() != null && event.getMember().getRoles().isEmpty()) {
            Instant joinTime = event.getMember().getGuild().retrieveMemberById(event.getAuthor().getId()).complete().getTimeJoined().toInstant();
            
            if (Duration.between(joinTime, now).toSeconds() >= HifumiBot.getSelf().getConfig().roles.autoAssignMemberTimeSeconds) {
                event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(HifumiBot.getSelf().getConfig().roles.autoAssignMemberRoleId)).complete();
            }
        }
        
        PixivSourceFetcher.getPixivLink(event.getMessage());
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        MessageHistoryEntry entry = HifumiBot.getSelf().getMessageHistoryManager().fetchMessage(event.getMessageId());

        if (entry != null) {
            if (!entry.getUserId().equals(HifumiBot.getSelf().getJDA().getSelfUser().getId())) {
                EventLogging.logMessageDeleteEvent(entry);
            }
        } else {
            EventLogging.logMessageDeleteEvent(event.getGuildChannel().getAsMention(), event.getMessageId());
        }
    }

    @Override 
    public void onMessageBulkDelete(MessageBulkDeleteEvent event) {
        for (String messageId : event.getMessageIds()) {
            MessageHistoryEntry entry = HifumiBot.getSelf().getMessageHistoryManager().fetchMessage(messageId);

            if (entry != null) {
                EventLogging.logMessageDeleteEvent(entry);
                HifumiBot.getSelf().getMessageHistoryManager().removeMessage(messageId);
            } else {
                EventLogging.logMessageDeleteEvent(event.getChannel().getAsMention(), messageId);
            }
        }
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        MessageHistoryEntry entry = HifumiBot.getSelf().getMessageHistoryManager().fetchMessage(event.getMessageId());

        if (!entry.getUserId().equals(HifumiBot.getSelf().getJDA().getSelfUser().getId())) {
            EventLogging.logMessageUpdateEvent(event, entry);
            HifumiBot.getSelf().getMessageHistoryManager().store(event.getMessage());
        }
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        for (Role role : event.getRoles()) {
            if (role.getId().equals(HifumiBot.getSelf().getConfig().roles.warezRoleId)
                    && !HifumiBot.getSelf().getWarezTracking().warezUsers.containsKey(event.getUser().getId())) {
                HifumiBot.getSelf().getWarezTracking().warezUsers.put(event.getUser().getId(), OffsetDateTime.now());
                ConfigManager.write(HifumiBot.getSelf().getWarezTracking());
                return;
            }
        }
    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        for (Role role : event.getRoles()) {
            if (role.getId().equals(HifumiBot.getSelf().getConfig().roles.warezRoleId)) {
                HifumiBot.getSelf().getWarezTracking().warezUsers.remove(event.getUser().getId());
                ConfigManager.write(HifumiBot.getSelf().getWarezTracking());
                return;
            }
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

        Messaging.editMessageEmbed(msg, eb.build());
        messages.remove(userId);
    }
}
