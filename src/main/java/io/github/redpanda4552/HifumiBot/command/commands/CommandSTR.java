/**
 * This file is part of HifumiBot, licensed under the MIT License (MIT)
 * 
 * Copyright (c) 2018 Brian Wood
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
package io.github.redpanda4552.HifumiBot.command.commands;

import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.CommandInterpreter;
import io.github.redpanda4552.HifumiBot.command.CommandMeta;
import io.github.redpanda4552.HifumiBot.util.EmbedUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;

public class CommandSTR extends AbstractCommand {

    private final String PASSMARK_STR = "https://www.cpubenchmark.net/singleThread.html";
    private final long REFRESH_PERIOD = 1000 * 60 * 60 * 4; // 4 hours
    
    private enum CPURating {
        OVERKILL("Overkill", 2800),
        GREAT("Great for most", 2400),
        GOOD("Good for most", 2000),
        MINIMUM_3D("Okay for 3D", 1600),
        MINIMUM_2D("Okay for 2D", 1200),
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
    
    private long lastUpdate = 0;
    private HashMap<String, String> ratingMap = new HashMap<String, String>();
    
    public CommandSTR() {
        super("str", CATEGORY_BUILTIN, false);
        update();
    }

    @Override
    protected void onExecute(CommandMeta cm) {
        long now = System.currentTimeMillis();
        
        if (now - lastUpdate > REFRESH_PERIOD) {
            if (update()) {
                lastUpdate = now;
            } else {
                HifumiBot.getSelf().sendMessage(cm.getChannel(), "Something went wrong when trying to reach Passmark... Going to try to use whatever info I already have!");
            }
        }
        
        // Search
        if (cm.getArgs().length == 0) {
            EmbedBuilder eb;
            
            if (cm.getMember() != null)
                eb = EmbedUtil.newFootedEmbedBuilder(cm.getMember());
            else 
                eb = EmbedUtil.newFootedEmbedBuilder(cm.getUser());
            
            eb.setTitle("About Single Thread Ratings (STR)");
            eb.appendDescription("**Single Thread Rating** (STR) is a benchmarking statistic used by Passmark's CPU benchmarking software. ")
              .appendDescription("The statistic indicates how powerful a single thread on a CPU is. ")
              .appendDescription("Though PCSX2 does have multiple threads, each thread still needs to be powerful in order to run emulation at full speed. ");
            eb.addField("Command Usage", "`" + CommandInterpreter.PREFIX + "str <cpu model here>`", false);
            HifumiBot.getSelf().sendMessage(cm.getChannel(), eb.build());
            return;
        }
        
        EmbedBuilder eb;
        
        if (cm.getMember() != null)
            eb = EmbedUtil.newFootedEmbedBuilder(cm.getMember());
        else
            eb = EmbedUtil.newFootedEmbedBuilder(cm.getUser());
        
        eb.setTitle("Comparing your search to " + ratingMap.size() + " CPUs...");
        eb.setColor(0xffff00);
        Message message = HifumiBot.getSelf().sendMessage(cm.getChannel(), new MessageBuilder().setEmbed(eb.build()).build());
        
        HashMap<String, Float> results = new HashMap<String, Float>();
        
        for (String cpuName : ratingMap.keySet()) {
            String normalized = cpuName.toLowerCase().trim();
            
            float toPush = 0;
            
            for (String arg : cm.getArgs()) {
                // Contains
                if (normalized.contains(arg.toLowerCase().trim())) {
                    toPush += 0.5;
                }
                
                // Whole word match
                for (String cpuPart : cpuName.replace("-", " ").split(" ")) {
                    if (cpuPart.equals(arg.toLowerCase().trim())) {
                        toPush += 1;
                    }
                }
            }
            
            if (toPush > 0)
                results.put(cpuName, toPush);
            
            sleep(1); // Just to give the raspi a break...
        }
        
        if (cm.getMember() != null)
            eb = EmbedUtil.newFootedEmbedBuilder(cm.getMember());
        else
            eb = EmbedUtil.newFootedEmbedBuilder(cm.getUser());
        
        if (results.size() > 0) {
            eb.setTitle("Query Results for \"" + StringUtils.join(cm.getArgs(), " ") + "\"");
            String highestName = null;
            float highestWeight = 0;
            
            while (!results.isEmpty() && eb.getFields().size() < 5) {
                for (String cpuName : results.keySet()) {
                    if (results.get(cpuName) > highestWeight) {
                        highestName = cpuName;
                        highestWeight = results.get(cpuName);
                    }
                }
                
                results.remove(highestName);
                highestWeight = 0;
                int highestScore = Integer.parseInt(ratingMap.get(highestName).replaceAll("[,. ]", ""));
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
        
        message.editMessage(eb.build()).complete();
    }

    @Override
    public String getHelpText() {
        return "Look up the Single Thread Rating for a CPU";
    }

    private boolean update() {
        try {
            Document doc = Jsoup.connect(PASSMARK_STR).maxBodySize(0).get();
            Elements charts = doc.getElementsByClass("chartlist");
            
            for (Element chart : charts) {
                Elements rows = chart.getElementsByTag("li");
                
                for (Element row : rows) {
                    String cpuName = row.getElementsByClass("prdname").get(0).text();
                    String rating = row.getElementsByClass("count").get(0).text();
                    ratingMap.put(cpuName, rating);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        
        return true;
    }
    
    private boolean sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            return false;
        }
        
        return true;
    }
}
