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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import io.github.redpanda4552.HifumiBot.permissions.PermissionLevel;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class CommandGameDB extends AbstractSlashCommand {

    private static final Pattern GAMEDB_SERIAL_PATTERN = Pattern.compile("^[A-Z]{4}-[0-9]{5}$");
    
    public CommandGameDB() {
        super(PermissionLevel.GUEST);
    }

    @Override
    protected void onExecute(SlashCommandEvent event) {
        event.deferReply().setEphemeral(true).queue();
        
        OptionMapping opt = event.getOption("serial");
        
        if (opt == null) {
            Messaging.logInfo("CommandGameDB", "onExecute", "Command tampering? Missing option 'serial' (user = " + event.getUser().getAsMention() + ")");
            event.getHook().sendMessage("Invalid option detected, admins have been alerted.").setEphemeral(true).queue();
            return;
        }
        
        String normalized = opt.getAsString().toUpperCase();
        Matcher m = GAMEDB_SERIAL_PATTERN.matcher(normalized);
        
        if (!m.matches()) {
            event.getHook().sendMessage("Invalid serial detected; serial numbers follow this format: `SLUS-12345`").setEphemeral(true).queue();
            return;
        }
        
        event.getHook().sendMessage(":information_source: Checking GameDB for serial `" + normalized + "`, this might take a moment...").setEphemeral(true).queue();
        MessageEmbed embed = HifumiBot.getSelf().getGameDB().present(normalized);
        event.getHook().sendMessageEmbeds(embed).setEphemeral(true).queue();
    }

    @Override
    protected CommandData defineSlashCommand() {
        CommandData data = new CommandData("gamedb", "Look up information stored in GameIndex.yaml (otherwise known as 'GameDB')")
                .addOption(OptionType.STRING, "serial", "Serial number to search for (e.g. 'SLUS-12345')", true);
        return data;
    }
}
