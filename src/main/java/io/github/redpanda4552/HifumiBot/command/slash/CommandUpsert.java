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
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class CommandUpsert extends AbstractSlashCommand {

    public CommandUpsert() {
        super(PermissionLevel.SUPERUSER);
    }

    @Override
    protected void onExecute(SlashCommandEvent event) {
        event.deferReply(true).queue();
        
        if (event.getSubcommandName().equals("name")) {
            HifumiBot.getSelf().getCommandIndex().upsertSlashCommand(event.getOption("name").getAsString());
        } else {
            HifumiBot.getSelf().getCommandIndex().upsertAllSlashCommands(event.getSubcommandName());
        }
        
        event.getHook().sendMessage("Slash commands updated!").setEphemeral(true).queue();
    }

    @Override
    protected CommandData defineSlashCommand() {
        SubcommandData subAll = new SubcommandData("all", "Upsert all slash commands at once, will likely exceed rate limiting");
        SubcommandData subNew = new SubcommandData("new", "Upsert only slash commands which do not exist on Discord servers yet");
        SubcommandData name = new SubcommandData("name", "Upsert one slash command, specified by name")
                .addOption(OptionType.STRING, "name", "Name of the slash command to upsert", true);
        
        return new CommandData("upsert", "Upsert all slash commands to the configured server.")
                .addSubcommands(subAll, subNew, name);
    }

}
