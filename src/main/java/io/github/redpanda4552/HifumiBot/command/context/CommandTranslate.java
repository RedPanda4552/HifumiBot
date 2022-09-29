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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import io.github.redpanda4552.HifumiBot.command.AbstractMessageContextCommand;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class CommandTranslate extends AbstractMessageContextCommand {

    private static final String URL_FORMAT = "https://translate.google.com/?sl=auto&tl=en&text=%s&op=translate";
    
    @Override
    protected void onExecute(MessageContextInteractionEvent event) {
        event.deferReply(true).queue();
        String content = event.getTarget().getContentDisplay();
        
        try {
            String encoded = URLEncoder.encode(content, "UTF-8");
            String url = String.format(URL_FORMAT, encoded);
            event.getHook().sendMessage(url).queue();
        } catch (UnsupportedEncodingException e) {
            event.getHook().sendMessage("Could not prepare a Google Translate link for this message, perhaps there are some weird characters in it?").queue();
        }
    }

    @Override
    protected CommandData defineMessageContextCommand() {
        return Commands.message("translate")
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED);
    }
}
