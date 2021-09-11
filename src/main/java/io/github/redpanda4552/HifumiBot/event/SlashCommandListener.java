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
package io.github.redpanda4552.HifumiBot.event;

import java.util.HashMap;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import io.github.redpanda4552.HifumiBot.command.slash.CommandWiki;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SlashCommandListener extends ListenerAdapter {

    private HashMap<String, AbstractSlashCommand> slashCommands = HifumiBot.getSelf().getCommandIndex().getSlashCommands();
    
    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if (!event.isFromGuild()) {
            event.reply("Slash commands are disabled in DMs.").setEphemeral(true).queue();
            return;
        }
        
        if (slashCommands.containsKey(event.getName())) {
            slashCommands.get(event.getName()).executeIfPermission(event);
        }
    }
    
    @Override
    public void onButtonClick(ButtonClickEvent event) {
       String[] id = event.getComponentId().split(":");
        
        if (id.length != 3) {
            event.reply("Detected a damaged event identifier, aborting.").setEphemeral(true).queue();
            return;
        }
        
        String authorId = id[0];
        String commandSource = id[1];
        String payload = id[2];
        
        if (!authorId.equals(event.getUser().getId())) {
            event.reply("You did not send this original command; you are not allowed to interact with these buttons.").setEphemeral(true).queue();
            return;
        }
        
        switch (commandSource) {
        case "wiki":
            event.deferEdit().queue();
            CommandWiki commandWiki = (CommandWiki) slashCommands.get(commandSource);
            commandWiki.onButtonEvent(event, event.getButton().getLabel());
            break;
        }
    }
}
