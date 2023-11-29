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
package io.github.redpanda4552.HifumiBot.event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.OffsetDateTime;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.MySQL;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class RoleEventListener extends ListenerAdapter {

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        OffsetDateTime now = OffsetDateTime.now();

        for (Role role : event.getRoles()) {
            if (role.getId().equals(HifumiBot.getSelf().getConfig().roles.warezRoleId)) {
                Connection conn = null;

                try {
                    conn = HifumiBot.getSelf().getMySQL().getConnection();

                    PreparedStatement insertUser = conn.prepareStatement("INSERT INTO user (discord_id, created_datetime, username) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE discord_id=discord_id;");
                    insertUser.setLong(1, event.getUser().getIdLong());
                    insertUser.setLong(2, event.getUser().getTimeCreated().toEpochSecond());
                    insertUser.setString(3, event.getUser().getName());
                    insertUser.executeUpdate();
                    insertUser.close();

                    PreparedStatement insertWarez = conn.prepareStatement("INSERT INTO warez_event (timestamp, fk_user, action) VALUES (?, ?, ?);");
                    insertWarez.setLong(1, now.toEpochSecond());
                    insertWarez.setLong(2, event.getUser().getIdLong());
                    insertWarez.setString(3, "add");
                    insertWarez.executeUpdate();
                    insertWarez.close();
                } catch (SQLException e) {
                    Messaging.logException("HifumiBot", "(constructor/warez-migration)", e);
                } finally {
                    MySQL.closeConnection(conn);
                }

                return;
            }
        }
    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        OffsetDateTime now = OffsetDateTime.now();

        for (Role role : event.getRoles()) {
            if (role.getId().equals(HifumiBot.getSelf().getConfig().roles.warezRoleId)) {
                Connection conn = null;

                try {
                    conn = HifumiBot.getSelf().getMySQL().getConnection();

                    PreparedStatement insertUser = conn.prepareStatement("INSERT INTO user (discord_id, created_datetime, username) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE discord_id=discord_id;");
                    insertUser.setLong(1, event.getUser().getIdLong());
                    insertUser.setLong(2, event.getUser().getTimeCreated().toEpochSecond());
                    insertUser.setString(3, event.getUser().getName());
                    insertUser.executeUpdate();
                    insertUser.close();

                    PreparedStatement insertWarez = conn.prepareStatement("INSERT INTO warez_event (timestamp, fk_user, action) VALUES (?, ?, ?);");
                    insertWarez.setLong(1, now.toEpochSecond());
                    insertWarez.setLong(2, event.getUser().getIdLong());
                    insertWarez.setString(3, "remove");
                    insertWarez.executeUpdate();
                    insertWarez.close();
                } catch (SQLException e) {
                    Messaging.logException("HifumiBot", "(constructor/warez-migration)", e);
                } finally {
                    MySQL.closeConnection(conn);
                }

                return;
            }
        }
    }
}
