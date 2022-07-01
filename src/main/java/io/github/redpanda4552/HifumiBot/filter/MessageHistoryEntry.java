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

import java.time.Instant;

import net.dv8tion.jda.api.entities.Message;

public class MessageHistoryEntry {

    private String userId;
    private String serverId;
    private String channelId;
    private String messageId;
    private String messageContent;
    private Instant instant;
    
    public MessageHistoryEntry(Message msg) {
        this.userId = msg.getAuthor().getId();
        this.serverId = msg.getGuild().getId();
        this.channelId = msg.getChannel().getId();
        this.messageId = msg.getId();
        this.messageContent = msg.getContentRaw();
        this.instant = msg.getTimeCreated().toInstant();
    }
    
    public String getUserId() {
        return userId;
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
    
    public Instant getInstant() {
        return instant;
    }
}
