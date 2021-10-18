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
import io.github.redpanda4552.HifumiBot.command.DynamicCommand;
import io.github.redpanda4552.HifumiBot.permissions.PermissionLevel;
import io.github.redpanda4552.HifumiBot.util.Strings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class CommandDynCmd extends AbstractSlashCommand {
    
    public CommandDynCmd() {
        super(PermissionLevel.ADMIN);
    }

    @Override
    protected void onExecute(SlashCommandEvent event) {
        event.deferReply().setEphemeral(true).queue();
        String name = event.getOption("name").getAsString();
        OptionMapping categoryOpt = event.getOption("category");
        OptionMapping subGroupOpt = event.getOption("sub-group");
        OptionMapping helpOpt = event.getOption("help-text");
        OptionMapping titleOpt = event.getOption("title");
        OptionMapping bodyOpt = event.getOption("body");
        OptionMapping imageOpt = event.getOption("image-url");
        DynamicCommand dyncmd = null;
        
        switch (event.getSubcommandName()) {
        case "get":
            dyncmd = HifumiBot.getSelf().getCommandIndex().getDynamicCommand(name);
            
            if (dyncmd == null) {
                event.getHook().sendMessage("No such command `" + name + "` exists").queue();
                return;
            }
            
            event.getHook().sendMessageEmbeds(getDynamicCommandEmbedBuilder(dyncmd).build()).queue();
            return;
        case "new":
            String category = event.getOption("category").getAsString();
            String subGroup = subGroupOpt.getAsString();
            String helpText = event.getOption("help-text").getAsString();
            
            dyncmd = HifumiBot.getSelf().getCommandIndex().getDynamicCommand(name);
            
            if (dyncmd != null) {
                event.getHook().sendMessage("Command `" + name + "` already exists").queue();
                return;
            }
            
            dyncmd = new DynamicCommand(
                    name, 
                    category, 
                    subGroup,
                    helpText, 
                    titleOpt != null ? titleOpt.getAsString() : null, 
                    bodyOpt != null ? Strings.unescapeNewlines(bodyOpt.getAsString()) : null, 
                    imageOpt != null ? imageOpt.getAsString() : null);
            HifumiBot.getSelf().getCommandIndex().addDynamicCommand(dyncmd);
            event.getHook().sendMessageEmbeds(getDynamicCommandEmbedBuilder(dyncmd).build()).queue();
            return;
        case "update":
            dyncmd = HifumiBot.getSelf().getCommandIndex().getDynamicCommand(name);
            
            if (dyncmd == null) {
                event.getHook().sendMessage("No such command `" + name + "` exists").queue();
                return;
            }
            
            if (categoryOpt != null) {
                dyncmd.setCategory(categoryOpt.getAsString());
            }
            
            if (subGroupOpt != null) {
                dyncmd.setSubGroup(subGroupOpt.getAsString());
            }
            
            if (helpOpt != null) {
                dyncmd.setHelpText(helpOpt.getAsString());
            }
            
            if (titleOpt != null) {
                dyncmd.setTitle(titleOpt.getAsString());
            }
            
            if (bodyOpt != null) {
                dyncmd.setBody(Strings.unescapeNewlines(bodyOpt.getAsString()));
            }
            
            if (imageOpt != null) {
                dyncmd.setImageURL(imageOpt.getAsString());
            }
            
            HifumiBot.getSelf().getCommandIndex().addDynamicCommand(dyncmd);
            event.getHook().sendMessageEmbeds(getDynamicCommandEmbedBuilder(dyncmd).build()).queue();
            return;
        case "delete":
            if (!HifumiBot.getSelf().getCommandIndex().isCommand(name)) {
                event.getHook().sendMessage("No such command `" + name + "` exists").queue();
                return;
            }
            
            HifumiBot.getSelf().getCommandIndex().deleteCommand(name);
            event.getHook().sendMessage("Deleted command `" + name + "`").queue();
            return;
        }
    }

    @Override
    protected CommandData defineSlashCommand() {
        OptionData name = new OptionData(OptionType.STRING, "name", "The name of the command");
        OptionData category = new OptionData(OptionType.STRING, "category", "Category of the command")
                .addChoice("support", "support")
                .addChoice("memes", "memes");
        OptionData subGroup = new OptionData(OptionType.STRING, "sub-group", "Subcommand group that this command will be a member of");
        OptionData helpText = new OptionData(OptionType.STRING, "help-text", "Help text for the command");
        OptionData title = new OptionData(OptionType.STRING, "title", "Title portion of the command output");
        OptionData body = new OptionData(OptionType.STRING, "body", "Body portion of the command output");
        OptionData imageUrl = new OptionData(OptionType.STRING, "image-url", "URL of an image to display in the command output's embed");
        
        SubcommandData get = new SubcommandData("get", "Get attributes of a dynamic command")
                .addOptions(name.setRequired(true));
        SubcommandData newDyncmd = new SubcommandData("new", "Create a new dynamic command")
                .addOptions(
                        name.setRequired(true), 
                        category.setRequired(true), 
                        helpText.setRequired(true),
                        subGroup,
                        title, 
                        body, 
                        imageUrl);
        SubcommandData update = new SubcommandData("update", "Update a dynamic command")
                .addOptions(
                        name.setRequired(true), 
                        category.setRequired(false),
                        helpText.setRequired(false),
                        subGroup,
                        title, 
                        body, 
                        imageUrl);
        SubcommandData delete = new SubcommandData("delete", "Delete a dynamic command")
                .addOptions(name.setRequired(true));
        return new CommandData("dyncmd", "Manage dynamic commands")
                .addSubcommands(get, newDyncmd, update, delete);
    }
    
    private EmbedBuilder getDynamicCommandEmbedBuilder(DynamicCommand dyncmd) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(dyncmd.getName());
        eb.setDescription(dyncmd.getHelpText());

        if (dyncmd.getCategory() != null && !dyncmd.getCategory().isBlank()) {
            eb.addField("Category", dyncmd.getCategory(), true);
        }
        
        if (dyncmd.getSubGroup() != null && !dyncmd.getSubGroup().isBlank()) {
            eb.addField("Sub Group", dyncmd.getSubGroup(), true);
        }

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
