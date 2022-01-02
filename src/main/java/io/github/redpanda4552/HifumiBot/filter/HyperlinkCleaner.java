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
package io.github.redpanda4552.HifumiBot.filter;

import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.util.DNSQueryResult;
import io.github.redpanda4552.HifumiBot.util.Internet;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.entities.Message;

public class HyperlinkCleaner implements Runnable {
    private static final Pattern URL_PATTERN = Pattern
            .compile("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");

    private final Message message;
    private final Instant instant;

    public HyperlinkCleaner(Message message, Instant instant) {
        this.message = message;
        this.instant = instant;
    }

    @Override
    public void run() {
        Matcher m = URL_PATTERN.matcher(message.getContentDisplay().toLowerCase());

        while (m.find()) {
            if (Internet.nslookup(m.group()) == DNSQueryResult.BLOCKED) {
                message.delete().complete();
                Messaging.logInfo("HyperlinkCleaner", "run", "Deleting message from user "
                        + message.getAuthor().getAsMention()
                        + " in channel <#"
                        + message.getChannel().getId()
                        + ">; DNS query on a URL inside failed and may be malicious.\n\nUser's message (formatting stripped):\n```\n"
                        + message.getContentStripped() + "\n```");
                
                if (instant != null) {
                    HifumiBot.getSelf().getKickHandler().storeIncident(message.getMember(), instant);
                }
                
                return;
            }
        }
    }

}
