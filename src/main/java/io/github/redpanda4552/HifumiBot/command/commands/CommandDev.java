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

import io.github.redpanda4552.HifumiBot.command.CommandMeta;
import io.github.redpanda4552.HifumiBot.util.EmbedUtil;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

public class CommandDev extends AbstractCommand
{

    private final String DEV_CHANNEL = "dev-builds";

    public CommandDev()
    {
        super("dev", CATEGORY_BUILTIN, false, false);
    }

    @Override
    protected void onExecute(CommandMeta cm)
    {
        if (!(cm.getChannel() instanceof TextChannel))
        {
            Messaging.sendMessage(cm.getChannel(), "Sorry, but this command can only be used within the PCSX2 server.");
            return;
        }

        EmbedBuilder eb = EmbedUtil.newFootedEmbedBuilder(cm.getMember());
        eb.setTitle("PCSX2 Development Builds");
        eb.setDescription(
                "Problems? Looking for PCSX2 updates? Consider using PCSX2 development builds! I post a message in ");
        TextChannel devBuilds = cm.getGuild().getTextChannelsByName(DEV_CHANNEL, false).get(0);
        eb.appendDescription(devBuilds.getAsMention()).appendDescription(" whenever a new development build is ready!");
        Messaging.sendMessage(cm.getChannel(), eb.build());
    }

    @Override
    public String getHelpText()
    {
        return "Print a dialog about development builds";
    }

}
