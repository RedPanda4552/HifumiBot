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
package io.github.redpanda4552.HifumiBot.command.commands;

import java.util.ArrayList;
import java.util.HashMap;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.CommandMeta;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

public class CommandHelp extends AbstractCommand
{

    public CommandHelp()
    {
        super("help", CATEGORY_BUILTIN, false, false);
    }

    @Override
    protected void onExecute(CommandMeta cm)
    {
        String category = "builtin";
        int pageNumber = 1;
        MessageEmbed toSend = null;
        HashMap<String, ArrayList<MessageEmbed>> helpPages = HifumiBot.getSelf().getCommandIndex().getHelpPages();

        if (cm.getArgs().length >= 1 && helpPages.get(cm.getArgs()[0]) != null)
        {
            category = cm.getArgs()[0];

            if (cm.getArgs().length >= 2)
            {
                try
                {
                    pageNumber = Integer.parseInt(cm.getArgs()[1]);
                } catch (NumberFormatException e)
                {
                }
            }

            if (pageNumber > helpPages.get(category).size())
                pageNumber = helpPages.get(category).size() - 1;

            if (pageNumber < 1)
                pageNumber = 1;

            toSend = helpPages.get(category).get(pageNumber - 1);
        } else
        {
            toSend = HifumiBot.getSelf().getCommandIndex().getHelpRootPage();
        }

        try
        {
            Messaging.sendMessage(cm.getUser().openPrivateChannel().complete(), toSend);
        } catch (ErrorResponseException e)
        {
            Messaging.sendMessage(cm.getChannel(),
                    "Sorry, `help` works through DMs to avoid clutter, but your DMs are not open to members of this server!");
        }

    }

    @Override
    public String getHelpText()
    {
        return "Display this help dialog";
    }
}
