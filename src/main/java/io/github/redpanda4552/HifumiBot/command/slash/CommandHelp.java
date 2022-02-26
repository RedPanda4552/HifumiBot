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

import java.util.ArrayList;
import java.util.HashMap;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import io.github.redpanda4552.HifumiBot.event.ButtonInteractionElement;
import io.github.redpanda4552.HifumiBot.event.ButtonInteractionElement.ButtonType;
import io.github.redpanda4552.HifumiBot.permissions.PermissionLevel;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandHelp extends AbstractSlashCommand {
    
    public CommandHelp() {
        super(PermissionLevel.GUEST);
    }

    @Override
    protected void onExecute(SlashCommandEvent event) {
        event.deferReply().setEphemeral(true).queue();
        String category = event.getOption("category").getAsString();
        HashMap<String, ArrayList<MessageEmbed>> helpPages = HifumiBot.getSelf().getCommandIndex().getHelpPages();
        ButtonInteractionElement prev = HifumiBot.getSelf().getSlashCommandListener().newButton(event.getUser().getId(), event.getName() + "_prev", "Previous", ButtonType.SECONDARY);
        ButtonInteractionElement next = HifumiBot.getSelf().getSlashCommandListener().newButton(event.getUser().getId(), event.getName() + "_next", "Next", ButtonType.PRIMARY);
        event.getHook().sendMessageEmbeds(helpPages.get(category).get(0)).addActionRow(prev.getButton(), next.getButton()).queue();
    }
    
    @Override 
    public void onButtonEvent(ButtonClickEvent event) {
        try {
            if (event.getMessage().getEmbeds().isEmpty()) {
                event.getHook().sendMessage("It looks like the help embed was deleted. Try using the help command again.").setEphemeral(true).queue();
                return;
            }
            
            if (event.getMessage().getEmbeds().size() > 1) {
                event.getHook().sendMessage("Embed error. Try using the help command again.").setEphemeral(true).queue();
                return;
            }
            
            MessageEmbed embed = event.getMessage().getEmbeds().get(0);
            String[] categoryParts = embed.getTitle().split(" - ");
            
            if (categoryParts.length != 2) {
                event.getHook().sendMessage("Category error. Try using the help command again.").setEphemeral(true).queue();
            }
            
            String category = categoryParts[1];
            
            String[] pageParts = embed.getFooter().getText().split(" / ");
            
            if (pageParts.length != 2) {
                event.getHook().sendMessage("Page error. Try using the help command again.").setEphemeral(true).queue();
                return;
            }
            
            int currentPage = 1;
            int totalPages = 1;
            
            try {
                currentPage = Integer.valueOf(pageParts[0]);
                totalPages = Integer.valueOf(pageParts[1]);
            } catch (NumberFormatException e) {
                event.getHook().sendMessage("Page number error. Try using the help command again.").setEphemeral(true).queue();
                return;
            }
            
            ButtonInteractionElement button = HifumiBot.getSelf().getSlashCommandListener().getButton(event.getButton().getId());
            String buttonValue = button.getCommandName();
            
            switch (buttonValue) {
            case "help_prev":
                 currentPage--;
                 break;
            case "help_next":
                currentPage++;
                break;
            }
            
            if (currentPage < 1) {
                currentPage = 1;
            } else if (currentPage > totalPages) {
                currentPage = totalPages;
            }
            
            HashMap<String, ArrayList<MessageEmbed>> helpPages = HifumiBot.getSelf().getCommandIndex().getHelpPages();
            ButtonInteractionElement prev = HifumiBot.getSelf().getSlashCommandListener().newButton(event.getUser().getId(), "help_prev", "Previous", ButtonType.SECONDARY);
            ButtonInteractionElement next = HifumiBot.getSelf().getSlashCommandListener().newButton(event.getUser().getId(), "help_next", "Next", ButtonType.PRIMARY);
            event.getHook().editOriginalEmbeds(helpPages.get(category).get((int) currentPage - 1)).setActionRow(prev.getButton(), next.getButton()).queue();
        } catch (Exception e) {
            event.getHook().editOriginal("An internal error occurred, aborting.").queue();
            Messaging.logException("CommandWiki", "onButtonEvent", e);
        }
    }

    @Override
    protected CommandData defineSlashCommand() {
        OptionData categoryOpt = new OptionData(OptionType.STRING, "category", "Dynamic command category")
                .addChoice("support", "support")
                .addChoice("memes", "memes")
                .setRequired(true);
        return new CommandData("help", "Help prompt for all dynamic commands")
                .addOptions(categoryOpt);
    }
}
