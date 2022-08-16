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
import io.github.redpanda4552.HifumiBot.command.DynamicCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class CommandSupport extends AbstractSlashCommand {

    @Override
    protected void onExecute(SlashCommandInteractionEvent event) {
        OptionMapping nameOpt = event.getOption("name");
        
        if (nameOpt == null) {
            event.reply("Missing required option `name`").setEphemeral(true).queue();
            return;
        }
        
        OptionMapping userOpt = event.getOption("user");
        Member pingMember = null;
        
        if (userOpt != null) {
            pingMember = userOpt.getAsMember();
        }
        
        DynamicCommand toExecute = HifumiBot.getSelf().getCommandIndex().getDynamicCommand(nameOpt.getAsString());
        
        if (toExecute != null && toExecute.getCategory().equals("support")) {
            toExecute.execute(event, pingMember);
        } else {
            event.reply("Could not find support prompt `" + nameOpt.getAsString() + "`, use `/help support` to browse options").setEphemeral(true).queue();
        }
    }

    @Override
    protected CommandData defineSlashCommand() {
        return Commands.slash("support", "Displays a support prompt (use /help for a list of options)")
                .addOption(OptionType.STRING, "name", "Name of the support prompt command to use", true)
                .addOption(OptionType.USER, "user", "Specify a user to ping with this command", false);
    }

}
