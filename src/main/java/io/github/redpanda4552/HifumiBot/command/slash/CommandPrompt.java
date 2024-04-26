package io.github.redpanda4552.HifumiBot.command.slash;

import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

public class CommandPrompt extends AbstractSlashCommand {

    @Override
    protected void onExecute(SlashCommandInteractionEvent event) {
        TextInput titleInput = TextInput.create("title", "Set Title", TextInputStyle.SHORT)
                .setMinLength(1)
                .setMaxLength(MessageEmbed.TITLE_MAX_LENGTH)
                .setRequired(true)
                .build();
        TextInput bodyInput = TextInput.create("body", "Set Body Content", TextInputStyle.PARAGRAPH)
                .setMinLength(1)
                .setMaxLength(4000)
                .setRequired(true)
                .build();

        Modal modal = Modal.create("prompt", "Make a new prompt")
                .addComponents(ActionRow.of(titleInput), ActionRow.of(bodyInput))
                .build();

        event.replyModal(modal).queue();
    }

    @Override
    protected CommandData defineSlashCommand() {
        return Commands.slash("prompt", "Open text inputs to generate a custom embed")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

}
