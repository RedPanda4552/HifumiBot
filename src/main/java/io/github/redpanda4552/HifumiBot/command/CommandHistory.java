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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import io.github.redpanda4552.HifumiBot.CommandInterpreter;
import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.SQLite;
import io.github.redpanda4552.HifumiBot.util.CommandMeta;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class CommandHistory extends AbstractCommand {

    private final MessageEmbed usage;
    
    public CommandHistory(HifumiBot hifumiBot) {
        super(hifumiBot, true, "builtin");
        
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("History Usage");
        eb.addField("Syntax", "`history <row count> [t+<datetime>] [t-<datetime] [s=<serverid>] [c=<channelid>] [u=<userid>]`", false);
        eb.addField("<row count>", "(Required) Max number of results.", false);
        /*
        eb.addField("[t+<datetime>]", "(Optional) Only return results after this datetime; <datetime> must be ISO-8601 compliant", false);
        eb.addField("[t-<datetime>]", "(Optional) Only return results before this datetime; <datetime> must be ISO-8601 compliant", false);
        eb.addField("[s=<serverid>]", "(Optional) Only return results matching this server ID", false);
        eb.addField("[c=<channelid>]", "(Optional) Only return results matching this channel ID", false);
        eb.addField("[u=<userid>]", "(Optional) Only return results matching this user ID", false);
        */
        usage = eb.build();
    }

    @Override
    protected void onExecute(CommandMeta cm) {
        String[] args = cm.getArgs();
        
        if (args.length == 0) {
            hifumiBot.sendMessage(cm.getChannel(), usage);
            return;
        }
        
        int limit = 5;
        
        if (args.length >= 1) {
            try {
                limit = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                // Suppress it and move along
            }
        }
        
        /*
        for (int i = 1; i < args.length; i++) {
            
        }
        */
        
        try {
            PreparedStatement ps = SQLite.prepareStatement(
                    "SELECT * FROM " + CommandInterpreter.HISTORY_TABLE + " ORDER BY timestamp DESC LIMIT " + limit + ";"
            );
            
            ResultSet res = ps.executeQuery();
            MessageBuilder mb = new MessageBuilder("```\n");
            
            while (res.next()) {
                StringBuilder sb = new StringBuilder();
                sb.append("tim = ").append(res.getString("timestamp")).append(" // ");
                sb.append("srv = ").append(res.getString("serverName")).append(" // ");
                sb.append("cha = ").append(res.getString("channelName")).append(" // ");
                sb.append("usr = ").append(res.getString("userName")).append(" // ");
                sb.append("cmd = ").append(res.getString("command")).append(" // ");
                sb.append("arg = ").append(res.getString("args")).append("\n");
                
                if (mb.length() + sb.length() > Message.MAX_CONTENT_LENGTH - 3) {
                    mb.append("```");
                    hifumiBot.sendMessage(cm.getUser().openPrivateChannel().complete(), mb.build());
                    mb = new MessageBuilder("```\n");
                }
                
                mb.append(sb.toString());
            }
            
            // And send anything leftover
            mb.append("```");
            hifumiBot.sendMessage(cm.getUser().openPrivateChannel().complete(), mb.build());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String getHelpText() {
        return "View history of Hifumi commands executed by users";
    }

}
