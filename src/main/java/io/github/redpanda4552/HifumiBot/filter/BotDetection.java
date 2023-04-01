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

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class BotDetection {

    private ConcurrentHashMap<String, ArrayList<MessageHistoryEntry>> messageHistory;
    
    public BotDetection() {
        this.messageHistory = new ConcurrentHashMap<String, ArrayList<MessageHistoryEntry>>();
    }
    
    public void addMessageHistoryEntry(Message msg) {
        // If message has no link, we don't care.
        if (!HyperlinkCleaner.hasHyperlink(msg)) {
            return;
        }
        
        String userId = msg.getAuthor().getId();
        ArrayList<MessageHistoryEntry> entries = null;
        
        if (messageHistory.containsKey(userId)) {
            entries = messageHistory.get(userId);
        }
        
        if (entries == null) {
            entries = new ArrayList<MessageHistoryEntry>();
        }
        
        MessageHistoryEntry mhe = new MessageHistoryEntry(msg);
        entries.add(mhe);
        entries = trim(entries);
        
        boolean res = evaluate(userId, entries);

        if (res) {
            removeBot(userId);
            
            if (messageHistory.containsKey(userId)) {
                messageHistory.remove(userId);
            }
        } else {
            messageHistory.put(userId, entries);
        }
    }
    
    private boolean evaluate(String userId, ArrayList<MessageHistoryEntry> entries) {
        if (entries.size() <= 1) {
            return false;
        }
        
        HashMap<String, ArrayList<String>> duplicateTracking = new HashMap<String, ArrayList<String>>();
        
        for (MessageHistoryEntry entry : entries) {
            ArrayList<String> channels = null;
            
            if (duplicateTracking.containsKey(entry.getMessageContent())) {
                channels = duplicateTracking.get(entry.getMessageContent());
            } else {
                channels = new ArrayList<String>();
            }
            
            if (!channels.contains(entry.getChannelId())) {
                channels.add(entry.getChannelId());
            }
            
            if (channels.size() >= 3) {
                return true;
            } else {
                duplicateTracking.put(entry.getMessageContent(), channels);
            }
        }
        
        return false;
    }
    
    private void removeBot(String userId) {
        User user = HifumiBot.getSelf().getJDA().getUserById(userId);
        String serverId = messageHistory.get(userId).get(0).getServerId();
        Guild server = HifumiBot.getSelf().getJDA().getGuildById(serverId);
        
        if (user != null) {
            Member member = server.retrieveMemberById(userId).complete();
            Messaging.sendPrivateMessage(user, HifumiBot.getSelf().getConfig().filterOptions.kickMessage);
            member.kick().complete();
            Messaging.logInfo("BotDetection", "removeBot", "Successfully messaged and kicked " + member.getUser().getAsMention() + " (" + member.getUser().getName() + "#" + member.getUser().getDiscriminator() + ") for setting off bot detection.");
            
            for (MessageHistoryEntry entry : messageHistory.get(userId)) {
                TextChannel channel = HifumiBot.getSelf().getJDA().getTextChannelById(entry.getChannelId());
                channel.deleteMessageById(entry.getMessageId()).complete();
            }
            
            messageHistory.remove(userId);
        }
    }
    
    public void clean() {
        Instant now = Instant.now();
        
        for (String userId : messageHistory.keySet()) {
            ArrayList<MessageHistoryEntry> entries = messageHistory.get(userId);
            
            while (!entries.isEmpty()) {
                MessageHistoryEntry entry = entries.get(0);
                
                if (Duration.between(entry.getInstant(), now).abs().toMinutes() > 15) {
                    entries.remove(0);
                } else {
                    break;
                }
            }
            
            if (entries.isEmpty()) {
                messageHistory.remove(userId);
            }
        }
    }
    
    private static ArrayList<MessageHistoryEntry> trim(ArrayList<MessageHistoryEntry> entries) {
        while (entries.size() > 5) {
            entries.remove(0);
        }
        
        return entries;
    }
}
