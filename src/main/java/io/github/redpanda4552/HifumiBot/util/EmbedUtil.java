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

import org.apache.commons.lang3.StringUtils;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.User;

public class EmbedUtil {

    public static Field prebuildField(String title, String value, boolean inline) {
        if (title.length() > MessageEmbed.TITLE_MAX_LENGTH) {
            title = StringUtils.truncate(title, MessageEmbed.TITLE_MAX_LENGTH);
        }

        if (value.length() > MessageEmbed.VALUE_MAX_LENGTH) {
            value = StringUtils.truncate(value, MessageEmbed.VALUE_MAX_LENGTH);
        }

        return new Field(title, value, inline);
    }

    public static EmbedBuilder newFootedEmbedBuilderForFilters() {
        return newFootedEmbedBuilder(null, HifumiBot.getSelf().getJDA().getSelfUser().getAvatarUrl());
    }

    public static EmbedBuilder newFootedEmbedBuilder(Message msg) {
        if (msg.getMember() != null) {
            return EmbedUtil.newFootedEmbedBuilder(msg.getMember());
        } else {
            return EmbedUtil.newFootedEmbedBuilder(msg.getAuthor());
        }
    }

    public static EmbedBuilder newFootedEmbedBuilder(Member sender) {
        return newFootedEmbedBuilder(sender.getEffectiveName(), sender.getUser().getEffectiveAvatarUrl());
    }

    public static EmbedBuilder newFootedEmbedBuilder(User sender) {
        return newFootedEmbedBuilder(sender.getName(), sender.getEffectiveAvatarUrl());
    }

    private static EmbedBuilder newFootedEmbedBuilder(String displayName, String avatarUrl) {
        EmbedBuilder eb = new EmbedBuilder();
        StringBuilder sb = new StringBuilder(
                "This message was automatically generated by " + HifumiBot.getSelf().getJDA().getSelfUser().getName());

        if (displayName != null) {
            sb.append(", at the instruction of ");
            sb.append(displayName);
        }

        sb.append(".");
        return eb.setFooter(sb.toString(), avatarUrl);
    }
}
