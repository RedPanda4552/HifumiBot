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
package io.github.redpanda4552.HifumiBot;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import io.github.redpanda4552.HifumiBot.permissions.PermissionLevel;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.entities.Message;

public class MessageHistory {

    public class MessageHistoryEntry {
        public Instant created;
        public String channelId;
        public String messageId;
        public String messageContent;
        
        public MessageHistoryEntry(Message msg) {
            created = msg.getTimeCreated().toInstant();
            channelId = msg.getChannel().getId();
            messageId = msg.getId();
            messageContent = msg.getContentRaw();
        }
    }
    
    public class MessageHistoryUser {
        public ArrayList<MessageHistoryEntry> entries;
        public int duplicateCount;
        
        public MessageHistoryUser() {
            entries = new ArrayList<MessageHistoryEntry>();
            duplicateCount = 0;
        }
    }
    
    private ConcurrentHashMap<String, MessageHistoryUser> messages = new ConcurrentHashMap<String, MessageHistoryUser>();
    
    public MessageHistory() {
        
    }
    
    public synchronized void addMessage(Message msg) {
        if (msg == null || msg.getContentRaw().isBlank() || HifumiBot.getSelf().getPermissionManager().hasPermission(PermissionLevel.MOD, msg.getMember())) {
            return;
        }
        
        String userId = msg.getAuthor().getId();
        MessageHistoryUser mhu = null;
        
        if (!messages.containsKey(userId)) {
            mhu = new MessageHistoryUser();
        } else {
            mhu = messages.get(userId);
        }
        
        mhu.entries.add(new MessageHistoryEntry(msg));
        
        while (mhu.entries.size() > HifumiBot.getSelf().getConfig().messageHistoryOptions.trackedMessages) {
            mhu.entries.remove(0);
        }
        
        mhu.duplicateCount = 0;

        for (MessageHistoryEntry mhe : mhu.entries) {
            if (mhe.messageContent.equals(msg.getContentRaw())) {
                mhu.duplicateCount++;
            }
        }
        
        messages.put(userId, mhu);
        
        if (mhu.duplicateCount >= HifumiBot.getSelf().getConfig().messageHistoryOptions.maxDuplicates) {
            Messaging.logInfo("MessageHistory", "addMessage", msg.getMember().getAsMention() + " (" + msg.getMember().getUser().getName() + "#" + msg.getMember().getUser().getDiscriminator() + ") set off the duplicate message detection, but no action was taken (this feature is being tested for now)");
            /*
            HifumiBot.getSelf().getKickHandler().doKick(msg.getMember());
            Messaging.logInfo("MessageHistory", "addMessage", "Successfully messaged and kicked " + msg.getMember().getAsMention() + " (" + msg.getMember().getUser().getName() + "#" + msg.getMember().getUser().getDiscriminator() + ") for exceeding the maximum number of duplicate messages.");
            
            for (MessageHistoryEntry mhe : mhu.entries) {
                TextChannel channel = msg.getGuild().getTextChannelById(mhe.channelId);
                channel.deleteMessageById(mhe.messageId).queue();
            }
            */
        }
    }
    
    public synchronized void clean() {
        for (String userId : messages.keySet()) {
            if (messages.containsKey(userId)) {
                MessageHistoryUser mhu = messages.get(userId);
                
                if (mhu != null) {
                    ArrayList<MessageHistoryEntry> entries = mhu.entries;
                    
                    if (!entries.isEmpty() && Duration.between(Instant.now(), entries.get(entries.size() - 1).created).toMinutes() > 15) {
                        messages.remove(userId);
                    }
                }
            }
        }
    }
}
