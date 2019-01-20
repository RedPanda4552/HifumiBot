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

import io.github.redpanda4552.HifumiBot.HifumiBot;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class DynamicCommand extends AbstractCommand {

    private String helpText, title, body, imageUrl;
    
    public DynamicCommand(HifumiBot hifumiBot, boolean admin, String helpText, String title, String body, String imageUrl) {
        super(hifumiBot, admin);
        this.helpText = helpText;
        this.title = title;
        this.body = body;
        this.imageUrl = imageUrl;
    }

    @Override
    protected void onExecute(MessageChannel channel, Member senderMember, User senderUser, String[] args) {
        EmbedBuilder eb;
        
        if (senderMember != null) {
            eb = this.newFootedEmbedBuilder(senderMember);
        } else {
            eb = this.newFootedEmbedBuilder(senderUser);
        }
        
        eb.setTitle(title);
        eb.setDescription(body);
        eb.setImage(imageUrl);
        hifumiBot.sendMessage(channel, eb.build());
    }
    
    @Override
    public String getHelpText() {
        return helpText;
    }
}
