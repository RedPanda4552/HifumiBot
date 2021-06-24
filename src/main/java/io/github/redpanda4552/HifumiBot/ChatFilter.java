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
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ChatFilter
{
    /**
     * Applies any filters specified in the filterExpressions property of the config.
     * <br/>
     * <i>Note: All messages are flushed to lower case; regular expressions should
     * be written for lower case, or be case-agnostic.</i>
     * @param event - The MessageReceivedEvent to filter.
     * @return True if a regular expression matched and the message was filtered out, false otherwise.
     */
    public static boolean applyFilters(MessageReceivedEvent event)
    {
        if (HifumiBot.getSelf().getPermissionManager().hasPermission(event.getMember(), event.getAuthor()))
        {
            return false;
        }
        
        for (String filterExpression : HifumiBot.getSelf().getConfig().filterExpressions)
        {
            Pattern p = Pattern.compile(filterExpression);
            Matcher m = p.matcher(event.getMessage().getContentDisplay().toLowerCase());
            
            if (m.matches())
            {
                event.getMessage().delete().complete();
                Messaging.logInfo("ChatFilter", "applyFilters", "Message from user " + event.getMessage().getAuthor().getAsMention() + " was filtered.\n\nUser's message (formatting stripped):\n```\n" + event.getMessage().getContentStripped() + "\n```\nMatched filter:\n```\n" + filterExpression + "\n```");
                return true;
            }
        }

        return false;
    }
}
