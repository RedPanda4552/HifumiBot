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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.permissions.PermissionLevel;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ChatFilter
{
    HashMap<String, ArrayList<Pattern>> patternMap = new HashMap<String, ArrayList<Pattern>>();
    
    public ChatFilter()
    {
        this.compile();
    }
    
    public synchronized void compile()
    {
        patternMap.clear();
        
        for (Filter filter : HifumiBot.getSelf().getConfig().filters.values())
        {
            ArrayList<Pattern> patterns = new ArrayList<Pattern>();

            for (String regex : filter.regexes.values())
            {
                patterns.add(Pattern.compile(regex));
            }
            
            patternMap.put(filter.name, patterns);
        }
    }
    
    /**
     * Applies any filters specified in the filters property of the config.
     * <br/>
     * <i>Note: All messages are flushed to lower case; regular expressions should
     * be written for lower case, or be case-agnostic.</i>
     * @param event - The MessageReceivedEvent to filter.
     * @return True if a regular expression matched and the message was filtered out, false otherwise.
     */
    public synchronized boolean applyFilters(MessageReceivedEvent event)
    {
        if (event.getAuthor().getId().equals(HifumiBot.getSelf().getJDA().getSelfUser().getId()))
        {
            return false;
        }
            
        if (HifumiBot.getSelf().getPermissionManager().hasPermission(PermissionLevel.MOD, event.getAuthor(), event.getMember()))
        {
            return false;
        }
        
        for (String filterName : patternMap.keySet())
        {
            for (Pattern p : patternMap.get(filterName))
            {
                Matcher m = p.matcher(event.getMessage().getContentDisplay().toLowerCase());
                
                if (m.matches())
                {
                    event.getMessage().delete().complete();
                    String replyMessage = HifumiBot.getSelf().getConfig().filters.get(filterName).replyMessage;
                    
                    if (!replyMessage.isBlank())
                    {
                        Messaging.sendMessage(event.getChannel(), replyMessage);
                    }
                    
                    Messaging.logInfo("ChatFilter", "applyFilters", "Message from user " + event.getMessage().getAuthor().getAsMention() + " was filtered.\n\nUser's message (formatting stripped):\n```\n" + event.getMessage().getContentStripped() + "\n```\nMatched this regular expression in filter `" + filterName + "` :\n```\n" + p.pattern() + "\n```");
                    return true;
                }
            }
        }

        return false;
    }
}
