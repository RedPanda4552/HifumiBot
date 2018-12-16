/**
 * This file is part of HifumiBot, licensed under the MIT License (MIT)
 * 
 * Copyright (c) 2017 Brian Wood
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

import java.util.ArrayList;
import java.util.TreeSet;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;

public class CommandHelp extends AbstractCommand {

    private final int COMMANDS_PER_PAGE = 10;
    
    private ArrayList<MessageEmbed> helpPages;
    private int pageCount = 0;
    
    public CommandHelp(HifumiBot hifumiBot) {
        super(hifumiBot, false);
    }

    @Override
    protected void onExecute(MessageChannel channel, Member sender, String[] args) {
        int pageNumber = 1;
        
        if (args.length >= 1) {
            try {
                pageNumber = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) { }
        }
        
        if (pageNumber >= helpPages.size())
            pageNumber = helpPages.size() - 1;
        
        if (pageNumber < 1)
            pageNumber = 1;
        
        if (channel instanceof TextChannel) {
            hifumiBot.sendMessage(sender.getUser().openPrivateChannel().complete(), helpPages.get(pageNumber - 1));
        } else {
            hifumiBot.sendMessage(channel, helpPages.get(pageNumber - 1));
        }
    }
    
    @Override
    protected String getHelpText() {
        return "Display this help dialog";
    }
    
    public void rebuildHelpPages() {
        helpPages = new ArrayList<MessageEmbed>();
        TreeSet<String> commandNames = hifumiBot.getCommandInterpreter().getCommandNames();
        pageCount = (int) Math.ceil((double) commandNames.size() / COMMANDS_PER_PAGE);
        EmbedBuilder eb = new EmbedBuilder();
        
        for (String commandName : commandNames) {
            AbstractCommand command = hifumiBot.getCommandInterpreter().getCommandMap().get(commandName);
            
            eb.addField(">" + commandName, (command instanceof DynamicCommand ? " [DynCmd]" : "") + command.getHelpText(), false);
            
            if (eb.getFields().size() >= COMMANDS_PER_PAGE) {
                addToPages(eb);
                eb = new EmbedBuilder();
            }
        }
        
        if (eb.getFields().size() > 0)
            addToPages(eb);
    }
    
    private void addToPages(EmbedBuilder eb) {
        eb.setTitle("HifumiBot Help Page " + (helpPages.size() + 1) + " / " + pageCount);
        eb.setDescription("The prefix for all commands is \">\".\nA [DynCmd] tag in a command description means it is a custom command built by a server admin.");
        helpPages.add(eb.build());
    }
}
