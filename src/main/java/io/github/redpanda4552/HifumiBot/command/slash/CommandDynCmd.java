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
import io.github.redpanda4552.HifumiBot.util.EmbedUtil;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

public class CommandDynCmd extends AbstractSlashCommand {
    
    @Override
    protected void onExecute(SlashCommandInteractionEvent event) {
        OptionMapping commandOpt = event.getOption("command");
        OptionMapping subcommandOpt = event.getOption("subcommand");
        OptionMapping choiceOpt = event.getOption("choice");
        
        if (commandOpt == null || subcommandOpt == null || choiceOpt == null) {
            event.reply("Missing required arguments - a command, subcommand, and choice are required.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Validate that the subcommand is one of the four accepted options: get, delete, new, update
        switch (event.getSubcommandName()) {
            case "get":
            case "delete":
            case "new":
            case "update":
                break;
            default:
                event.reply("Invalid subcommand; please stop trying to break things")
                        .setEphemeral(true)
                        .queue();
                return;
        }

        String commandStr = commandOpt.getAsString();
        String subcommandStr = subcommandOpt.getAsString();
        String choiceStr = choiceOpt.getAsString();
        DynamicCommand command = HifumiBot.getSelf().getDynCmdConfig().dynamicCommands.get(commandStr);
        DynamicSubcommand subcommand = (command != null ? command.getSubcommand(subcommandStr) : null);
        DynamicChoice choice = (subcommand != null ? subcommand.getChoice(choiceStr) : null);

        // First, check for subcommand "get" - we don't need any modals, it's just an embed printout.
        if (event.getSubcommandName().equals("get")) {
            if (choice == null) {
                event.reply("No such command `" + commandStr + " " + subcommandStr + " " + choiceStr + "` exists")
                        .setEphemeral(true)
                        .queue();
                return;
            }
            
            event.replyEmbeds(EmbedUtil.getDynamicCommandEmbedBuilder(choice).build())
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Delete also does not depend on modal input, look for it next.
        if (event.getSubcommandName().equals("delete")) {
            if (choice == null) {
                event.reply("No such command `" + commandStr + " " + subcommandStr + " " + choiceStr + "` exists")
                        .setEphemeral(true)
                        .queue();
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
            event.reply("Deleted command `" + commandStr + " " + subcommandStr + " " + choiceStr + "`")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // New and update should prompt the user to input new values
        TextInput.Builder commandName = TextInput.create("command", "Command Name (DO NOT EDIT ME)", TextInputStyle.SHORT);
        TextInput.Builder descriptionInput = TextInput.create("description", "Description", TextInputStyle.SHORT)
                .setMinLength(1)
                .setMaxLength(SlashCommandData.MAX_DESCRIPTION_LENGTH)
                .setRequired(true);
        TextInput.Builder titleInput = TextInput.create("title", "Title", TextInputStyle.SHORT)
                .setMinLength(1)
                .setMaxLength(MessageEmbed.TITLE_MAX_LENGTH)
                .setRequired(true);
        TextInput.Builder bodyInput = TextInput.create("body", "Body", TextInputStyle.PARAGRAPH)
                .setMinLength(1)
                .setMaxLength(4000)
                .setRequired(true);
        TextInput.Builder imageInput = TextInput.create("image", "Image URL (Optional)", TextInputStyle.SHORT)
                .setMinLength(12)
                .setMaxLength(255)
                .setRequired(false);

        // If update specifically, plug in the existing values.
        if (event.getSubcommandName().equals("update")) {
            if (choice == null) {
                event.reply("No such command `" + commandStr + " " + subcommandStr + " " + choiceStr + "` exists")
                        .setEphemeral(true)
                        .queue();
                return;
            }
            
            descriptionInput.setValue(choice.getDescription());
            titleInput.setValue(choice.getTitle());
            bodyInput.setValue(choice.getBody());
            imageInput.setValue(choice.getImageURL());
        }

        commandName.setValue(commandStr + " " + subcommandStr + " " + choiceStr);

        Modal.Builder modal = Modal.create("dyncmd", "Make a new prompt")
                .addComponents(
                    ActionRow.of(commandName.build()),
                    ActionRow.of(descriptionInput.build()),
                    ActionRow.of(titleInput.build()),
                    ActionRow.of(bodyInput.build()),
                    ActionRow.of(imageInput.build())
                );

        event.replyModal(modal.build()).queue();
    }

    @Override
    protected CommandData defineSlashCommand() {
        OptionData command = new OptionData(OptionType.STRING, "command", "Command name");
        OptionData subcommand = new OptionData(OptionType.STRING, "subcommand", "Subcommand name");
        OptionData choice = new OptionData(OptionType.STRING, "choice", "Choice name");
        
        SubcommandData get = new SubcommandData("get", "Get attributes of a dynamic command")
                .addOptions(
                        command.setRequired(true), 
                        subcommand.setRequired(true), 
                        choice.setRequired(true)
                );
        SubcommandData delete = new SubcommandData("delete", "Delete a dynamic command")
                .addOptions(
                        command.setRequired(true), 
                        subcommand.setRequired(true), 
                        choice.setRequired(true)
                );
        SubcommandData newDyncmd = new SubcommandData("new", "Create a new dynamic command")
                .addOptions(
                        command.setRequired(true),
                        subcommand.setRequired(true),
                        choice.setRequired(true)
                );
        SubcommandData update = new SubcommandData("update", "Update a dynamic command")
                .addOptions(
                        command.setRequired(true),
                        subcommand.setRequired(true),
                        choice.setRequired(true)
                );
        return Commands.slash("dyncmd", "Manage dynamic commands")
                .addSubcommands(get, delete, newDyncmd, update)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }
}
