/**
 * This file is part of HifumiBot, licensed under the MIT License (MIT)
 * 
 * Copyright (c) 2017 Brian Wood
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
package io.github.redpanda4552.HifumiBot;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.TextChannel;

public class BuildMonitor implements Runnable {

    private static final long SCRAPE_GAP_MS = 1000 * 60 * 15, SLEEP_TIME_MS = 1000 * 60;
    private static final String ORPHIS_PCSX2_ROOT = "https://buildbot.orphis.net/pcsx2/";
    
    private TextChannel outputChannel;
    private boolean debug = false;
    private String gitRevision = "";
    
    public BuildMonitor(TextChannel outputChannel, boolean debug) {
        this.outputChannel = outputChannel;
        this.debug = debug;
    }
    
    public void run() {
        try {
            while (!Thread.interrupted()) {
                MessageHistory channelHistory = outputChannel.getHistory();
                Message lastPostedMessage;
                
                do {
                    lastPostedMessage = channelHistory.retrievePast(1).complete().get(0);
                } while (lastPostedMessage.getEmbeds().size() == 0);
                
                MessageEmbed lastPostedEmbed = lastPostedMessage.getEmbeds().get(0);
                String lastPostedRevision = "";
                
                // Look for the revision field in the latest embed
                for (Field field : lastPostedEmbed.getFields()) {
                    if (field.getName().equals("Revision:")) {
                        lastPostedRevision = field.getValue();
                        break;
                    }
                }
                
                // Get the Orphis page
                Document buildBotPage = Jsoup.connect(ORPHIS_PCSX2_ROOT).get();
                Element table = buildBotPage.getElementsByClass("listing").get(0);
                Elements rows = table.getElementsByTag("tr");
                
                scrape:
                for (Element row : rows) {
                    Elements cells = row.getElementsByTag("td");
                    gitRevision = "";
                    
                    for (Element cell : cells) {
                        Elements anchors = cell.getElementsByTag("a");
                        
                        if (cell == cells.get(0)) {
                            gitRevision = anchors.get(0).ownText();
                        }
                        
                        if (cell == cells.get(3)) {
                            if (anchors.isEmpty()) {
                                break; // No build, skip it and try next row.
                            } else {
                                if (gitRevision.equals(lastPostedRevision)) {
                                    break scrape; // Up to date, stop trying.
                                } else {
                                    lastPostedRevision = gitRevision;
                                }
                                
                                EmbedBuilder eb = new EmbedBuilder();
                                eb.setAuthor("New PCSX2 Development Build Available!");
                                eb.addField("Revision:", gitRevision, false);
                                eb.addField("Download and view changes:", ORPHIS_PCSX2_ROOT, false);
                                eb.setColor(outputChannel.getGuild().getMember(HifumiBot.getSelf().getJDA().getSelfUser()).getColor());
                                
                                if (outputChannel != null) {
                                    outputChannel.sendMessage(eb.build()).complete();
                                }
                                
                                break scrape;
                            }
                        }
                    }
                }
                
                if (!sleep())
                    return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private boolean sleep() {
        long next = System.currentTimeMillis() + SCRAPE_GAP_MS, diff = 0;
        
        while ((diff = next - System.currentTimeMillis()) > 0) {
            String[] parts = gitRevision.split("-");
            String time = String.valueOf(diff / 1000 / 60) + "m";
            StringBuilder sb = new StringBuilder(parts[0]);
            sb.append(" / ")
              .append(parts[2])
              .append(" / ")
              .append(time);
            
            if (debug)
                sb.append(" / dbg");
              
            HifumiBot.getSelf().getJDA().getPresence().setGame(Game.watching(sb.toString()));
            
            try {
                Thread.sleep(SLEEP_TIME_MS);
            } catch (InterruptedException e) {
                System.out.println("BuildMonitor Interrupt!");
                return false;
            }
        }
        
        return true;
    }
}
