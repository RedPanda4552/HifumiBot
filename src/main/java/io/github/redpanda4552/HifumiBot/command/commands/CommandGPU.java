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

public class CommandGPU extends AbstractCommand {

    private final String PASSMARK_HIGH_END = "https://www.videocardbenchmark.net/high_end_gpus.html";
    private final String PASSMARK_MID_HIGH = "https://www.videocardbenchmark.net/mid_range_gpus.html";
    private final String PASSMARK_MID_LOW = "https://www.videocardbenchmark.net/midlow_range_gpus.html";
    private final String PASSMARK_LOW_END = "https://www.videocardbenchmark.net/low_end_gpus.html";
    private final long REFRESH_PERIOD = 1000 * 60 * 60 * 4; // 4 hours
    
    private enum GPURating {
        x8NATIVE("8x Native (~5K)", 13030),
        x6NATIVE("6x Native (~4K)", 8660),
        x5NATIVE("5x Native (~3K)", 6700),
        x4NATIVE("4x Native (~2K)", 4890),
        x3NATIVE("3x Native (~1080p)", 3230),
        x2NATIVE("2x Native (~720p)", 1720),
        NATIVE("Native", 360),
        SLOW("Slow", 0);
        
        private String displayName;
        private int minimum;
        
        private GPURating(String displayName, int minimum) {
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
    
    public CommandGPU() {
        super("gpu", CATEGORY_BUILTIN, false);
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
            
            eb.setTitle("About GPU Ratings");
            eb.appendDescription("Passmark's GPU benchmarking software measures overall performance of GPUs. ")
              .appendDescription("Higher upscaling quality in PCSX2 will require increasingly powerful GPUs, ")
              .appendDescription("so this tool will help you determine what Internal Resolution a GPU is capable of. ")
              .appendDescription("*These ratings should only be used as a rough guide; **some games are unusually demanding ")
              .appendDescription("on the GPU and will still have performance problems.***");
            eb.addField("High End GPUs", "https://www.videocardbenchmark.net/high_end_gpus.html", false);
            eb.addField("Mid-High GPUs", "https://www.videocardbenchmark.net/mid_range_gpus.html", false);
            eb.addField("Mid-Low GPUs", "https://www.videocardbenchmark.net/midlow_range_gpus.html", false);
            eb.addField("Low End GPUs", "https://www.videocardbenchmark.net/low_end_gpus.html", false);
            eb.addField("Command Usage", "`" + CommandInterpreter.PREFIX + "gpu <gpu model here>`", false);
            HifumiBot.getSelf().sendMessage(cm.getChannel(), eb.build());
            return;
        }
        
        EmbedBuilder eb;
        
        if (cm.getMember() != null)
            eb = EmbedUtil.newFootedEmbedBuilder(cm.getMember());
        else
            eb = EmbedUtil.newFootedEmbedBuilder(cm.getUser());
        
        eb.setTitle("Comparing your search to " + ratingMap.size() + " GPUs...");
        eb.setColor(0xffff00);
        Message message = HifumiBot.getSelf().sendMessage(cm.getChannel(), new MessageBuilder().setEmbed(eb.build()).build());
        
        HashMap<String, Float> results = new HashMap<String, Float>();
        
        for (String gpuName : ratingMap.keySet()) {
            String normalized = gpuName.toLowerCase().trim();
            
            float toPush = 0;
            
            for (String arg : cm.getArgs()) {
                // Contains
                if (normalized.contains(arg.toLowerCase().trim())) {
                    toPush += 0.5;
                }
                
                // Whole word match
                for (String gpuPart : normalized.replace("-", " ").split(" ")) {
                    if (gpuPart.equals(arg.toLowerCase().trim())) {
                        toPush += 1;
                    }
                }
            }
            
            if (toPush > 0)
                results.put(gpuName, toPush);
            
            sleep(1); // Just to give the raspi a break...
        }
        
        if (cm.getMember() != null)
            eb = EmbedUtil.newFootedEmbedBuilder(cm.getMember());
        else
            eb = EmbedUtil.newFootedEmbedBuilder(cm.getUser());
        
        if (results.size() > 0) {
            eb.setTitle("Query Results for \"" + StringUtils.join(cm.getArgs(), " ") + "\"");
            eb.setDescription(":warning: This feature is in BETA! Please do not take these results as absolute!");
            String highestName = null;
            float highestWeight = 0;
            
            while (!results.isEmpty() && eb.getFields().size() < 5) {
                for (String gpuName : results.keySet()) {
                    if (results.get(gpuName) > highestWeight) {
                        highestName = gpuName;
                        highestWeight = results.get(gpuName);
                    }
                }
                
                results.remove(highestName);
                highestWeight = 0;
                int highestScore = -1;
                try {
                    highestScore = Integer.parseInt(ratingMap.get(highestName).replaceAll("[,. ]", ""));
                } catch (NumberFormatException e) { }
                String highestScoreDescription = "";
                
                for (int i = 0; i < GPURating.values().length; i++) {
                    if (highestScore >= GPURating.values()[i].getMinimum()) {
                        highestScoreDescription = GPURating.values()[i].getDisplayName();
                        break;
                    }
                }
                
                eb.addField(highestName, highestScore + " - " + highestScoreDescription, false);
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
        return update(PASSMARK_HIGH_END) && update(PASSMARK_MID_HIGH) && update(PASSMARK_MID_LOW) && update(PASSMARK_LOW_END);
    }
    
    private boolean update(String url) {
        try {
            Document doc = Jsoup.connect(url).maxBodySize(0).get();
            Element mark = doc.getElementById("mark");
            Elements charts = mark.getElementsByClass("chartlist");
            
            for (Element chart : charts) {
                Elements rows = chart.getElementsByTag("li");
                
                for (Element row : rows) {
                    String gpuName = row.getElementsByClass("prdname").get(0).text();
                    String rating = row.getElementsByClass("count").get(0).text();
                    ratingMap.put(gpuName, rating);
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
