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
package io.github.redpanda4552.HifumiBot.command;

import io.github.redpanda4552.HifumiBot.util.EmbedUtil;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class DynamicCommand {

    protected String name, category, helpText, title, body, imageURL;

    public DynamicCommand(String name, String category, String helpText, String title, String body, String imageURL) {
        this.name = name;
        this.category = category;
        this.helpText = helpText;
        this.title = title;
        this.body = body;
        this.imageURL = imageURL;
    }

    public void execute(CommandMeta cm) {
        EmbedBuilder eb;

        if (cm.getMember() != null) {
            eb = EmbedUtil.newFootedEmbedBuilder(cm.getMember());
        } else {
            eb = EmbedUtil.newFootedEmbedBuilder(cm.getUser());
        }

        eb.setTitle(title);
        eb.setDescription(body);
        eb.setImage(imageURL);
        Messaging.sendMessageEmbed(cm.getChannel(), eb.build());
    }
    
    public void execute(SlashCommandEvent event) {
        EmbedBuilder eb = EmbedUtil.newFootedEmbedBuilder(event.getMember());
        eb.setTitle(title);
        eb.setDescription(body);
        eb.setImage(imageURL);
        event.replyEmbeds(eb.build()).queue();
    }
    
    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getImageURL() {
        return this.imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
