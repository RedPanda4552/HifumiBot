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
package io.github.redpanda4552.HifumiBot.filter;

import java.awt.Color;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.database.Database;
import io.github.redpanda4552.HifumiBot.database.FilterEventObject;
import io.github.redpanda4552.HifumiBot.database.MessageObject;
import io.github.redpanda4552.HifumiBot.util.DNSQueryResult;
import io.github.redpanda4552.HifumiBot.util.Internet;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

public class FilterHandler {
    
    private static final Pattern URL_PATTERN = Pattern.compile("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
    private static final String BOT_FOOTER = "Don't know why you are receiving this message? Please check that your Discord account is secure, someone might be using your account as a spam bot.";

    private ConcurrentHashMap<String, Pattern> patternMap = new ConcurrentHashMap<String, Pattern>();

    public FilterHandler() {
        this.compile();
    }

    public void compile() {
        patternMap.clear();

        for (FilterObject filter : HifumiBot.getSelf().getConfig().filters.values()) {
            for (String regex : filter.regexes.values()) {
                patternMap.put(regex, Pattern.compile(regex));
            }
        }
    }

    /**
     * Applies any filters specified in the filters property of the config. <br/>
     * <i>Note: All messages are flushed to lower case; regular expressions should
     * be written for lower case, or be case-agnostic.</i>
     * 
     * @param event - The MessageReceivedEvent to filter.
     * @return True if a regular expression matched and the message was filtered
     *         out, false otherwise.
     */
    public boolean applyFilters(Message message) {
        for (FilterObject filter : HifumiBot.getSelf().getConfig().filters.values()) {
            for (String regexName : filter.regexes.keySet()) {
                String regexValue = filter.regexes.get(regexName);
                String filteredMessage = message.getContentDisplay().toLowerCase().replaceAll("[\n\r\t]", " ");
                Pattern p = patternMap.get(regexValue);
                Matcher m = p.matcher(filteredMessage);
                boolean matches = m.matches();
                boolean find = m.find();

                if (matches || find) {
                    Database.insertFilterEvent(new FilterEventObject(
                        message.getAuthor().getIdLong(), 
                        message.getIdLong(), 
                        (message.getTimeEdited() != null ? message.getTimeEdited() : message.getTimeCreated()).toEpochSecond(), 
                        filter.name, 
                        regexName, 
                        filter.informational
                    ));

                    if (!filter.informational) {
                        message.delete().complete();

                        // Before sending a private message, attempt to retrieve a member object for the user.
                        // If they are still in the server this will succeed; if they have been kicked already
                        // (and the bot is still trying to catch up on processing messages), this will fail with
                        // an exception and silently skip the private message.
                        try {
                            message.getGuild().retrieveMemberById(message.getAuthor().getIdLong()).complete();

                            EmbedBuilder eb = new EmbedBuilder();
                            eb.setTitle("Message Deleted in PCSX2 Server");
                            eb.setDescription(HifumiBot.getSelf().getConfig().filterOptions.filterMessage);
                            eb.setFooter(BOT_FOOTER);
                            eb.setColor(Color.YELLOW);
                            Messaging.sendPrivateMessageEmbed(message.getAuthor(), eb.build());
                        } catch (Exception e) {
                            // Squelch
                        }
                    }

                    if (!filter.replyMessage.isBlank()) {
                        Messaging.sendMessage(message.getChannel(), filter.replyMessage);
                    }
                    
                    User usr = message.getAuthor();

                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle("Filter Event");
                    eb.setDescription(StringUtils.truncate(message.getContentRaw().replaceAll("\s", " "), 4000));
                    eb.addField("Regular Expression", "`" + p.pattern() + "`", true);
                    eb.addField("Filter Name", filter.name, true);
                    eb.addField("Channel", message.getChannel().getAsMention(), true);
                    eb.addField("Informational (No Delete)", String.valueOf(filter.informational), true);
                    eb.addField("User (As Mention)", usr.getAsMention(), true);
                    eb.addField("Username", usr.getName(), true);
                    eb.addField("User ID", usr.getId(), true);
                    eb.setColor(Color.ORANGE);

                    Messaging.logInfoEmbed(eb.build());
                    return true;
                }
            }
        }

        return false;
    }

    public boolean applyDNSFilter(Message message) {
        Matcher m = URL_PATTERN.matcher(message.getContentDisplay().toLowerCase());

        while (m.find()) {
            String url = m.group();

            if (Internet.nslookup(url) == DNSQueryResult.BLOCKED) {
                Database.insertFilterEvent(new FilterEventObject(
                    message.getAuthor().getIdLong(), 
                    message.getIdLong(), 
                    (message.getTimeEdited() != null ? message.getTimeEdited() : message.getTimeCreated()).toEpochSecond(), 
                    "DNS Block", 
                    StringUtils.abbreviate(url, 255), 
                    false
                ));

                message.delete().complete();
                
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Message Deleted in PCSX2 Server");
                eb.setDescription(HifumiBot.getSelf().getConfig().filterOptions.dnsMessage);
                eb.setFooter(BOT_FOOTER);
                eb.setColor(Color.YELLOW);
                Messaging.sendPrivateMessageEmbed(message.getAuthor(), eb.build());

                User usr = message.getAuthor();
                        
                eb = new EmbedBuilder();
                eb.setTitle("DNS Block Event");
                eb.setDescription(StringUtils.truncate(message.getContentRaw().replaceAll("\s", " "), 4000));
                eb.addField("Flagged URL", url, true);
                eb.addField("Channel", message.getChannel().getAsMention(), true);
                eb.addField("User (As Mention)", usr.getAsMention(), true);
                eb.addField("Username", usr.getName(), true);
                eb.addField("User ID", usr.getId(), true);
                eb.setColor(Color.MAGENTA);
                
                Messaging.logInfoEmbed(eb.build());
                return true;
            }
        }

        return false;
    }

    public boolean reviewFilterEvents(long userIdLong, long timestamp) {
        ArrayList<FilterEventObject> filterEvents = Database.getFilterEventsSinceTime(userIdLong, timestamp);

        if (filterEvents == null || filterEvents.isEmpty()) {
            return false;
        }

        if (filterEvents.size() >= HifumiBot.getSelf().getConfig().filterOptions.maxIncidents) {
            return true;
        }

        return false;
    }

    public boolean reviewSpam(Message message, long timestamp) {
        String rawContent = message.getContentRaw();

        if (rawContent == null || rawContent.isEmpty()) {
            return false;
        }

        ArrayList<MessageObject> duplicates = Database.getIdenticalMessagesSinceTime(message.getContentRaw(), timestamp);

        if (duplicates == null || duplicates.isEmpty()) {
            return false;
        }

        if (duplicates.size() >= HifumiBot.getSelf().getConfig().filterOptions.maxIncidents) {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Spam Warning");
            eb.setDescription(HifumiBot.getSelf().getConfig().filterOptions.spamMessage);
            eb.setFooter(BOT_FOOTER);
            eb.setColor(Color.YELLOW);

            Messaging.sendPrivateMessageEmbed(message.getAuthor(), eb.build());
            return true;
        }

        return false;
    }

    public synchronized void deleteMessages(Message message, long timestamp) {
        ArrayList<MessageObject> duplicates = Database.getIdenticalMessagesSinceTime(message.getContentRaw(), timestamp);

        for (MessageObject duplicate : duplicates) {
            try {
                HifumiBot.getSelf().getJDA().getTextChannelById(duplicate.getChannelId()).deleteMessageById(duplicate.getMessageId()).queue();
            } catch (Exception e) {
                // Squelch
            }
        }
    }

    public synchronized boolean timeoutUser(Guild server, long userIdLong) {
        try {
            Member member = server.retrieveMemberById(userIdLong).complete();

            if (member != null) {
                if (!member.isTimedOut()) {
                    member.timeoutFor(Duration.ofMinutes(HifumiBot.getSelf().getConfig().filterOptions.timeoutDurationMinutes)).complete();

                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle("Timed Out in PCSX2 Server");
                    eb.setDescription(HifumiBot.getSelf().getConfig().filterOptions.timeoutMessage);
                    eb.setFooter(BOT_FOOTER);
                    eb.setColor(Color.ORANGE);
                    Messaging.sendPrivateMessageEmbed(member.getUser(), eb.build());
                    return true;
                }
            }
        } catch (Exception e) {
            Messaging.logException("FilterHandler", "timeoutUser", e);
        }
        
        return false;
    }

    public synchronized boolean kickUser(Guild server, long userIdLong) {
        try {
            Member member = server.retrieveMemberById(userIdLong).complete();

            if (member != null) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Kicked From PCSX2 Server");
                eb.setDescription(HifumiBot.getSelf().getConfig().filterOptions.kickMessage);
                eb.setFooter(BOT_FOOTER);
                eb.setColor(Color.RED);
                Messaging.sendPrivateMessageEmbed(member.getUser(), eb.build());
                member.kick().complete();
                return true;
            }
        } catch (Exception e) {
            // Squelch
        }
        
        return false;
    }
}
