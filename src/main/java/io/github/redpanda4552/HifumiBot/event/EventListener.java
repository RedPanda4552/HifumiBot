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
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.config.ConfigManager;
import io.github.redpanda4552.HifumiBot.filter.HyperlinkCleaner;
import io.github.redpanda4552.HifumiBot.parse.EmulogParser;
import io.github.redpanda4552.HifumiBot.parse.PnachParser;
import io.github.redpanda4552.HifumiBot.permissions.PermissionLevel;
import io.github.redpanda4552.HifumiBot.util.EmbedUtil;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import io.github.redpanda4552.HifumiBot.util.PixivSourceFetcher;
import io.github.redpanda4552.HifumiBot.wiki.Emotes;
import io.github.redpanda4552.HifumiBot.wiki.RegionSet;
import io.github.redpanda4552.HifumiBot.wiki.WikiPage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
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

    private HifumiBot hifumiBot;
    private HashMap<String, Message> messages = new HashMap<String, Message>();
    private ConcurrentHashMap<String, OffsetDateTime> joinEvents = new ConcurrentHashMap<String, OffsetDateTime>();

    public EventListener(HifumiBot hifumiBot) {
        this.hifumiBot = hifumiBot;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getChannelType() == ChannelType.PRIVATE) {
            Messaging.logInfo("EventListener", "onMessageReceived", "DM sent to Hifumi by user " + event.getAuthor().getAsMention() + " (" + event.getAuthor().getAsTag() + ")\n\n```\n" + StringUtils.truncate(event.getMessage().getContentRaw(), 500) + "\n```\nMessage content displayed raw format, truncated to 500 chars. Original length: " + event.getMessage().getContentRaw().length());
            return;
        }
        
        Instant now = Instant.now();
        
        if (HifumiBot.getSelf().getChatFilter().applyFilters(event)) {
            HifumiBot.getSelf().getKickHandler().storeIncident(event.getMember(), now);
            return;
        }

        if (HifumiBot.getSelf().getPermissionManager().hasPermission(PermissionLevel.GUEST, event.getMember())) {
            HifumiBot.getSelf().getCommandInterpreter().execute(event);

            if (Messaging.messageHasEmulog(event.getMessage())) {
                EmulogParser ep = new EmulogParser(event.getMessage());
                HifumiBot.getSelf().getScheduler().runOnce(ep);
            }

            if (Messaging.messageHasPnach(event.getMessage())) {
                PnachParser pp = new PnachParser(event.getMessage());
                HifumiBot.getSelf().getScheduler().runOnce(pp);
            }
        }

        if (!HifumiBot.getSelf().getPermissionManager().hasPermission(PermissionLevel.MOD, event.getMember())) {
            HifumiBot.getSelf().getScheduler().runOnce(new HyperlinkCleaner(event.getMessage(), now));
        }
        
        PixivSourceFetcher.getPixivLink(event.getMessage());
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
    
    private boolean kickNewUsers = false;
    private OffsetDateTime cooldown;

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (HifumiBot.getSelf().getConfig().enableBotKicker) {
            if (cooldown != null && OffsetDateTime.now().isAfter(cooldown)) {
                kickNewUsers = false;
                cooldown = null;
                Messaging.logInfo("EventListener", "onGuildMemberJoin", "Cooldown has been released, new users are now able to join the server again.");
            }
            
            if (kickNewUsers) {
                Member member = event.getGuild().getMemberById(event.getUser().getId());
                HifumiBot.getSelf().getKickHandler().doKickForBotJoin(member);
            }
            
            joinEvents.put(event.getUser().getId(), OffsetDateTime.now());
            
            while (joinEvents.size() > 3) {
                String toRemove = null;
                
                for (String userId : joinEvents.keySet()) {
                    if (toRemove == null || joinEvents.get(userId).isBefore(joinEvents.get(toRemove))) {
                        toRemove = userId;
                    }
                }
                
                if (toRemove != null) {
                    joinEvents.remove(toRemove);
                }
            }
            
            if (joinEvents.size() == 3) {
                OffsetDateTime minTime = null;
                OffsetDateTime maxTime = null;
                
                for (String userId : joinEvents.keySet()) {
                    if (minTime == null || joinEvents.get(userId).isBefore(minTime)) {
                        minTime = joinEvents.get(userId);
                    }
                    
                    if (maxTime == null || joinEvents.get(userId).isAfter(maxTime)) {
                        maxTime = joinEvents.get(userId);
                    }
                }
                
                if (Math.abs(Duration.between(minTime, maxTime).toSeconds()) < 1) {
                    cooldown = OffsetDateTime.now().plusSeconds(60 * 5);
                    kickNewUsers = true;
                    Messaging.sendMessage(HifumiBot.getSelf().getJDA().getTextChannelById(HifumiBot.getSelf().getConfig().channels.systemOutputChannelId), "@here");
                    Messaging.logInfo("EventListener", "onGuildMemberJoin", "Cooldown has been tripped by three users joining in under one second; new users will be automatically messaged and kicked for the next five minutes.");
                    
                    for (String userId : joinEvents.keySet()) {
                        Member member = event.getGuild().getMemberById(userId);
                        HifumiBot.getSelf().getKickHandler().doKickForBotJoin(member);
                    }
                }
            }
        }

        if (HifumiBot.getSelf().getWarezTracking().warezUsers.containsKey(event.getUser().getId())) {
            // First assign the warez role
            Role role = event.getGuild().getRoleById(HifumiBot.getSelf().getConfig().roles.warezRoleId);
            event.getGuild().addRoleToMember(event.getMember(), role).complete();

            // Then send a notification
            EmbedBuilder eb = EmbedUtil.newFootedEmbedBuilder(HifumiBot.getSelf().getJDA().getSelfUser());
            eb.setTitle("Warez Member Rejoined");
            eb.setDescription("A user who was previously warez'd has rejoined the server.");
            eb.addField("User Name", event.getUser().getName(), true);
            eb.addField("Display Name", event.getMember().getEffectiveName(), true);
            String dateStr = HifumiBot.getSelf().getWarezTracking().warezUsers.get(event.getUser().getId())
                    .format(DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm:ss")) + " UTC";
            eb.addField("Warez Date", dateStr, true);
            Messaging.sendMessageEmbed(
                    event.getGuild().getTextChannelById(HifumiBot.getSelf().getConfig().channels.systemOutputChannelId),
                    eb.build());
        }
    }

    @Override
    public void onPrivateMessageReactionAdd(PrivateMessageReactionAddEvent event) {
        onMessageReactionAdd(event.getUser().getId(), event.getMessageId(),
                event.getReactionEmote().getName().toLowerCase());
    }

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        onMessageReactionAdd(event.getUser().getId(), event.getMessageId(),
                event.getReactionEmote().getName().toLowerCase());
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
