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
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class Messaging
{

    public static Message sendMessage(String channelId, String... msg)
    {
        return Messaging.sendMessage(HifumiBot.getSelf().getJDA().getTextChannelById(channelId), msg);
    }

    public static Message sendMessage(String channelId, MessageEmbed embed)
    {
        return Messaging.sendMessage(HifumiBot.getSelf().getJDA().getTextChannelById(channelId), embed);
    }

    public static Message sendMessage(MessageChannel channel, MessageEmbed embed)
    {
        return channel.sendMessage(embed).complete();
    }

    public static Message sendMessage(MessageChannel channel, String... strArr)
    {
        MessageBuilder mb = new MessageBuilder();

        for (String str : strArr)
        {
            mb.append(str);
        }

        return Messaging.sendMessage(channel, mb.build());
    }

    public static Message sendMessage(MessageChannel channel, Message msg)
    {
        return channel.sendMessage(msg).complete();
    }

    public static void logInfo(String className, String methodName, String msg)
    {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Info from " + className + "." + methodName);
        eb.addField("Message", msg, false);
        Messaging.sendMessage(HifumiBot.getSelf().getConfig().systemOutputChannelId, eb.build());
    }

    public static void logException(String className, String methodName, Exception e)
    {
        if (HifumiBot.getSelf().getJDA() == null)
        {
            e.printStackTrace();
            return;
        }
        
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Exception caught in " + className + "." + methodName);
        eb.addField("Message", e.getMessage(), false);
        StringBuilder sb = new StringBuilder();

        for (StackTraceElement ste : e.getStackTrace())
        {
            sb.append(ste.toString()).append("\n");
        }

        eb.addField("Stack Trace", StringUtils.abbreviate(sb.toString(), 1024), false);

        if (e.getCause() != null)
        {
            eb.addField("Caused By", e.getCause().getMessage(), false);
            sb = new StringBuilder();

            for (StackTraceElement ste : e.getCause().getStackTrace())
            {
                sb.append(ste.toString()).append("\n");
            }

            eb.addField("Caused By Stack Trace", StringUtils.abbreviate(sb.toString(), 1024), false);
        }

        Messaging.sendMessage(HifumiBot.getSelf().getConfig().systemOutputChannelId, eb.build());
    }
}
