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
package io.github.redpanda4552.HifumiBot.command.context;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.AbstractMessageContextCommand;
import io.github.redpanda4552.HifumiBot.util.EmbedUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class CommandTranslateEN extends AbstractMessageContextCommand {

    @Override
    protected void onExecute(MessageContextInteractionEvent event) {
        event.deferReply(true).queue();
        String userId = event.getTarget().getAuthor().getId();
        String content = event.getTarget().getContentDisplay();
        event.getHook().editOriginal(":hourglass: Sending a translation request to Chat GPT... This may take a moment...").queue();
        String translated = HifumiBot.getSelf().getChatGPT().translate(userId, content);

        if (translated != null) {
            EmbedBuilder eb = EmbedUtil.newFootedEmbedBuilder(event.getMember());
            eb.setTitle("Chat GPT Translation");
            eb.setDescription(translated);
            event.getTarget().replyEmbeds(eb.build()).mentionRepliedUser(false).queue();

            try {
                event.getHook().deleteOriginal().queue();    
            } catch (Exception e) {
                // Squelch
            }
            
        } else {
            event.getHook().editOriginal("Either an error occurred, or you are being rate limited. Please try again in a few minutes.").queue();
        }
    }

    @Override
    protected CommandData defineMessageContextCommand() {
        return Commands.message("translate-en")
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED);
    }
}
