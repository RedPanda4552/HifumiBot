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
package io.github.redpanda4552.HifumiBot.command;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.permissions.PermissionLevel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public abstract class AbstractSlashCommand {
    
    private PermissionLevel permissionLevel;
    
    public AbstractSlashCommand(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
    }
    
    public void executeIfPermission(SlashCommandEvent event) {
        if (HifumiBot.getSelf().getPermissionManager().hasPermission(permissionLevel, event.getUser(), event.getMember())) {
            onExecute(event);
        } else {
            event.reply("You do not have permission to use this command.").setEphemeral(true).queue();
        }
    }
    
    protected abstract void onExecute(SlashCommandEvent event);
    public void onButtonEvent(ButtonClickEvent event) { }
    public void onSelectionEvent(SelectionMenuEvent event) { }
    protected abstract CommandData defineSlashCommand();
    
    public void upsertSlashCommand() {
        String serverId = HifumiBot.getSelf().getConfig().server.id;
        
        if (serverId == null || serverId.isBlank()) {
            return;
        }
        
        CommandData commandData = defineSlashCommand();
        HifumiBot.getSelf().getJDA().getGuildById(serverId).upsertCommand(commandData).queue();
    }
}
