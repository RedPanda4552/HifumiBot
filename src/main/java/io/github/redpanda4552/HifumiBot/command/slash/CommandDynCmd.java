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
import io.github.redpanda4552.HifumiBot.command.dynamic.DynamicChoice;
import io.github.redpanda4552.HifumiBot.command.dynamic.DynamicCommand;
import io.github.redpanda4552.HifumiBot.command.dynamic.DynamicSubcommand;
import io.github.redpanda4552.HifumiBot.config.ConfigManager;
import io.github.redpanda4552.HifumiBot.config.ConfigType;
import io.github.redpanda4552.HifumiBot.util.Strings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class CommandDynCmd extends AbstractSlashCommand {
    
    @Override
    protected void onExecute(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        OptionMapping commandOpt = event.getOption("command");
        OptionMapping subcommandOpt = event.getOption("subcommand");
        OptionMapping choiceOpt = event.getOption("choice");
        OptionMapping descriptionOpt = event.getOption("description");
        OptionMapping titleOpt = event.getOption("title");
        OptionMapping bodyOpt = event.getOption("body");
        OptionMapping imageOpt = event.getOption("image-url");

        if (commandOpt == null || subcommandOpt == null || choiceOpt == null) {
            event.getHook().sendMessage("Missing required arguments - a command, subcommand, and choice are required.").queue();
            return;
        }

        String commandStr = commandOpt.getAsString();
        String subcommandStr = subcommandOpt.getAsString();
        String choiceStr = choiceOpt.getAsString();
        String descriptionStr = descriptionOpt != null ? descriptionOpt.getAsString() : null;
        String titleStr = titleOpt != null ? titleOpt.getAsString() : null;
        String bodyStr = bodyOpt != null ? bodyOpt.getAsString() : null;
        String imageStr = imageOpt != null ? imageOpt.getAsString() : null;
        DynamicCommand command = null;
        DynamicSubcommand subcommand = null;
        DynamicChoice choice = null;
        
        switch (event.getSubcommandName()) {
        case "get":
            command = HifumiBot.getSelf().getDynCmdConfig().dynamicCommands.get(commandStr);

            if (command == null) {
                event.getHook().sendMessage("No such command `" + commandStr + "` exists").queue();
                return;
            }

            subcommand = command.getSubcommand(subcommandStr);

            if (subcommand == null) {
                event.getHook().sendMessage("No such subcommand `" + subcommandStr + "` exists").queue();
                return;
            }

            choice = subcommand.getChoice(choiceStr);
            
            if (choice == null) {
                event.getHook().sendMessage("No such choice `" + choiceStr + "` exists").queue();
                return;
            }
            
            event.getHook().sendMessageEmbeds(getDynamicCommandEmbedBuilder(choice).build()).queue();
            return;
        case "new":
            command = HifumiBot.getSelf().getDynCmdConfig().dynamicCommands.get(commandStr);

            if (command != null) {
                subcommand = command.getSubcommand(subcommandStr);

                if (subcommand != null) {
                    choice = subcommand.getChoice(choiceStr);
                
                    if (choice != null) {
                        event.getHook().sendMessage("Command `" + commandStr + " " + subcommandStr + " " + choiceStr + "` exists").queue();
                        return;
                    }
                }
            }

            if (descriptionStr == null) {
                event.getHook().sendMessage("Missing required argument `description`").queue();
                return;
            }
            
            choice = new DynamicChoice(choiceStr, descriptionStr, titleStr, bodyStr, imageStr);
            
            if (subcommand == null) {
                subcommand = new DynamicSubcommand(subcommandStr, descriptionStr);
            }
            
            subcommand.putChoice(choice);
            
            if (command == null) {
                command = new DynamicCommand(commandStr, descriptionStr);
            }

            command.putSubcommand(subcommand);

            HifumiBot.getSelf().getDynCmdConfig().dynamicCommands.put(commandStr, command);
            ConfigManager.write(HifumiBot.getSelf().getDynCmdConfig());
            HifumiBot.getSelf().getCommandIndex().rebuild();
            event.getHook().sendMessageEmbeds(getDynamicCommandEmbedBuilder(choice).build()).queue();
            return;
        case "update":
            command = HifumiBot.getSelf().getDynCmdConfig().dynamicCommands.get(commandStr);

            if (command == null) {
                event.getHook().sendMessage("No such command `" + commandStr + "` exists").queue();
                return;
            }

            subcommand = command.getSubcommand(subcommandStr);

            if (subcommand == null) {
                event.getHook().sendMessage("No such subcommand `" + subcommandStr + "` exists").queue();
                return;
            }

            choice = subcommand.getChoice(choiceStr);
            
            if (choice == null) {
                event.getHook().sendMessage("No such choice `" + choiceStr + "` exists").queue();
                return;
            }

            if (descriptionStr != null) {
                choice.setDescription(descriptionStr);
            }
            
            if (titleStr != null) {
                choice.setTitle(titleStr);
            }
            
            if (bodyStr != null) {
                choice.setBody(bodyStr);
            }
            
            if (imageStr != null) {
                choice.setImageURL(imageStr);
            }

            subcommand.putChoice(choice);
            command.putSubcommand(subcommand);
            
            HifumiBot.getSelf().getDynCmdConfig().dynamicCommands.put(commandStr, command);
            ConfigManager.write(HifumiBot.getSelf().getDynCmdConfig());
            HifumiBot.getSelf().getCommandIndex().rebuild();
            event.getHook().sendMessageEmbeds(getDynamicCommandEmbedBuilder(choice).build()).queue();
            return;
        case "delete":
            command = HifumiBot.getSelf().getDynCmdConfig().dynamicCommands.get(commandStr);

            if (command == null) {
                event.getHook().sendMessage("No such command `" + commandStr + "` exists").queue();
                return;
            }

            subcommand = command.getSubcommand(subcommandStr);

            if (subcommand == null) {
                event.getHook().sendMessage("No such subcommand `" + subcommandStr + "` exists").queue();
                return;
            }

            choice = subcommand.getChoice(choiceStr);
            
            if (choice == null) {
                event.getHook().sendMessage("No such choice `" + choiceStr + "` exists").queue();
                return;
            }
            
            subcommand.clearChoice(choiceStr);

            if (subcommand.getChoices().size() == 0) {
                HifumiBot.getSelf().getDynCmdConfig().dynamicCommands.remove(commandStr);
            } else {
                command.putSubcommand(subcommand);
                HifumiBot.getSelf().getDynCmdConfig().dynamicCommands.put(commandStr, command);
            }

            ConfigManager.write(HifumiBot.getSelf().getDynCmdConfig());
            HifumiBot.getSelf().getCommandIndex().rebuild();
            event.getHook().sendMessage("Deleted command `" + commandStr + " " + subcommandStr + " " + choiceStr + "`").queue();
            return;
        }
    }

    @Override
    protected CommandData defineSlashCommand() {
        OptionData command = new OptionData(OptionType.STRING, "command", "Command name");
        OptionData subcommand = new OptionData(OptionType.STRING, "subcommand", "Subcommand name");
        OptionData choice = new OptionData(OptionType.STRING, "choice", "Choice name");
        OptionData description = new OptionData(OptionType.STRING, "description", "Description");
        OptionData title = new OptionData(OptionType.STRING, "title", "Title portion of the command output");
        OptionData body = new OptionData(OptionType.STRING, "body", "Body portion of the command output");
        OptionData imageUrl = new OptionData(OptionType.STRING, "image-url", "URL of an image to display in the command output's embed");
        
        SubcommandData get = new SubcommandData("get", "Get attributes of a dynamic command")
                .addOptions(
                    command.setRequired(true), 
                    subcommand.setRequired(true), 
                    choice.setRequired(true));
        SubcommandData newDyncmd = new SubcommandData("new", "Create a new dynamic command")
                .addOptions(
                        command.setRequired(true),
                        subcommand.setRequired(true),
                        choice.setRequired(true), 
                        description.setRequired(true),
                        title.setRequired(false), 
                        body.setRequired(false), 
                        imageUrl.setRequired(false));
        SubcommandData update = new SubcommandData("update", "Update a dynamic command")
                .addOptions(
                        command.setRequired(true),
                        subcommand.setRequired(true),
                        choice.setRequired(true),
                        description.setRequired(false),
                        title.setRequired(false), 
                        body.setRequired(false), 
                        imageUrl.setRequired(false));
        SubcommandData delete = new SubcommandData("delete", "Delete a dynamic command")
                .addOptions(
                    command.setRequired(true), 
                    subcommand.setRequired(true), 
                    choice.setRequired(true));
        return Commands.slash("dyncmd", "Manage dynamic commands")
                .addSubcommands(get, newDyncmd, update, delete)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }
    
    private EmbedBuilder getDynamicCommandEmbedBuilder(DynamicChoice dyncmd) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(dyncmd.getName());
        eb.setDescription(dyncmd.getDescription());

        if (dyncmd.getTitle() != null && !dyncmd.getTitle().isBlank()) {
            eb.addField("Title", dyncmd.getTitle(), true);
        }

        if (dyncmd.getBody() != null && !dyncmd.getBody().isBlank()) {
            eb.addField("Body", "```\n" + dyncmd.getBody() + "\n```", false);
        }

        if (dyncmd.getImageURL() != null && !dyncmd.getImageURL().isBlank()) {
            eb.addField("Image URL", "<" + dyncmd.getImageURL() + ">", false);
        }
        
        return eb;
    }
}
