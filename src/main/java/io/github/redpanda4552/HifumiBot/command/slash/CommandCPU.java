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

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

import io.github.redpanda4552.HifumiBot.CpuIndex;
import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import io.github.redpanda4552.HifumiBot.permissions.PermissionLevel;
import io.github.redpanda4552.HifumiBot.util.SimpleSearch;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class CommandCPU extends AbstractSlashCommand {
    
    private static final int MAX_RESULTS = 5;

    private enum CPURating {
        OVER_9000("IT'S OVER 9000", 9000),
        WTF("What the fuck, why?", 4000),
        OVERKILL("Overkill", 2800), 
        GREAT("Great for most", 2400), 
        GOOD("Good for most", 2000),
        MINIMUM_3D("Okay for some 3D", 1600), 
        MINIMUM_2D("Okay for some 2D", 1200), 
        VERY_SLOW("Very Slow", 800),
        AWFUL("Awful", 0);

        private String displayName;
        private int minimum;

        private CPURating(String displayName, int minimum) {
            this.displayName = displayName;
            this.minimum = minimum;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getMinimum() {
            return minimum;
        }
    }

    public CommandCPU() {
        super(PermissionLevel.GUEST);
    }

    @Override
    protected void onExecute(SlashCommandEvent event) {
        boolean isEphemeral = true;
        
        if (event.getChannel().getId().equals(HifumiBot.getSelf().getConfig().channels.restrictedCommandChannelId) || HifumiBot.getSelf().getPermissionManager().hasPermission(PermissionLevel.MOD, event.getMember())) {
            isEphemeral = false;
        }
        
        EmbedBuilder eb = new EmbedBuilder();
        OptionMapping opt = event.getOption("name");
        
        if (opt == null) {
            event.reply("Missing required argument `name`").setEphemeral(isEphemeral).queue();
            return;
        }

        String name = opt.getAsString();
        CpuIndex cpuIndex = HifumiBot.getSelf().getCpuIndex();
        HashMap<String, Float> results = SimpleSearch.search(cpuIndex.getAllCpus(), StringUtils.join(name, " "));

        if (results.size() > 0) {
            eb.setAuthor("Passmark CPU Single Thread Performance", "https://www.cpubenchmark.net/singleThread.html");
            eb.setTitle("Search results for '" + StringUtils.join(name, " ").trim() + "'");
            eb.setDescription(":warning: Some games may have unusually high CPU requirements! If in doubt, ask!");
            String highestName = null;
            float highestWeight = 0;

            while (!results.isEmpty() && eb.getFields().size() < MAX_RESULTS) {
                for (String cpuName : results.keySet()) {
                    if (results.get(cpuName) > highestWeight) {
                        highestName = cpuName;
                        highestWeight = results.get(cpuName);
                    }
                }

                results.remove(highestName);
                highestWeight = 0;
                int highestScore = Integer.parseInt(cpuIndex.getCpuRating(highestName).replaceAll("[,. ]", ""));
                String highestScoreDescription = "";

                for (int i = 0; i < CPURating.values().length; i++) {
                    if (highestScore >= CPURating.values()[i].getMinimum()) {
                        highestScoreDescription = CPURating.values()[i].getDisplayName();
                        break;
                    }
                }

                eb.addField(highestName, highestScore + " (" + highestScoreDescription + ")", false);
            }

            eb.setColor(0x00ff00);
        } else {
            eb.setTitle("No results matched your query!");
            eb.setColor(0xff0000);
        }

        event.replyEmbeds(eb.build()).queue();
    }

    @Override
    protected CommandData defineSlashCommand() {
        return new CommandData("cpu", "Look up the single thread rating of a CPU")
                .addOption(OptionType.STRING, "name", "Name of the CPU to look up", true);
    }
}
