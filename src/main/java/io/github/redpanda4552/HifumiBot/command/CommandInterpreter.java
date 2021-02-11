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

import java.util.Collections;

import org.apache.commons.lang3.ArrayUtils;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.commands.AbstractCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandInterpreter extends ListenerAdapter {

    public static final String PREFIX = ">";
    
    private HifumiBot hifumiBot;
    
    /**
     * @param hifumiBot - Required because we are instancing commands before
     * HifumiBot.self has been assigned.
     */
    public CommandInterpreter(HifumiBot hifumiBot) {
        this.hifumiBot = hifumiBot;
    }
    
    public void execute(MessageReceivedEvent event) {
        if (event.getAuthor().getId().equals(hifumiBot.getJDA().getSelfUser().getId()))
            return;
        
        Message message = event.getMessage();
        String[] args = message.getContentDisplay().split(" ");
        String command = args[0].toLowerCase();
        args = ArrayUtils.remove(args, 0);
        AbstractCommand toExecute;
        
        if (!command.startsWith(PREFIX))
            return;
        
        command = command.replaceFirst(PREFIX, "");
        
        if ((toExecute = hifumiBot.getCommandIndex().getCommand(command)) != null) {
            CommandMeta cm = new CommandMeta(
                    command, 
                    toExecute.isAdminCommand(), 
                    toExecute.getCategory(), 
                    event.getChannel() instanceof TextChannel ? event.getGuild() : null, 
                    event.getChannel(), 
                    event.getMember(), 
                    event.getAuthor(), 
                    message, 
                    event.getChannel() instanceof TextChannel ? message.getMentionedMembers() : Collections.emptyList(), 
                    args
            );
            toExecute.run(cm);
        }
    }
}
