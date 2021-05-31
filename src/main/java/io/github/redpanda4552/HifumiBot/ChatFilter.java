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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ChatFilter
{

    private static final Pattern serverInvitePattern1 = Pattern.compile(".*https*://discord\\.gg/\\w+.*");
    private static final Pattern serverInvitePattern2 = Pattern.compile(".*https*://discord\\.com/invite/\\w+.*");

    public static boolean applyFilters(MessageReceivedEvent event)
    {
        if (HifumiBot.getSelf().getPermissionManager().hasPermission(event.getMember(), event.getAuthor()))
        {
            return false;
        }

        return filterServerInvites(event.getMessage());
    }

    private static boolean filterServerInvites(Message msg)
    {
        Matcher m1 = serverInvitePattern1.matcher(msg.getContentDisplay().toLowerCase());

        if (m1.matches())
        {
            Messaging.logInfo("ChatFilter", "filterServerInvites", "User " + msg.getAuthor().getAsMention()
                    + " attempted to send a server invite, deleting it... \n\nUser's message (formatting stripped):\n```"
                    + msg.getContentStripped() + "```");
            msg.delete().complete();
            return true;
        }

        Matcher m2 = serverInvitePattern2.matcher(msg.getContentDisplay().toLowerCase());

        if (m2.matches())
        {
            Messaging.logInfo("ChatFilter", "filterServerInvites", "User " + msg.getAuthor().getAsMention()
                    + " attempted to send a server invite, deleting it... \n\nUser's message (formatting stripped):\n```"
                    + msg.getContentStripped() + "```");
            msg.delete().complete();
            return true;
        }
        
        return false;
    }
}
