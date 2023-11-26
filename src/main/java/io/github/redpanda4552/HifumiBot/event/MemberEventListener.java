package io.github.redpanda4552.HifumiBot.event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;

import io.github.redpanda4552.HifumiBot.EventLogging;
import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.MySQL;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MemberEventListener extends ListenerAdapter {
    
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        // Store user and join records, then check for the join-leave-join pattern
        Connection conn = null;

        try {
            conn = HifumiBot.getSelf().getMySQL().getConnection();
            
            PreparedStatement insertUser = conn.prepareStatement("INSERT INTO user (discord_id, created_datetime, username) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE discord_id=discord_id;");
            insertUser.setLong(1, event.getMember().getIdLong());
            insertUser.setLong(2, event.getMember().getTimeCreated().toEpochSecond());
            insertUser.setString(3, event.getUser().getName());
            insertUser.executeUpdate();
            insertUser.close();

            PreparedStatement insertEvent = conn.prepareStatement("INSERT INTO member_event (timestamp, fk_user, action) VALUES (?, ?, ?);");
            insertEvent.setLong(1, event.getGuild().retrieveMemberById(event.getMember().getId()).complete().getTimeJoined().toEpochSecond());
            insertEvent.setLong(2, event.getMember().getIdLong());
            insertEvent.setString(3, "join");
            insertEvent.executeUpdate();
            insertEvent.close();

            PreparedStatement eventCount = conn.prepareStatement("SELECT COUNT(timestamp) events FROM member_event WHERE fk_user = ? AND action = 'join';");
            eventCount.setLong(1, event.getMember().getIdLong());
            ResultSet eventCountRes = eventCount.executeQuery();
            
            if (eventCountRes.next()) {
                if (eventCountRes.getInt("events") == 2) {
                    PreparedStatement joinEvents = conn.prepareStatement("SELECT timestamp FROM member_event WHERE fk_user = ? AND action = 'join' ORDER BY timestamp ASC LIMIT 2;");
                    joinEvents.setLong(1, event.getMember().getIdLong());
                    ResultSet joinEventsRes = joinEvents.executeQuery();

                    ArrayList<Instant> eventTimes = new ArrayList<Instant>();

                    if (joinEventsRes.next()) {
                        do {
                            long epochSeconds = joinEventsRes.getLong("timestamp");
                            Instant eventTime = Instant.ofEpochSecond(epochSeconds);
                            eventTimes.add(eventTime);
                        } while (joinEventsRes.next());
                    }

                    if (eventTimes.size() == 2) {
                        long minutesBetween = Duration.between(eventTimes.get(0), eventTimes.get(1)).toMinutes();
                        
                        if (minutesBetween < 5) {
                            EmbedBuilder eb = new EmbedBuilder();
                            eb.setTitle("Fast Join-Leave-Join Detected");
                            eb.setDescription("A join-leave-join pattern was detected within less than five minutes. This is often a sign that someone is trying to remove the 'new here' badge given to new server members. Check the <#" + HifumiBot.getSelf().getConfig().channels.logging.memberJoin + "> channel for details.");
                            eb.addField("Username (As Mention)", event.getUser().getAsMention(), true);
                            eb.addField("Username (Plain Text)", event.getUser().getName(), true);
                            eb.addField("User ID", event.getUser().getId(), false);
                            Messaging.logInfoEmbed(eb.build());
                        }
                    }

                    joinEventsRes.close();
                    joinEvents.close();
                }
            }

            eventCountRes.close();
            eventCount.close();
        } catch (SQLException e) {
             Messaging.logException("MemberEventListener", "onGuildMemberJoin", e);
        } finally {
            MySQL.closeConnection(conn);
        }
        
        // Reassign warez
        if (HifumiBot.getSelf().getWarezTracking().warezUsers.containsKey(event.getUser().getId())) {
            Role role = event.getGuild().getRoleById(HifumiBot.getSelf().getConfig().roles.warezRoleId);
            event.getGuild().addRoleToMember(event.getMember(), role).queue();
        }

        EventLogging.logGuildMemberJoinEvent(event);
    }

    @Override 
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        OffsetDateTime time = OffsetDateTime.now();

        // Store user and leave records
        Connection conn = null;

        try {
            conn = HifumiBot.getSelf().getMySQL().getConnection();
            
            PreparedStatement insertUser = conn.prepareStatement("INSERT INTO user (discord_id, created_datetime, username) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE discord_id=discord_id;");
            insertUser.setLong(1, event.getUser().getIdLong());
            insertUser.setLong(2, event.getUser().getTimeCreated().toEpochSecond());
            insertUser.setString(3, event.getUser().getName());
            insertUser.executeUpdate();
            insertUser.close();

            PreparedStatement insertEvent = conn.prepareStatement("INSERT INTO member_event (timestamp, fk_user, action) VALUES (?, ?, ?);");
            insertEvent.setLong(1, time.toEpochSecond());
            insertEvent.setLong(2, event.getMember().getIdLong());
            insertEvent.setString(3, "leave");
            insertEvent.executeUpdate();
            insertEvent.close();
        } catch (SQLException e) {
             Messaging.logException("MemberEventListener", "onGuildMemberJoin", e);
        } finally {
            MySQL.closeConnection(conn);
        }

        EventLogging.logGuildMemberRemoveEvent(event);
    }

    @Override
    public void onGuildBan(GuildBanEvent event) {
        OffsetDateTime time = OffsetDateTime.now();

        // Store user and leave records
        Connection conn = null;

        try {
            conn = HifumiBot.getSelf().getMySQL().getConnection();

            PreparedStatement insertUser = conn.prepareStatement("INSERT INTO user (discord_id, created_datetime, username) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE discord_id=discord_id;");
            insertUser.setLong(1, event.getUser().getIdLong());
            insertUser.setLong(2, event.getUser().getTimeCreated().toEpochSecond());
            insertUser.setString(3, event.getUser().getName());
            insertUser.executeUpdate();
            insertUser.close();

            PreparedStatement insertEvent = conn.prepareStatement("INSERT INTO member_event (timestamp, fk_user, action) VALUES (?, ?, ?);");
            insertEvent.setLong(1, time.toEpochSecond());
            insertEvent.setLong(2, event.getUser().getIdLong());
            insertEvent.setString(3, "ban");
            insertEvent.executeUpdate();
            insertEvent.close();
        } catch (SQLException e) {
             Messaging.logException("MemberEventListener", "onGuildMemberJoin", e);
        } finally {
            MySQL.closeConnection(conn);
        }

        EventLogging.logGuildBanEvent(event);
    }
}
