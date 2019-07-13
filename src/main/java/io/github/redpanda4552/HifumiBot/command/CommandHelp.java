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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import io.github.redpanda4552.HifumiBot.CommandInterpreter;
import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.util.CommandMeta;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;

public class CommandHelp extends AbstractCommand {

    private final int COMMANDS_PER_PAGE = 10;
    
    private MessageEmbed helpRoot;
    private HashMap<String, ArrayList<MessageEmbed>> helpPages;
    
    public CommandHelp(HifumiBot hifumiBot) {
        super(hifumiBot, false, CATEGORY_BUILTIN);
    }

    @Override
    protected void onExecute(CommandMeta cm) {
        String category = "builtin";
        int pageNumber = 1;
        MessageEmbed toSend = null;
        
        if (cm.getArgs().length >= 1 && helpPages.get(cm.getArgs()[0]) != null) {
            category = cm.getArgs()[0];
            
            if (cm.getArgs().length >= 2) {
                try {
                    pageNumber = Integer.parseInt(cm.getArgs()[1]);
                } catch (NumberFormatException e) { }
            }
            
            if (pageNumber > helpPages.get(category).size())
                pageNumber = helpPages.get(category).size() - 1;
            
            if (pageNumber < 1)
                pageNumber = 1;
            
            toSend = helpPages.get(category).get(pageNumber - 1);
        } else {
            toSend = helpRoot;
        }
        
        hifumiBot.sendMessage(cm.getUser().openPrivateChannel().complete(), toSend);
    }
    
    @Override
    protected String getHelpText() {
        return "Display this help dialog";
    }
    
    /**
     * Fully rebuilds the help page lists.
     */
    public void rebuildHelpPages() {
        helpPages = new HashMap<String, ArrayList<MessageEmbed>>();
        HashMap<String, TreeSet<String>> commandMap = hifumiBot.getCommandInterpreter().getCategorizedCommandNames();
        
        for (String category : commandMap.keySet()) {
            int pageCount = (int) Math.ceil((double) commandMap.get(category).size() / COMMANDS_PER_PAGE);
            helpPages.put(category, new ArrayList<MessageEmbed>());
            EmbedBuilder eb = new EmbedBuilder();
            
            for (String command : commandMap.get(category)) {
                eb.addField(">" + command, hifumiBot.getCommandInterpreter().getCommandMap().get(command).getHelpText(), false);
                
                if (eb.getFields().size() >= COMMANDS_PER_PAGE) {
                    addToPages(category, eb, pageCount);
                    eb = new EmbedBuilder();
                }
            }
            
            if (eb.getFields().size() > 0)
                addToPages(category, eb, pageCount);
        }
        
        EmbedBuilder helpRootBuilder = new EmbedBuilder();
        helpRootBuilder.setTitle("HifumiBot Help");
        helpRootBuilder.setDescription("The prefix for all commands is \"" + CommandInterpreter.PREFIX + "\".\nTo view available commands use `" + CommandInterpreter.PREFIX + "help <category> [page]`");
        StringBuilder sb = new StringBuilder();
        
        for (String category : commandMap.keySet())
            sb.append(category).append("\n");
        
        helpRootBuilder.addField("Available Categories", sb.toString(), false);
        helpRoot = helpRootBuilder.build();
    }
    
    private void addToPages(String category, EmbedBuilder eb, int pageCount) {
        eb.setTitle("HifumiBot Help - " + category + " - Page " + (helpPages.get(category).size() + 1) + " / " + pageCount);
        eb.setDescription("Use `" + CommandInterpreter.PREFIX + "help " + category + " [page]` to browse other pages.");
        helpPages.get(category).add(eb.build());
    }
}
