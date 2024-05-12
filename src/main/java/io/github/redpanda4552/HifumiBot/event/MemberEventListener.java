package io.github.redpanda4552.HifumiBot.event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;

import io.github.redpanda4552.HifumiBot.EventLogging;
import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.database.Database;
import io.github.redpanda4552.HifumiBot.database.MemberEventObject;
import io.github.redpanda4552.HifumiBot.database.MySQL;
import io.github.redpanda4552.HifumiBot.database.WarezEventObject;
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
        Database.insertMemberJoinEvent(event);
        ArrayList<MemberEventObject> events = Database.getRecentMemberEvents(event.getMember().getIdLong());

        if (events.size() >= 3) {
            MemberEventObject latestEvent = events.get(0);
            MemberEventObject secondEvent = events.get(1);
            MemberEventObject thirdEvent = events.get(2);

            if (latestEvent.getAction().equals(MemberEventObject.Action.JOIN) && secondEvent.getAction().equals(MemberEventObject.Action.LEAVE) && thirdEvent.getAction().equals(MemberEventObject.Action.JOIN)) {
                Instant newestJoinTime = Instant.ofEpochSecond(latestEvent.getTimestamp());
                Instant olderJoinTime = Instant.ofEpochSecond(latestEvent.getTimestamp());
                long minutesBetween = Duration.between(olderJoinTime, newestJoinTime).toMinutes();

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
        }
        
        // Reassign warez
        WarezEventObject warezEvent = Database.getLatestWarezAction(event.getMember().getIdLong());

        if (warezEvent != null) {
            if (warezEvent.getAction().equals(WarezEventObject.Action.ADD)) {
                Role role = event.getGuild().getRoleById(HifumiBot.getSelf().getConfig().roles.warezRoleId);
                event.getGuild().addRoleToMember(event.getMember(), role).queue();
            }
        }
        
        EventLogging.logGuildMemberJoinEvent(event, warezEvent);
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
            insertEvent.setLong(2, event.getUser().getIdLong());
            insertEvent.setString(3, "leave");
            insertEvent.executeUpdate();
            insertEvent.close();
        } catch (SQLException e) {
             Messaging.logException("MemberEventListener", "onGuildMemberRemove", e);
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
             Messaging.logException("MemberEventListener", "onGuildBan", e);
        } finally {
            MySQL.closeConnection(conn);
        }

        EventLogging.logGuildBanEvent(event);
    }
}
