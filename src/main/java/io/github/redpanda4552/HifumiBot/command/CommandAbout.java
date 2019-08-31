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
import java.time.OffsetDateTime;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.SQLite;
import io.github.redpanda4552.HifumiBot.util.CommandMeta;
import io.github.redpanda4552.HifumiBot.util.EmbedUtil;
import net.dv8tion.jda.core.EmbedBuilder;

public class CommandAbout extends AbstractCommand {

    public CommandAbout(HifumiBot hifumiBot) {
        super(hifumiBot, false, CATEGORY_BUILTIN);
    }

    @Override
    protected void onExecute(CommandMeta cm) {
        EmbedBuilder eb;
        
        if (cm.getMember() != null) {
            eb = EmbedUtil.newFootedEmbedBuilder(cm.getMember());
        } else {
            eb = EmbedUtil.newFootedEmbedBuilder(cm.getUser());
        }
        
        eb.setTitle("About HifumiBot");
        eb.setDescription("A helper bot for the PCSX2 server.");
        eb.addField("Created By", "pandubz", true);
        String version = getClass().getPackage().getImplementationVersion();
        eb.addField("Version", version != null ? version : "[Debug Mode]", true);
        
        try {
            PreparedStatement ps = SQLite.prepareStatement(
                    "SELECT COUNT(timestamp) commandCount " + 
                    "FROM command_history " + 
                    "WHERE timestamp > ?;"
            );
            ps.setString(1, OffsetDateTime.now().minusDays(1).toString());
            ResultSet res = ps.executeQuery();
            
            if (res.next()) {
                eb.addField("Commands Processed (24 hours)", res.getString("commandCount"), true);
            }
            
            ps = SQLite.prepareStatement(
                    "SELECT COUNT(timestamp) commandCount " + 
                    "FROM command_history;"
            );
            
            res = ps.executeQuery();
            
            if (res.next()) {
                eb.addField("Commands Processed (all time)", res.getString("commandCount"), true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        HifumiBot.getSelf().sendMessage(cm.getChannel(), eb.build());
    }

    @Override
    protected String getHelpText() {
        return "Info about HifumiBot";
    }

}
