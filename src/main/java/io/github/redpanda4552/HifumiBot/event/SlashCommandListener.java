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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import io.github.redpanda4552.HifumiBot.command.dynamic.DynamicChoice;
import io.github.redpanda4552.HifumiBot.command.dynamic.DynamicCommand;
import io.github.redpanda4552.HifumiBot.command.dynamic.DynamicSubcommand;
import io.github.redpanda4552.HifumiBot.command.slash.CommandEmulog;
import io.github.redpanda4552.HifumiBot.command.slash.CommandWiki;
import io.github.redpanda4552.HifumiBot.database.CommandEventObject;
import io.github.redpanda4552.HifumiBot.database.Database;
import io.github.redpanda4552.HifumiBot.event.ButtonInteractionElement.ButtonType;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class SlashCommandListener extends ListenerAdapter {

    private HashMap<String, AbstractSlashCommand> slashCommands = HifumiBot.getSelf().getCommandIndex().getSlashCommands();
    private HashMap<UUID, ButtonInteractionElement> buttonCache = new HashMap<UUID, ButtonInteractionElement>();
    private HashMap<UUID, SelectionInteractionElement> selectionCache = new HashMap<UUID, SelectionInteractionElement>();
    
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        // First, abort if not in a server
        if (!event.isFromGuild()) {
            event.reply("Slash commands are disabled in DMs.").setEphemeral(true).queue();
            return;
        }

        // Fetch the last occurrence of this command, in this channel,
        // from another user, within the ninja time, if available.
        Optional<CommandEventObject> recentCommandInstance = Database.getLatestCommandEventNotFromUser(event.getChannelIdLong(), event.getCommandIdLong(), event.getUser().getIdLong());

        // Store this command event to database
        Database.insertCommandEvent(
            event.getCommandIdLong(), 
            "slash", 
            event.getName(), 
            event.getSubcommandGroup(), 
            event.getSubcommandName(), 
            event.getIdLong(), 
            event.getUser(),
            event.getChannelIdLong(),
            event.getTimeCreated().toEpochSecond(), 
            recentCommandInstance.isPresent(),
            event.getOptions()
        );

        // Now abort if it was a ninja
        if (recentCommandInstance.isPresent()) {
            event.reply(":ninja:").setEphemeral(true).queue();
            return;
        }

        // Finally, try processing the command and pass along to the appropriate command to execute
        if (slashCommands.containsKey(event.getName())) {
            try {
                slashCommands.get(event.getName()).onExecute(event);
            } catch (Exception e) {
                e.printStackTrace();
                Messaging.logException("SlashCommandListener", "onSlashCommand", e);
                event.getHook().sendMessage("An internal exception occurred and has been reported to admins.").setEphemeral(true).queue();
            }
        } else if (HifumiBot.getSelf().getDynCmdConfig().dynamicCommands.containsKey(event.getName())) {
            DynamicCommand command = HifumiBot.getSelf().getDynCmdConfig().dynamicCommands.get(event.getName());
            
            if (command.getSubcommands().containsKey(event.getSubcommandGroup())) {
                DynamicSubcommand subcommand = command.getSubcommand(event.getSubcommandGroup());
                
                if (subcommand.getChoices().containsKey(event.getSubcommandName())) {
                    DynamicChoice choice = subcommand.getChoice(event.getSubcommandName());

                    OptionMapping mentionOpt = event.getOption("mention");

                    if (mentionOpt != null) {
                        choice.execute(event, mentionOpt.getAsMember());
                    } else {
                        choice.execute(event);
                    }
                }
            }
        } else {
            Messaging.logInfo("SlashCommandListener", "onSlashCommandInteraction", "Reveived slash command `" + event.getName() + "`, but we don't have any kind of handler for it!");
        }
    }
    
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();

        String[] parts = componentId.split(":");

        if (parts.length < 2) {
            Messaging.logInfo("SlashCommandListener", "onButtonInteraction", "Received a button click event, but got a malformed button ID. Received:\n```\n" + componentId + "\n```");
            event.reply("Something went wrong with this button. Admins have been notified.").setEphemeral(true).queue();
            return;
        }

        String reply = null;

        switch (parts[0]) {
            case "emulog_prev":
            case "emulog_next":
                CommandEmulog commandEmulog = (CommandEmulog) slashCommands.get("emulog");
                event.deferEdit().queue();
                commandEmulog.onButtonEvent(event);
                break;
            case "timeout":
                try {
                    if (event.getMember().hasPermission(Permission.MODERATE_MEMBERS)) {
                        event.getGuild().retrieveMemberById(parts[1]).complete().timeoutFor(Duration.ofMinutes(60)).queue();
                        reply = "Member timed out successfully!";
                    } else {
                        reply = "You don't have permission to timeout members";
                    }    
                } catch (Exception e) {
                    reply = "An error occurred while attempting to timeout the member - are they still in the server?";
                    reply += "\nException message: " + e.getMessage();
                }

                event.reply(reply).queue();
                break;
            case "kick":
                try {
                    if (event.getMember().hasPermission(Permission.KICK_MEMBERS)) {
                        event.getGuild().retrieveMemberById(parts[1]).complete().kick().queue();
                        reply = "Member kicked successfully!";
                    } else {
                        reply = "You don't have permission to kick members";
                    }    
                } catch (Exception e) {
                    reply = "An error occurred while attempting to timeout the member - are they still in the server?";
                    reply += "\nException message: " + e.getMessage();
                }

                event.reply(reply).queue();
                break;
            case "ban":
                try {
                    if (event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
                        event.getGuild().retrieveMemberById(parts[1]).complete().ban(1, TimeUnit.HOURS).queue();
                        reply = "Member banned successfully!";
                    } else {
                        reply = "You don't have permission to ban members";
                    }    
                } catch (Exception e) {
                    reply = "An error occurred while attempting to ban the member - are they still in the server?";
                    reply += "\nException message: " + e.getMessage();
                }

                event.reply(reply).queue();
                break;
        }
    }
    
    @Override 
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
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
            commandWiki.onStringSelectEvent(event);
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
    
    public synchronized ButtonInteractionElement getButton(String uuidString) {
        return buttonCache.get(UUID.fromString(uuidString));
    }
}
