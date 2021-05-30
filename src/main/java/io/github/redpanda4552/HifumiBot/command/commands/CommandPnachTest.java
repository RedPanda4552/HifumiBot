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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.CommandMeta;
import io.github.redpanda4552.HifumiBot.parse.PnachParser;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;

public class CommandPnachTest extends AbstractCommand
{

    public CommandPnachTest()
    {
        super("pnachtest", CATEGORY_BUILTIN, false, false);
    }

    @Override
    protected void onExecute(CommandMeta cm)
    {
        List<Attachment> attachments = cm.getMessage().getAttachments();
        
        boolean pnachFound = checkAttachmentsForPnach(cm.getMessage(), attachments);
        
        if (!pnachFound)
        {
            for (String arg : cm.getArgs())
            {
                try
                {
                    new URL(arg);
                    int lastSlash = arg.lastIndexOf("/");
                    
                    if (lastSlash > 0 && arg.length() > lastSlash + 1)
                    {
                        Message msg = cm.getChannel().retrieveMessageById(arg.substring(lastSlash + 1)).complete();
                        pnachFound = checkAttachmentsForPnach(cm.getMessage(), msg.getAttachments());
                        break;
                    }
                }
                catch (MalformedURLException e) { }
            }
        }

        if (!pnachFound)
        {
            Messaging.sendMessage(cm.getChannel(), "No pnach found! Please attach your pnach to your message, **OR** include a Discord message link to a message containing a pnach attachment.");
        }
    }

    @Override
    public String getHelpText()
    {
        return "Test if a pnach is valid";
    }

    private boolean checkAttachmentsForPnach(Message msg, List<Attachment> attachments)
    {
        for (Attachment attachment : attachments)
        {
            if (attachment.getFileExtension().equalsIgnoreCase("pnach"))
            {
                PnachParser pp = new PnachParser(msg, attachment);
                HifumiBot.getSelf().getScheduler().runOnce(pp);
                return true;
            }
        }
        
        return false;
    }
}
