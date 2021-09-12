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

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import io.github.redpanda4552.HifumiBot.command.slash.CommandWiki;
import io.github.redpanda4552.HifumiBot.event.ButtonInteractionElement.ButtonType;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SlashCommandListener extends ListenerAdapter {

    private HashMap<String, AbstractSlashCommand> slashCommands = HifumiBot.getSelf().getCommandIndex().getSlashCommands();
    private HashMap<UUID, ButtonInteractionElement> buttonCache = new HashMap<UUID, ButtonInteractionElement>();
    private HashMap<UUID, SelectionInteractionElement> selectionCache = new HashMap<UUID, SelectionInteractionElement>();
    
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
    public void onSelectionMenu(SelectionMenuEvent event) {
        UUID uuid = null;
        
        try {
            uuid = UUID.fromString(event.getComponentId());
        } catch (IllegalArgumentException e) {
            Messaging.logException("SlashCommandListener", "onSelectionMenu", e);
            event.reply("Selection tampering detected, admins have been notified.").setEphemeral(true).queue();
            return;
        }
        
        if (!selectionCache.containsKey(uuid)) {
            event.reply("Whoops! This selection has expired. You'll need to run the command again to get an active selection.").setEphemeral(true).queue();
            return;
        }
        
        SelectionInteractionElement selection = selectionCache.get(uuid);
        
        if (!selection.getUserId().equals(event.getUser().getId())) {
            event.reply("You did not send this original command; you are not allowed to interact with this selection.").setEphemeral(true).queue();
            return;
        }
        
        switch (selection.getCommandName()) {
        case "wiki":
            event.deferEdit().queue();
            CommandWiki commandWiki = (CommandWiki) slashCommands.get(selection.getCommandName());
            commandWiki.onSelectionEvent(event);
            break;
        }
    }
    
    public synchronized ButtonInteractionElement newButton(String userId, String commandName, String label, ButtonType buttonType) {
        ButtonInteractionElement button = new ButtonInteractionElement(userId, commandName, label, buttonType);
        this.buttonCache.put(button.getUUID(), button);
        return button;
    }
    
    public synchronized void cleanInteractionElements() {
        for (UUID key : this.buttonCache.keySet()) {
            if (Duration.between(this.buttonCache.get(key).getCreatedInstant(), Instant.now()).toMillis() > HifumiBot.getSelf().getConfig().slashCommands.timeoutSeconds * 1000) {
                this.buttonCache.remove(key);
            }
        }
        
        for (UUID key : this.selectionCache.keySet()) {
            if (Duration.between(this.selectionCache.get(key).getCreatedInstant(), Instant.now()).toMillis() > HifumiBot.getSelf().getConfig().slashCommands.timeoutSeconds * 1000) {
                this.selectionCache.remove(key);
            }
        }
    }
    
    public synchronized SelectionInteractionElement newSelection(String userId, String commandName) {
        SelectionInteractionElement selection = new SelectionInteractionElement(userId, commandName);
        this.selectionCache.put(selection.getUUID(), selection);
        return selection;
    }
}
