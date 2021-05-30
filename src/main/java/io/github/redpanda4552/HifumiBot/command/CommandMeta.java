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
package io.github.redpanda4552.HifumiBot.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public class CommandMeta
{

    private String command;
    private boolean admin;
    private boolean restricted;
    private String category;
    private Guild guild;
    private MessageChannel channel;
    private Member member;
    private User user;
    private Message message;
    private List<Member> mentions;
    private String[] args;
    private HashMap<String, String> switches;

    public CommandMeta(String command, boolean admin, boolean restricted, String category, Guild guild,
            MessageChannel channel, Member member, User user, Message message, List<Member> mentions, String[] args)
    {
        this.command = command;
        this.admin = admin;
        this.restricted = restricted;
        this.category = category;
        this.guild = guild;
        this.channel = channel;
        this.member = member;
        this.user = user;
        this.message = message;
        this.mentions = mentions;
        formatArgs(args);
    }

    private void formatArgs(String[] args)
    {
        ArrayList<String> newArgs = new ArrayList<String>();
        switches = new HashMap<String, String>();
        String toInsert = null, switchStart = null;
        boolean openQuote = false, openSwitch = false;

        for (String arg : args)
        {
            if (openQuote)
            {
                toInsert += " " + arg;

                if (arg.endsWith("\""))
                {
                    openQuote = false;

                    if (openSwitch)
                    {
                        openSwitch = false;
                        switches.put(switchStart, toInsert.replaceAll("\"", ""));
                    }
                    else
                    {
                        newArgs.add(toInsert.replaceAll("\"", ""));
                    }
                }
            }
            else if (!openSwitch && arg.startsWith("-"))
            {
                openSwitch = true;
                switchStart = arg.replaceFirst("-+", "");
            }
            else if (!openQuote && arg.startsWith("\"") && !arg.endsWith("\""))
            {
                openQuote = true;
                toInsert = arg;
            }
            else if (openSwitch)
            {
                openSwitch = false;
                switches.put(switchStart, arg);
            }
            else
            {
                newArgs.add(arg);
            }
        }

        this.args = newArgs.toArray(new String[newArgs.size()]);
    }

    public String getCommand()
    {
        return command;
    }

    public boolean isAdmin()
    {
        return admin;
    }

    public boolean isRestricted()
    {
        return restricted;
    }

    public String getCategory()
    {
        return category;
    }

    public Guild getGuild()
    {
        return guild;
    }

    public MessageChannel getChannel()
    {
        return channel;
    }

    public Member getMember()
    {
        return member;
    }

    public User getUser()
    {
        return user;
    }

    public Message getMessage()
    {
        return message;
    }

    public List<Member> getMentions()
    {
        return mentions;
    }

    public String[] getArgs()
    {
        return args;
    }

    public HashMap<String, String> getSwitches()
    {
        return switches;
    }
}
