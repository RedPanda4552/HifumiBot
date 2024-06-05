package io.github.redpanda4552.HifumiBot.command.slash;

import io.github.redpanda4552.HifumiBot.charting.AbstractChartGenerator;
import io.github.redpanda4552.HifumiBot.charting.WarezChartGenerator;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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

        AbstractChartGenerator.ChartType chartType = AbstractChartGenerator.ChartType.valueOf(typeOpt.getAsString());
        
        switch (chartType) {
            case AbstractChartGenerator.ChartType.WAREZ: {
                WarezChartGenerator generator = new WarezChartGenerator();
                FileUpload file = FileUpload.fromData(generator.build(), "warez.png");
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

        for (AbstractChartGenerator.ChartType chartType : AbstractChartGenerator.ChartType.values()) {
            typeOption.addChoice(chartType.toString().toLowerCase(), chartType.toString());
        }

        return Commands.slash("chartgen", "Generate a chart")
                .addOptions(typeOption);
    }

}

