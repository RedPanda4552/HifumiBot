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
package io.github.redpanda4552.HifumiBot.util;

import org.apache.commons.lang3.StringUtils;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public class Messaging {
    
    public static Message sendPrivateMessage(User user, String str) {
        MessageBuilder mb = new MessageBuilder(str);
        return sendPrivateMessage(user, mb.build());
    }
    
    public static Message sendPrivateMessage(User user, Message msg) {
        PrivateChannel channel = user.openPrivateChannel().complete();
        return channel.sendMessage(msg).complete();
    }
    
    public static Message sendMessage(MessageChannel channel, String str) {
        MessageBuilder mb = new MessageBuilder(str);
        return Messaging.sendMessage(channel, mb.build(), null, null);
    }
    
    public static Message sendMessage(MessageChannel channel, String str, Message toReference, boolean pingReference) {
        MessageBuilder mb = new MessageBuilder(str);
        return Messaging.sendMessage(channel, mb.build(), null, null, null, null, toReference, pingReference);
    }

    public static Message sendMessage(MessageChannel channel, Message msg) {
        return Messaging.sendMessage(channel, msg, null, null);
    }

    public static Message sendMessage(MessageChannel channel, String str, String fileName, String fileContents) {
        MessageBuilder mb = new MessageBuilder(str);
        return Messaging.sendMessage(channel, mb.build(), fileName, fileContents);
    }

    public static Message sendMessage(MessageChannel channel, Message msg, String fileName, String fileContents) {
        return Messaging.sendMessage(channel, msg, fileName, fileContents, null, null);
    }
    
    public static Message sendMessage(MessageChannel channel, String str, String fileName, String fileContents, String linkLabel, String linkDestination) {
        MessageBuilder mb = new MessageBuilder(str);
        return Messaging.sendMessage(channel, mb.build(), fileName, fileContents, linkLabel, linkDestination); 
    }
    
    public static Message sendMessage(MessageChannel channel, Message msg, String fileName, String fileContents, String linkLabel, String linkDestination) {
        return Messaging.sendMessage(channel, msg, fileName, fileContents, linkLabel, linkDestination, null, false);
    }
    
    public static Message sendMessage(MessageChannel channel, String str, String fileName, String fileContents, String linkLabel, String linkDestination, Message toReference, boolean pingReference) {
        MessageBuilder mb = new MessageBuilder(str);
        return Messaging.sendMessage(channel, mb.build(), fileName, fileContents, linkLabel, linkDestination, toReference, pingReference); 
    }
    
    public static Message sendMessage(MessageChannel channel, Message msg, String fileName, String fileContents, String linkLabel, String linkDestination, Message toReference, boolean pingReference) {
        MessageAction action = channel.sendMessage(msg);
        
        if (fileName != null && !fileName.isBlank() && fileContents != null && !fileContents.isBlank()) {
            action.addFile(fileContents.getBytes(), fileName);
        }
        
        if (linkLabel != null && !linkLabel.isBlank() && linkDestination != null && !linkDestination.isBlank()) {
            action.setActionRow(Button.link(linkDestination, linkLabel));
        }
        
        if (toReference != null) {
            action.reference(toReference);
            action.mentionRepliedUser(pingReference);
        }
        
        return action.complete();
    }

    public static Message sendMessageEmbed(String channelId, MessageEmbed embed) {
        return Messaging.sendMessageEmbed(HifumiBot.getSelf().getJDA().getTextChannelById(channelId), embed);
    }

    public static Message sendMessageEmbed(MessageChannel channel, MessageEmbed embed) {
        return channel.sendMessageEmbeds(embed).complete();
    }

    public static Message editMessageEmbed(Message msg, MessageEmbed embed) {
        return msg.editMessageEmbeds(embed).complete();
    }

    public static void logInfo(String className, String methodName, String msg) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Info from " + className + "." + methodName);
        eb.addField("Message", msg, false);
        Messaging.sendMessageEmbed(HifumiBot.getSelf().getConfig().channels.systemOutputChannelId, eb.build());
    }

    public static void logException(String className, String methodName, Exception e) {
        if (HifumiBot.getSelf().getJDA() == null) {
            e.printStackTrace();
            return;
        }

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Exception caught in " + className + "." + methodName);
        eb.addField("Message", e.getMessage(), false);
        StringBuilder sb = new StringBuilder();

        for (StackTraceElement ste : e.getStackTrace()) {
            sb.append(ste.toString()).append("\n");
        }

        eb.addField("Stack Trace", StringUtils.abbreviate(sb.toString(), 1024), false);

        if (e.getCause() != null) {
            eb.addField("Caused By", e.getCause().getMessage(), false);
            sb = new StringBuilder();

            for (StackTraceElement ste : e.getCause().getStackTrace()) {
                sb.append(ste.toString()).append("\n");
            }

            eb.addField("Caused By Stack Trace", StringUtils.abbreviate(sb.toString(), 1024), false);
        }

        Messaging.sendMessageEmbed(HifumiBot.getSelf().getConfig().channels.systemOutputChannelId, eb.build());
    }

    public static boolean messageHasEmulog(Message msg) {
        for (Attachment attachment : msg.getAttachments()) {
            if (attachment.getFileName().equalsIgnoreCase("emulog.txt")) {
                return true;
            }
        }

        return false;
    }

    public static boolean messageHasPnach(Message msg) {
        for (Attachment attachment : msg.getAttachments()) {
            if (attachment.getFileExtension().equalsIgnoreCase("pnach")) {
                return true;
            }
        }

        return false;
    }
}
