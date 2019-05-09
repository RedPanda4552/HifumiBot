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
package io.github.redpanda4552.HifumiBot.command;

import java.io.IOException;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.util.CommandMeta;
import net.dv8tion.jda.core.EmbedBuilder;

public class CommandSTR extends AbstractCommand {

    private final String PASSMARK_STR = "https://www.cpubenchmark.net/singleThread.html";
    private final long REFRESH_PERIOD = 1000 * 60 * 60 * 4; // 4 hours
    private final long IO_WAIT_MS = 1000;
    
    private long lastUpdate = 0;
    private HashMap<String, String> ratingMap;
    private int ioWaitMultiplier = 1;
    
    public CommandSTR(HifumiBot hifumiBot) {
        super(hifumiBot, false, "builtin");
        update();
    }

    @Override
    protected void onExecute(CommandMeta cm) {
        long now = System.currentTimeMillis();
        
        if (now - lastUpdate > REFRESH_PERIOD) {
            update();
        }
        
        // Search
        if (cm.getArgs().length == 0) {
            hifumiBot.sendMessage(cm.getChannel(), "I can't search for nothing! Try `!str <cpu model here>`");
            return;
        }
        
        hifumiBot.sendMessage(cm.getChannel(), "Comparing your search to " + ratingMap.size() + " CPUs...");
        
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
            
            toPush -= 0.1f * Math.abs(cpuName.replace("-", " ").split(" ").length - cm.getArgs().length);
            
            if (toPush > 0)
                results.put(cpuName, toPush);
            
            sleep(1); // Just to give the raspi a break...
        }
        
        EmbedBuilder eb;
        
        if (cm.getMember() != null)
            eb = newFootedEmbedBuilder(cm.getMember());
        else
            eb = newFootedEmbedBuilder(cm.getUser());
        
        if (results.size() > 0) {
            eb.setTitle("Query Results for \"" + stringify(cm.getArgs()) + "\"");
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
                eb.addField(highestName, String.valueOf(ratingMap.get(highestName)), false);
            }
        } else {
            eb.setTitle("No results matched your query!");
            eb.setColor(0xff0000);
        }
        
        hifumiBot.sendMessage(cm.getChannel(), eb.build());
    }

    @Override
    protected String getHelpText() {
        return "Look up the Single Thread Rating for a CPU";
    }

    private void update() {
        ratingMap = new HashMap<String, String>();
        
        try {
            Document doc = Jsoup.connect(PASSMARK_STR).get();
            Elements rows = doc.getElementById("mark").getElementsByTag("tr");
            
            for (Element row : rows) {
                Elements columns = row.getElementsByTag("td");
                
                if (columns.size() < 3)
                    continue;
                
                String cpuName = columns.get(0).text();
                String rating = columns.get(1).text();
                ratingMap.put(cpuName, rating);
            }
            
            ioWaitMultiplier = 1;
        } catch (IOException e) {
            sleep(IO_WAIT_MS * ioWaitMultiplier);
            ioWaitMultiplier *= 2;
        }
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
