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
package io.github.redpanda4552.HifumiBot.command.commands;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.CommandMeta;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;

public class CommandWelcome extends AbstractCommand {

    private final MessageEmbed usage;
    
    public CommandWelcome() {
        super("welcome", CATEGORY_BUILTIN, true);
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Welcome Usage");
        eb.addField("Send the welcome message to yourself", "`welcome me`", false);
        eb.addField("Set the welcome message title", "`welcome title <title>`", false);
        eb.addField("Set the welcome message body", "`welcome body <body>`", false);
        eb.addField("Add a field", "`welcome addfield <title> <body> <inline>`", false);
        eb.addField("Remove a field", "`welcome rmfield <title>`", false);
        usage = eb.build();
    }

    @Override
    protected void onExecute(CommandMeta cm) {
        if (cm.getArgs().length < 1) {
            HifumiBot.getSelf().sendMessage(cm.getChannel(), usage);
            return;
        }
        
        String subCommand = cm.getArgs()[0];
        
        switch (subCommand.toLowerCase()) {
        case "me":
            if (!HifumiBot.getSelf().getNewMemberMessageController().isEnabled()) {
                HifumiBot.getSelf().sendMessage(cm.getChannel(), "Welcome message is not set up yet!");
                return;
            }
            
            HifumiBot.getSelf().getNewMemberMessageController().sendMessage(cm.getUser());
            break;
        case "title":
            if (cm.getArgs().length == 2) {
                HifumiBot.getSelf().getNewMemberMessageController().getNewMemberMessage().setTitle(cm.getArgs()[1]);
            } else {
                HifumiBot.getSelf().sendMessage(cm.getChannel(), usage);
            }
            
            break;
        case "body":
            if (cm.getArgs().length == 2) {
                HifumiBot.getSelf().getNewMemberMessageController().getNewMemberMessage().setBody(cm.getArgs()[1]);
            } else {
                HifumiBot.getSelf().sendMessage(cm.getChannel(), usage);
            }
            
            break;
        case "addfield":
            if (cm.getArgs().length == 4) {
                Field field = new Field(cm.getArgs()[1], cm.getArgs()[2], Boolean.parseBoolean(cm.getArgs()[3]));
                HifumiBot.getSelf().getNewMemberMessageController().getNewMemberMessage().addField(field);
            } else {
                HifumiBot.getSelf().sendMessage(cm.getChannel(), usage);
            }
            
            break;
        case "rmfield":
            if (cm.getArgs().length == 2) {
                Field toRemove = null;
                
                for (Field field : HifumiBot.getSelf().getNewMemberMessageController().getNewMemberMessage().getFields()) {
                    if (field.getName().equals(cm.getArgs()[1])) {
                        toRemove = field;
                    }
                }
                
                if (toRemove != null) {
                    HifumiBot.getSelf().getNewMemberMessageController().getNewMemberMessage().removeField(toRemove);
                }
            } else {
                HifumiBot.getSelf().sendMessage(cm.getChannel(), usage);
            }
            
            break;
        }
        
        HifumiBot.getSelf().getNewMemberMessageController().saveConfig();
    }

    @Override
    public String getHelpText() {
        return "Manage automatic welcome message";
    }
}
