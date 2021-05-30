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

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.CommandInterpreter;
import io.github.redpanda4552.HifumiBot.command.CommandMeta;
import io.github.redpanda4552.HifumiBot.util.EmbedUtil;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.EmbedBuilder;

public class CommandRun extends AbstractCommand
{

    public CommandRun()
    {
        super("run", CATEGORY_BUILTIN, true, false);
    }

    @Override
    protected void onExecute(CommandMeta cm)
    {
        if (cm.getArgs().length == 0)
        {
            EmbedBuilder eb = EmbedUtil.newFootedEmbedBuilder(cm.getMember());
            eb.setTitle("Run a background runnable immediately");
            eb.setDescription("`" + CommandInterpreter.PREFIX + this.getName() + " <runnable>`");
            StringBuilder sb = new StringBuilder();

            for (String name : HifumiBot.getSelf().getScheduler().getRunnableNames())
            {
                if (sb.length() != 0)
                {
                    sb.append(", ");
                }

                sb.append(name);
            }

            eb.addField("Available runnables", sb.toString(), false);
            Messaging.sendMessage(cm.getChannel(), eb.build());
            return;
        } else
        {
            String name = cm.getArgs()[0];
            boolean result = HifumiBot.getSelf().getScheduler().runScheduledNow(name);

            if (result)
            {
                Messaging.sendMessage(cm.getChannel(), "Sent an execute request for runnable '" + name
                        + "' to the thread pool; it will run whenever a thread is available to host it.");
            } else
            {
                Messaging.sendMessage(cm.getChannel(),
                        "Could not find a runnable with name '" + name + "'; use `" + CommandInterpreter.PREFIX
                                + this.getName() + "` with no args for a list of available runnables.");
            }
        }
    }

    @Override
    public String getHelpText()
    {
        return "Forcefully execute a runnable background task that normally executes on a schedule.";
    }

}
