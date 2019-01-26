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

import java.util.HashMap;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.wiki.Emotes;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class CommandWiki extends AbstractCommand {

    public CommandWiki(HifumiBot hifumiBot) {
        super(hifumiBot, false, CATEGORY_BUILTIN);
    }

    @Override
    protected void onExecute(MessageChannel channel, Member senderMember, User senderUser, String[] args) {
        if (args.length == 0) {
            hifumiBot.sendMessage(channel, "I can't search for nothing! Try `!wiki <title of game here>`");
            return;
        }
        
        HashMap<String, Float> results = new HashMap<String, Float>();
        
        for (String name : hifumiBot.getFullGamesMap().keySet()) {
            float toPush = 0;
            
            for (String arg : args) {
                if (name.toLowerCase().trim().contains(arg.toLowerCase().trim()))
                    toPush++;
            }
            
            String[] nameParts = name.split(" ");
            toPush -= 0.1f * Math.abs(nameParts.length - args.length);
            
            if (toPush > 0)
                results.put(name, toPush);
        }
        
        EmbedBuilder eb = new EmbedBuilder();
        int i = 0;
        
        if (results.size() > 0) {
            eb.setTitle("Query Results");
            String highestName = null;
            float highestWeight = 0;
            
            while (!results.isEmpty() && i < 6) {
                for (String name : results.keySet()) {
                    if (results.get(name) > highestWeight) {
                        highestName = name;
                        highestWeight = results.get(name);
                    }
                }
                
                results.remove(highestName);
                highestWeight = 0;
                eb.addField(String.valueOf(++i), highestName, false);
            }
            
            eb.setFooter("Click the reaction number matching the game you are looking for.\nThis message will self-modify with it's wiki information.", hifumiBot.getJDA().getSelfUser().getAvatarUrl());
        } else {
            eb.setTitle("No results matched your query!");
            eb.setColor(0xff0000);
        }
        
        Message msg = hifumiBot.sendMessage(channel, eb.build());
        
        // String concatenation with unicodes is apparently punishable by build error, so we instead have this.
        if (i > 0)
            msg.addReaction(Emotes.ONE).complete();
        if (i > 1)
            msg.addReaction(Emotes.TWO).complete();
        if (i > 2)
            msg.addReaction(Emotes.THREE).complete();
        if (i > 3)
            msg.addReaction(Emotes.FOUR).complete();
        if (i > 4)
            msg.addReaction(Emotes.FIVE).complete();
        if (i > 5)
            msg.addReaction(Emotes.SIX).complete();
        
        hifumiBot.getEventListener().waitForMessage(senderUser.getId(), msg);
    }
    
    @Override
    protected String getHelpText() {
        return "Search the PCSX2 wiki by game title";
    }
}
