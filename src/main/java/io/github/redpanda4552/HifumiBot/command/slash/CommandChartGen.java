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
        OptionMapping timeUnitOpt = event.getOption("time-unit");
        OptionMapping lengthOpt = event.getOption("length");
        
        if (typeOpt == null || timeUnitOpt == null || lengthOpt == null) {
            event.reply("Missing required options").setEphemeral(true).queue();
            return;
        }

        FileUpload file = null;

        switch (typeOpt.getAsString()) {
            case "warez": {
                file = FileUpload.fromData(ChartGenerator.buildWarezChart(timeUnitOpt.getAsString(), lengthOpt.getAsLong()), "warez.png");
                break;
            }
            case "member": {
                file = FileUpload.fromData(ChartGenerator.buildMemberChart(timeUnitOpt.getAsString(), lengthOpt.getAsLong()), "member.png"); 
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
        typeOption.addChoice("member", "member");

        OptionData timeUnitOption = new OptionData(OptionType.STRING, "time-unit", "What time unit to use", true);
        timeUnitOption.addChoice("Days", "Days");
        timeUnitOption.addChoice("Months", "Months");

        OptionData lengthOption = new OptionData(OptionType.INTEGER, "length", "How far back in time to look", true);
        lengthOption.addChoice("week", 7);
        lengthOption.addChoice("month", 30);
        lengthOption.addChoice("year", 365);


        return Commands.slash("chartgen", "Generate a chart")
                .addOptions(typeOption, timeUnitOption, lengthOption)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

}

