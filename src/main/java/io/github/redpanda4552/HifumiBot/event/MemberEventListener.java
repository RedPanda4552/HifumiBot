package io.github.redpanda4552.HifumiBot.event;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.OffsetDateTime;

import io.github.redpanda4552.HifumiBot.EventLogging;
import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MemberEventListener extends ListenerAdapter {
    
    private boolean lockdown = false;

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (lockdown) {
            if (event.getMember() != null) {
                HifumiBot.getSelf().getKickHandler().doKickForBotJoin(event.getMember());
                return;
            }
        }

        // Store user and join records
        try {
            PreparedStatement ps = null;
            
            ps = HifumiBot.getSelf().getMySQL().prepareStatement("INSERT INTO user (discord_id, created_datetime) VALUES (?, ?) ON DUPLICATE KEY UPDATE discord_id=discord_id;");
            ps.setLong(1, event.getMember().getIdLong());
            ps.setLong(2, event.getMember().getTimeCreated().toEpochSecond());
            ps.executeUpdate();

            ps = HifumiBot.getSelf().getMySQL().prepareStatement("INSERT INTO member_event (timestamp, fk_user, action) VALUES (?, ?, ?);");
            ps.setLong(1, event.getGuild().retrieveMemberById(event.getMember().getId()).complete().getTimeJoined().toEpochSecond());
            ps.setLong(2, event.getMember().getIdLong());
            ps.setString(3, "join");
            ps.executeUpdate();
        } catch (SQLException e) {
             Messaging.logException("MemberEventListener", "onGuildMemberJoin", e);
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
        try {
            PreparedStatement ps = null;
            
            ps = HifumiBot.getSelf().getMySQL().prepareStatement("INSERT INTO user (discord_id, created_datetime) VALUES (?, ?) ON DUPLICATE KEY UPDATE discord_id=discord_id;");
            ps.setLong(1, event.getMember().getIdLong());
            ps.setLong(2, event.getMember().getTimeCreated().toEpochSecond());
            ps.executeUpdate();

            ps = HifumiBot.getSelf().getMySQL().prepareStatement("INSERT INTO member_event (timestamp, fk_user, action) VALUES (?, ?, ?);");
            ps.setLong(1, time.toEpochSecond());
            ps.setLong(2, event.getMember().getIdLong());
            ps.setString(3, "leave");
            ps.executeUpdate();
        } catch (SQLException e) {
             Messaging.logException("MemberEventListener", "onGuildMemberJoin", e);
        }

        EventLogging.logGuildMemberRemoveEvent(event);
    }

    @Override
    public void onGuildBan(GuildBanEvent event) {
        OffsetDateTime time = OffsetDateTime.now();

        // Store user and leave records
        try {
            PreparedStatement ps = null;
            
            ps = HifumiBot.getSelf().getMySQL().prepareStatement("INSERT INTO user (discord_id, created_datetime) VALUES (?, ?) ON DUPLICATE KEY UPDATE discord_id=discord_id;");
            ps.setLong(1, event.getUser().getIdLong());
            ps.setLong(2, event.getUser().getTimeCreated().toEpochSecond());
            ps.executeUpdate();

            ps = HifumiBot.getSelf().getMySQL().prepareStatement("INSERT INTO member_event (timestamp, fk_user, action) VALUES (?, ?, ?);");
            ps.setLong(1, time.toEpochSecond());
            ps.setLong(2, event.getUser().getIdLong());
            ps.setString(3, "ban");
            ps.executeUpdate();
        } catch (SQLException e) {
             Messaging.logException("MemberEventListener", "onGuildMemberJoin", e);
        }

        EventLogging.logGuildBanEvent(event);
    }

    public void setLockdown(boolean lockdown) {
        this.lockdown = lockdown;
    }
    
    public boolean getLockdown() {
        return lockdown;
    }
}
