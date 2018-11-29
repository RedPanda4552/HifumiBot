/**
 * This file is part of HifumiBot, licensed under the MIT License (MIT)
 * 
 * Copyright (c) 2017 Brian Wood
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
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;

public class CommandWarez extends AbstractCommand {

    private final String RULES_CHANNEL = "welcome-rules";
    
    public CommandWarez(HifumiBot hifumiBot) {
        super(hifumiBot, true);
    }

    @Override
    protected void onExecute(MessageChannel channel, Member sender, String[] args) {
        if (!(channel instanceof TextChannel))
            return;
        
        EmbedBuilder eb = newFootedEmbedBuilder(sender);
        eb.setDescription("As per ");
        TextChannel welcomeRules = ((TextChannel) channel).getGuild().getTextChannelsByName(RULES_CHANNEL, false).get(0);
        eb.appendDescription(welcomeRules.getAsMention())
          .appendDescription(", our server does not support users who are found to be in possession of illegally obtained (e.g. \"pirated\", \"a friend gave me theirs\", etc) materials, such as a PS2 BIOS, game ISO, etc. ")
          .appendDescription("This rule is enforced at the discretion of admins and moderators. ")
          .appendDescription("You may appeal by contacting a moderator or admin. As a rule of thumb, they will want some visual proof that you actually own the item in question (physical PS2, if a BIOS, game disc, if a game ISO). ")
          .appendDescription("\n\n**The PCSX2 team thanks you for playing fair!**");
        hifumiBot.sendMessage(channel, eb.build());
    }

    @Override
    protected String getHelpText() {
        return "Print a dialog about warez/piracy rules";
    }
}
