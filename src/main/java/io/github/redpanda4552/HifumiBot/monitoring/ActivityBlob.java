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
package io.github.redpanda4552.HifumiBot.monitoring;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.collections4.Bag;
import org.apache.commons.lang3.StringUtils;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public class ActivityBlob {

    private String userId;
    private OffsetDateTime createdTime;
    private OffsetDateTime lastEditTime;
    private String serverId;
    private String channelId;
    private String messageId;
    private String messageContent;
    private HashMap<String, Integer> mentionedRoleIds = new HashMap<String, Integer>();
    private HashMap<String, Integer> mentionedUserIds = new HashMap<String, Integer>();
    private boolean mentionedEveryone = false;
    
    public ActivityBlob(Message message) {
        this.userId = message.getAuthor().getId();
        this.createdTime = message.getTimeCreated();
        this.lastEditTime = message.getTimeEdited();
        this.serverId = message.getGuild().getId();
        this.channelId = message.getChannel().getId();
        this.messageId = message.getId();
        this.messageContent = StringUtils.truncate(message.getContentRaw(), 256);
        
        processMentions(message);
    }
    
    private void processMentions(Message message) {
        Bag<Role> rolesBag = message.getMentionedRolesBag();
        rolesBag.forEach((role) -> {
            mentionedRoleIds.put(role.getId(), rolesBag.getCount(role));
        });
        
        Bag<User> usersBag = message.getMentionedUsersBag();
        usersBag.forEach((user) -> {
            mentionedUserIds.put(user.getId(), usersBag.getCount(user));
        });
        
        mentionedEveryone = message.mentionsEveryone();
    }
    
    public String getUserId() {
        return userId;
    }
    
    public OffsetDateTime getCreatedTime() {
        return createdTime;
    }
    
    public OffsetDateTime getLastEditTime() {
        return lastEditTime;
    }
    
    public String getServerId() {
        return serverId;
    }
    
    public String getChannelId() {
        return channelId;
    }
    
    public String getMessageId() {
        return messageId;
    }
    
    public String getMessageContent() {
        return messageContent;
    }
    
    public Set<String> getMentionedRoleIds() {
        return mentionedRoleIds.keySet();
    }
    
    public Integer getRoleMentionCount(String roleId) {
        Integer ret = mentionedRoleIds.get(roleId);
        return ret != null ? ret : 0;
    }
    
    public Set<String> getMentionedUserIds() {
        return mentionedUserIds.keySet();
    }
    
    public Integer getUserMentionCount(String userId) {
        Integer ret = mentionedUserIds.get(userId);
        return ret != null ? ret : 0;
    }
    
    public Integer getTotalMentionCount() {
        Integer ret = 0;
        
        for (String roleId : mentionedRoleIds.keySet()) {
            Integer i = mentionedRoleIds.get(roleId);
            ret += (i != null ? i : 0);
        }
        
        for (String userId : mentionedUserIds.keySet()) {
            Integer i = mentionedUserIds.get(userId);
            ret += (i != null ? i : 0);
        }
        
        return ret;
    }
    
    public boolean hasMentionedEveryone() {
        return mentionedEveryone;
    }
}
