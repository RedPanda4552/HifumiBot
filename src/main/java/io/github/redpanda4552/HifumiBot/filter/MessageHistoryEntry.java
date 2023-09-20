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
import java.time.OffsetDateTime;
import java.util.ArrayList;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;

public class MessageHistoryEntry {

    public String userId;
    public String serverId;
    public String channelId;
    public String messageId;
    public String messageContent;
    public OffsetDateTime dateTime;
    public ArrayList<String> attachmentUrls;
    public int count;
    public String referencedMessageLink;
    
    public MessageHistoryEntry(Message msg) {
        this.userId = msg.getAuthor().getId();
        this.serverId = msg.getGuild().getId();
        this.channelId = msg.getChannel().getId();
        this.messageId = msg.getId();
        this.messageContent = msg.getContentRaw();
        this.dateTime = msg.getTimeCreated();
        this.attachmentUrls = new ArrayList<String>();

        for (Attachment attachment : msg.getAttachments()) {
            this.attachmentUrls.add(attachment.getProxyUrl());
        }

        this.count = 1;
        
        Message ref = msg.getReferencedMessage();

        if (ref != null) {
            this.referencedMessageLink = ref.getJumpUrl();
        }
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
    
    public OffsetDateTime getDateTime() {
        return dateTime;
    }

    public ArrayList<String> getAttachmentUrls() {
        return attachmentUrls;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
