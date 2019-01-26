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

import org.apache.commons.lang3.ArrayUtils;

import io.github.redpanda4552.HifumiBot.CommandInterpreter;
import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.voting.Vote;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;

public class CommandVote extends AbstractCommand {

    private final int VOTES_PER_PAGE = 6;
    private final String[] USAGE = {
            "To see this help dialog:`" + CommandInterpreter.PREFIX + "vote`",
            "To list all open votes:`" + CommandInterpreter.PREFIX + "vote list`",
            "To start a vote:`" + CommandInterpreter.PREFIX + "vote start <name> <\"your question\"> <\"answer 1\"> <\"answer 2\"> ... [\"answer n\"]`",
            "To stop a vote:`" + CommandInterpreter.PREFIX + "vote stop <name>`",
            "To cast a vote:`" + CommandInterpreter.PREFIX + "vote cast <name> <answer>`",
    };
    
    public CommandVote(HifumiBot hifumiBot) {
        super(hifumiBot, false, CATEGORY_BUILTIN);
    }

    @Override
    protected void onExecute(MessageChannel channel, Member senderMember, User senderUser, String[] args) {
        if (args.length < 1) {
            showHelp(channel, senderMember);
        } else {
            switch (args[0].trim().toLowerCase()) {
            case "list":
                showList(channel, senderMember);
                break;
            case "start":
                startVote(channel, senderMember, args);
                break;
            case "stop":
                stopVote(channel, senderMember, args);
                break;
            case "cast":
                castVote(channel, senderMember, args);
                break;
            default:
                showHelp(channel, senderMember);
            }
        }
    }

    @Override
    protected String getHelpText() {
        return "Start a vote, or participate in one.";
    }
    
    private void showHelp(MessageChannel channel, Member senderMember) {
        EmbedBuilder eb = newFootedEmbedBuilder(senderMember);
        eb.setTitle("HifumiBot Voting System");
        eb.setDescription("HifumiBot will automatically add a thumbs up reaction to your casts, to let you know she has seen it.");
        
        for (String str : USAGE) {
            String[] parts = str.split(":");
            eb.addField(parts[0], parts[1], false);
        }
        
        hifumiBot.sendMessage(channel, USAGE);
    }
    
    private void showList(MessageChannel channel, Member senderMember) {
        ArrayList<Vote> allVotes = hifumiBot.getVoteManager().getAllVotes();
        ArrayList<MessageEmbed> embedList = new ArrayList<MessageEmbed>();
        int pageCurrent = 1, pageTotal = (int) Math.ceil((double) allVotes.size() / VOTES_PER_PAGE);
        
        EmbedBuilder eb = newFootedEmbedBuilder(senderMember);
        eb.setTitle("Open Votes (" + pageCurrent++ + "/" + pageTotal + ")");
        
        for (Vote vote : allVotes) {
            if (eb.getFields().size() >= VOTES_PER_PAGE) {
                embedList.add(eb.build());
                eb = newFootedEmbedBuilder(senderMember);
                eb.setTitle("Open Votes (" + pageCurrent++ + "/" + pageTotal + ")");
            }
            
            StringBuilder sb = new StringBuilder();
            
            for (String option : vote.getOptions())
                sb.append(option).append("\n");
            
            eb.addField(vote.getName(), sb.toString().trim(), true);
        }
        
        for (MessageEmbed embed : embedList)
            hifumiBot.sendMessage(channel, embed);
    }
    
    private void startVote(MessageChannel channel, Member senderMember, String[] args) {
        if (args.length < 5) {
            showHelp(channel, senderMember);
            return;
        }
        
        hifumiBot.getVoteManager().createVote(args[1], args[2], senderMember.getUser().getId(), ArrayUtils.subarray(args, 3, args.length));
        
        EmbedBuilder eb = newFootedEmbedBuilder(senderMember);
        eb.setTitle("New Vote Started by " + senderMember.getEffectiveName());
        
    }
    
    private void stopVote(MessageChannel channel, Member senderMember, String[] args) {
        
    }
    
    private void castVote(MessageChannel channel, Member senderMember, String[] args) {
        
    }
}
