package io.github.redpanda4552.HifumiBot.event;

import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import io.github.redpanda4552.HifumiBot.command.slash.CommandEmulog;
import io.github.redpanda4552.HifumiBot.command.slash.CommandWhois;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ButtonEventListener extends ListenerAdapter {

    private HashMap<String, AbstractSlashCommand> slashCommands = HifumiBot.getSelf().getCommandIndex().getSlashCommands();
    
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
            case "whois":
                CommandWhois commandWhois = (CommandWhois) slashCommands.get("whois");
                event.deferEdit().queue();
                commandWhois.handleButtonEvent(event);
                break;
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
}
