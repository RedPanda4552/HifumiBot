package io.github.redpanda4552.HifumiBot.command.slash;

import io.github.redpanda4552.HifumiBot.charting.ChartGenerator;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public class CommandChartGen extends AbstractSlashCommand {

    @Override
    protected void onExecute(SlashCommandInteractionEvent event) {
        OptionMapping typeOpt = event.getOption("type");
        
        if (typeOpt == null) {
            event.reply("Missing required option 'type'").setEphemeral(true).queue();
            return;
        }

        switch (typeOpt.getAsString()) {
            case "warez": {
                FileUpload file = FileUpload.fromData(ChartGenerator.buildWarezChart(), "warez.png");
                MessageCreateBuilder mb = new MessageCreateBuilder();
                mb.addFiles(file);
                event.reply(mb.build()).queue();
                break;
            }
            default: {
                event.reply("Unknown chart type").setEphemeral(true).queue();
                break;
            }
        }
    }

    @Override
    protected CommandData defineSlashCommand() {
        OptionData typeOption = new OptionData(OptionType.STRING, "type", "Type of chart to generate", true);
        typeOption.addChoice("warez", "warez");

        return Commands.slash("chartgen", "Generate a chart")
                .addOptions(typeOption)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

}

