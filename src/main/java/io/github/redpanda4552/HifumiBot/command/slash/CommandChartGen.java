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

        FileUpload file = null;

        switch (typeOpt.getAsString()) {
            case "warez": {
                file = FileUpload.fromData(ChartGenerator.buildWarezChart(), "warez.png");
                break;
            }
            case "member-year": {
                file = FileUpload.fromData(ChartGenerator.buildMemberChartYear(), "member.png"); 
                break;
            }
            case "member-week": {
                file = FileUpload.fromData(ChartGenerator.buildMemberChartWeek(), "member.png");
                break;
            }
            default: {
                event.reply("Unknown chart type").setEphemeral(true).queue();
                return;
            }
        }

        MessageCreateBuilder mb = new MessageCreateBuilder();
        mb.addFiles(file);
        event.reply(mb.build()).queue();
    }

    @Override
    protected CommandData defineSlashCommand() {
        OptionData typeOption = new OptionData(OptionType.STRING, "type", "Type of chart to generate", true);
        typeOption.addChoice("warez", "warez");
        typeOption.addChoice("member-week", "member-week");
        typeOption.addChoice("member-year", "member-year");

        return Commands.slash("chartgen", "Generate a chart")
                .addOptions(typeOption)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

}

