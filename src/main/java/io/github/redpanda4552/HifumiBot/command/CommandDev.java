/**
 * This file is part of HifumiBot, licensed under the MIT License (MIT)
 * 
 * Copyright (c) 2018 Brian Wood
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

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.util.CommandMeta;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;

public class CommandDev extends AbstractCommand {

    private final String DEV_CHANNEL = "dev-builds";
    
    public CommandDev(HifumiBot hifumiBot) {
        super(hifumiBot, false, CATEGORY_BUILTIN);
    }

    @Override
    protected void onExecute(CommandMeta cm) {
        if (!(cm.getChannel() instanceof TextChannel))
            return;
        
        EmbedBuilder eb = newFootedEmbedBuilder(cm.getMember());
        eb.setTitle("PCSX2 Development Builds");
        eb.setDescription("Looking for the latest builds of PCSX2? Check out ");
        TextChannel devBuilds = cm.getGuild().getTextChannelsByName(DEV_CHANNEL, false).get(0);
        eb.appendDescription(devBuilds.getAsMention())
          .appendDescription(" for a link to the build bot! I post a new message there whenever a new development build is ready!");
        hifumiBot.sendMessage(cm.getChannel(), eb.build());
    }

    @Override
    protected String getHelpText() {
        return "Print a dialog about development builds";
    }

}
