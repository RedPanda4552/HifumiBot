// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.command.slash;

import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import io.github.redpanda4552.HifumiBot.command.dynamic.DynamicChoice;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class CommandDynCmd extends AbstractSlashCommand {

  @Override
  protected void onExecute(SlashCommandInteractionEvent event) {
    event.reply("Temporarily disabled").setEphemeral(true).queue();
    return;
    /*
            event.deferReply().setEphemeral(true).queue();
            String name = event.getOption("name").getAsString();
            OptionMapping categoryOpt = event.getOption("category");
            OptionMapping subCategoryOpt = event.getOption("sub-category");
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
                String category = categoryOpt.getAsString();
                String subCategory = subCategoryOpt.getAsString();
                String helpText = helpOpt.getAsString();

                dyncmd = HifumiBot.getSelf().getCommandIndex().getDynamicCommand(name);

                if (dyncmd != null) {
                    event.getHook().sendMessage("Command `" + name + "` already exists").queue();
                    return;
                }

                dyncmd = new DynamicCommand(
                        category,
                        subCategory,
                        name,
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
                if (!HifumiBot.getSelf().getCommandIndex().isDynamicCommand(name)) {
                    event.getHook().sendMessage("No such command `" + name + "` exists").queue();
                    return;
                }

                HifumiBot.getSelf().getCommandIndex().deleteDynamicCommand(name);
                event.getHook().sendMessage("Deleted command `" + name + "`").queue();
                return;
            }
    */
  }

  @Override
  protected CommandData defineSlashCommand() {
    OptionData category =
        new OptionData(OptionType.STRING, "category", "Category of the command")
            .addChoice("support", "support")
            .addChoice("memes", "memes");
    OptionData subCategory =
        new OptionData(OptionType.STRING, "sub-category", "Sub-category of the command");
    OptionData name = new OptionData(OptionType.STRING, "name", "The name of the command");
    OptionData helpText =
        new OptionData(OptionType.STRING, "help-text", "Help text for the command");
    OptionData title =
        new OptionData(OptionType.STRING, "title", "Title portion of the command output");
    OptionData body =
        new OptionData(OptionType.STRING, "body", "Body portion of the command output");
    OptionData imageUrl =
        new OptionData(
            OptionType.STRING,
            "image-url",
            "URL of an image to display in the command output's embed");

    SubcommandData get =
        new SubcommandData("get", "Get attributes of a dynamic command")
            .addOptions(name.setRequired(true));
    SubcommandData newDyncmd =
        new SubcommandData("new", "Create a new dynamic command")
            .addOptions(
                category.setRequired(true),
                subCategory.setRequired(true),
                name.setRequired(true),
                helpText.setRequired(true),
                title,
                body,
                imageUrl);
    SubcommandData update =
        new SubcommandData("update", "Update a dynamic command")
            .addOptions(
                category.setRequired(true),
                subCategory.setRequired(true),
                name.setRequired(true),
                helpText.setRequired(false),
                title,
                body,
                imageUrl);
    SubcommandData delete =
        new SubcommandData("delete", "Delete a dynamic command").addOptions(name.setRequired(true));
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
