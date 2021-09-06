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
package io.github.redpanda4552.HifumiBot;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import io.github.redpanda4552.HifumiBot.util.Messaging;
import io.github.redpanda4552.HifumiBot.util.Refreshable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;

public class BuildMonitor implements Refreshable {

    private static final String ORPHIS_PCSX2_ROOT = "https://buildbot.orphis.net/pcsx2/";
    private static final String WINDOWS_INSTRUCTIONS = "- Download the topmost (latest) build from " + ORPHIS_PCSX2_ROOT
            + ".\n- Extract the .7z file to somewhere like Documents or Desktop.\n- DO NOT extract it to Program Files; this folder is write protected by Windows and you won't be able to save any configuration settings.\n- Upgrading? We strongly recommend AGAINST dropping the new files over top of the old. It is better to keep the new version separate, and then move your BIOS and memory card files over to the new version.";
    private static final String LINUX_INSTRUCTIONS = "First time installation:\n```\nsudo dpkg --add-architecture i386\nsudo apt-add-repository ppa:pcsx2-team/pcsx2-daily\nsudo apt update\nsudo apt install pcsx2-unstable\n```\nAlready installed?\n```\nsudo apt update\nsudo apt upgrade\n```";

    private TextChannel outputChannel;
    private String gitRevision = "";

    public BuildMonitor(TextChannel outputChannel) {
        this.outputChannel = outputChannel;
        this.refresh();
    }

    @Override
    public synchronized void refresh() {
        try {
            MessageHistory channelHistory = outputChannel.getHistory();
            Message lastPostedMessage = null;
            MessageEmbed lastPostedEmbed = null;
            String lastPostedRevision = null;

            do {
                List<Message> historyMessages = channelHistory.retrievePast(1).complete();

                if (historyMessages.isEmpty()) {
                    break;
                }

                lastPostedMessage = historyMessages.get(0);
            } while (lastPostedMessage.getEmbeds().size() == 0);

            if (lastPostedMessage != null) {
                lastPostedEmbed = lastPostedMessage.getEmbeds().get(0);
            }

            if (lastPostedEmbed != null) {
                // Look for the revision field in the latest embed
                for (Field field : lastPostedEmbed.getFields()) {
                    if (field.getName().equals("Revision:")) {
                        lastPostedRevision = field.getValue();
                        break;
                    }
                }
            }

            Document buildBotPage = Jsoup.connect(ORPHIS_PCSX2_ROOT).get(); // Get the entire Orphis page
            Element table = buildBotPage.getElementsByClass("listing").get(0); // Get the table
            Element row = table.getElementsByTag("tr").get(1); // Get first row
            Element revisionCell = row.getElementsByTag("td").get(0); // Get first cell
            gitRevision = revisionCell.getElementsByTag("a").get(0).ownText(); // Get display text
            Element commitCell = row.getElementsByTag("td").get(4); // Get last cell

            if (lastPostedRevision == null || !gitRevision.equals(lastPostedRevision)) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setAuthor("New PCSX2 Development Build Available!");
                eb.addField("Revision:", gitRevision, false);
                eb.addField("Commit:", commitCell.ownText().substring(0, 512), false);
                eb.addField("Windows:", WINDOWS_INSTRUCTIONS, false);
                eb.addField("Ubuntu:", LINUX_INSTRUCTIONS, false);
                eb.addField("Linux (Any)",
                        "A guide to compile from source can be found on GitHub:\nhttps://github.com/PCSX2/pcsx2/wiki/Installing-on-Linux",
                        false);
                eb.setColor(outputChannel.getGuild().getMember(HifumiBot.getSelf().getJDA().getSelfUser()).getColor());

                if (outputChannel != null) {
                    Messaging.sendMessageEmbed(outputChannel, eb.build());
                }
            }
        } catch (Exception e) {
            Messaging.logException("BuildMonitor", "refresh", e);
        }
    }
}
