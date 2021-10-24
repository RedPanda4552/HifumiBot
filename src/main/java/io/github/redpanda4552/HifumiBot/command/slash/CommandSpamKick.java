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
package io.github.redpanda4552.HifumiBot.command.slash;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import io.github.redpanda4552.HifumiBot.permissions.PermissionLevel;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class CommandSpamKick extends AbstractSlashCommand {

    public CommandSpamKick() {
        super(PermissionLevel.SUPER_ADMIN);
    }

    @Override
    protected void onExecute(SlashCommandEvent event) {
        OptionMapping opt = event.getOption("user");
        
        if (opt == null) {
            event.reply("Required option `user` missing").setEphemeral(true);
            return;
        }
        
        Member member = opt.getAsMember();
        
        try {
            HifumiBot.getSelf().getKickHandler().doKick(member);
            event.reply("Successfully messaged and kicked " + member.getUser().getAsMention()).setEphemeral(true).queue();
        } catch (Exception e) {
            Messaging.logException("CommandSpamKick", "onExecute", e);
            event.reply("An internal error occurred, check the bot logging channel").setEphemeral(true).queue();
        }
    }

    @Override
    protected CommandData defineSlashCommand() {
        return new CommandData("spamkick", "Send a user a DM telling them their account is compromised and spamming, then kick the user")
                .addOption(OptionType.USER, "user", "User to DM and kick", true);
    }

}
