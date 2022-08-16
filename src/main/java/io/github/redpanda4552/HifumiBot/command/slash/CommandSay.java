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
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class CommandSay extends AbstractSlashCommand {

    @Override
    protected void onExecute(SlashCommandInteractionEvent event) {
        OptionMapping stringOpt = event.getOption("string");
        OptionMapping messageLinkOpt = event.getOption("message-link");
        
        String newContent = null;
        String messageLink = null;
        String[] parts = null;
        String messageId = null;
        String channelId = null;
        TextChannel channel = null;
        Message msg = null;
        
        switch (event.getSubcommandName()) {
        case "something":
            if (stringOpt == null) {
                requiredArgStop(event);
                return;
            }
            
            event.reply(stringOpt.getAsString()).queue();
            break;
        case "edit":
            if (messageLinkOpt == null || stringOpt == null) {
                requiredArgStop(event);
                return;
            }
            
            messageLink = messageLinkOpt.getAsString();
            newContent = stringOpt.getAsString();
            
            if (!messageLink.startsWith("http")) {
                event.reply("Error while parsing message-link, this doesn't look like a valid link.").setEphemeral(true).queue();
                return;
            }
            
            parts = messageLink.split("/");
            
            if (parts.length < 3) {
                event.reply("Error while parsing message-link, this doesn't look like a valid link.").setEphemeral(true).queue();
                return;
            }
            
            event.deferReply(true).queue();
            messageId = parts[parts.length - 1];
            channelId = parts[parts.length - 2];
            
            channel = HifumiBot.getSelf().getJDA().getTextChannelById(channelId);
            msg = channel.retrieveMessageById(messageId).complete();
            
            if (!msg.getAuthor().getId().equals(HifumiBot.getSelf().getJDA().getSelfUser().getId())) {
                event.getHook().sendMessage("You cannot edit a user's message, only the bot's messages.").queue();
                return;
            }
            
            event.getHook().editMessageById(messageId, newContent).queue();
            event.getHook().sendMessage("Edit complete!").queue();
            break;
        case "get":
            if (messageLinkOpt == null) {
                requiredArgStop(event);
                return;
            }
            
            messageLink = messageLinkOpt.getAsString();
            
            if (!messageLink.startsWith("http")) {
                event.reply("Error while parsing message-link, this doesn't look like a valid link.").setEphemeral(true).queue();
                return;
            }
            
            parts = messageLink.split("/");
            
            if (parts.length < 3) {
                event.reply("Error while parsing message-link, this doesn't look like a valid link.").setEphemeral(true).queue();
                return;
            }
            
            messageId = parts[parts.length - 1];
            channelId = parts[parts.length - 2];
            
            channel = HifumiBot.getSelf().getJDA().getTextChannelById(channelId);
            msg = channel.retrieveMessageById(messageId).complete();
            event.reply("```\n" + msg.getContentRaw() + "\n```").setEphemeral(true).queue();
            break;
        }
    }

    @Override
    protected CommandData defineSlashCommand() {
        SubcommandData something = new SubcommandData("something", "Make the bot say something")
                .addOption(OptionType.STRING, "string", "String content to say", true);
        SubcommandData edit = new SubcommandData("edit", "Edit something the bot has said")
                .addOption(OptionType.STRING, "message-link", "Link to the message to edit", true)
                .addOption(OptionType.STRING, "string", "String content to replace with", true);
        SubcommandData get = new SubcommandData("get", "Get the raw message content from something the bot has said")
                .addOption(OptionType.STRING, "message-link", "Link to the message to get", true);
        
        return Commands.slash("say", "Control basic bot message sending")
                .addSubcommands(something, edit, get);
                
    }
    
    private void requiredArgStop(SlashCommandInteractionEvent event) {
        Messaging.logInfo("CommandSay", "onExecute", "Missing required argument, command may have been tampered with.\nUser = " + event.getUser().getAsMention() + "\nChannel = " + event.getChannel().getAsMention());
        event.reply("Required argument missing - command aborted").setEphemeral(true).queue();
        return;
    }
}
