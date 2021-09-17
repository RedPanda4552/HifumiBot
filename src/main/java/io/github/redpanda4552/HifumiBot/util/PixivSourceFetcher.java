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
package io.github.redpanda4552.HifumiBot.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;

public class PixivSourceFetcher {

    private static final String PIXIV_BASE_URL = "https://www.pixiv.net/artworks/";
    private static final String PIXIV_PATTERN = "([0-9]+)";
    private static final Pattern p = Pattern.compile(PIXIV_PATTERN);
    
    public static void getPixivLink(Message message) {
        if (!message.getChannel().getId().equals(HifumiBot.getSelf().getConfig().channels.pixivChannelId)) {
            return;
        }
        
        MessageBuilder mb = new MessageBuilder();
        
        for (Attachment attach : message.getAttachments()) {
            Matcher m = p.matcher(attach.getFileName());
            
            while (m.find()) {
                try {
                    URL url = new URL(PIXIV_BASE_URL + m.group());
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();
                    
                    if (connection.getResponseCode() == 200) {
                        mb.append("<" + PIXIV_BASE_URL + m.group() + ">\n");
                        break;
                    }
                } catch (IOException e) { }
            }
        }
        
        if (!mb.isEmpty()) {
            Messaging.sendMessage(message.getChannel(), mb.build());
        }
    }
}
