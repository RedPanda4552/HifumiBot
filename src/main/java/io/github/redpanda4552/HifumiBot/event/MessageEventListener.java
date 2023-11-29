package io.github.redpanda4552.HifumiBot.event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;

import org.apache.commons.lang3.StringUtils;

import io.github.redpanda4552.HifumiBot.EventLogging;
import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.MySQL;
import io.github.redpanda4552.HifumiBot.filter.FilterRunnable;
import io.github.redpanda4552.HifumiBot.filter.MessageHistoryEntry;
import io.github.redpanda4552.HifumiBot.parse.CrashParser;
import io.github.redpanda4552.HifumiBot.parse.EmulogParser;
import io.github.redpanda4552.HifumiBot.parse.PnachParser;
import io.github.redpanda4552.HifumiBot.permissions.PermissionLevel;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import io.github.redpanda4552.HifumiBot.util.PixivSourceFetcher;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageEventListener extends ListenerAdapter {
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Instant now = Instant.now();

        if (event.getChannelType() == ChannelType.PRIVATE) {
            if (!event.getAuthor().getId().equals(HifumiBot.getSelf().getJDA().getSelfUser().getId())) {
                Messaging.logInfo("EventListener", "onMessageReceived", "DM sent to Hifumi by user " + event.getAuthor().getAsMention() + " (" + event.getAuthor().getName() + ")\n\n```\n" + StringUtils.truncate(event.getMessage().getContentRaw(), 500) + "\n```\nMessage content displayed raw format, truncated to 500 chars. Original length: " + event.getMessage().getContentRaw().length());
                Messaging.sendMessage(event.getChannel(), "I am a bot. If you need something, please ask a human in the server.", event.getMessage(), false);
            }
            
            return;
        }

        // Store user, channel, message, and event records
        Connection conn = null;

        try {
            conn = HifumiBot.getSelf().getMySQL().getConnection();

            PreparedStatement insertUser = conn.prepareStatement("INSERT INTO user (discord_id, created_datetime, username) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE discord_id=discord_id;");
            insertUser.setLong(1, event.getAuthor().getIdLong());
            insertUser.setLong(2, event.getAuthor().getTimeCreated().toEpochSecond());
            insertUser.setString(3, event.getAuthor().getName());
            insertUser.executeUpdate();
            insertUser.close();

            PreparedStatement insertChannel = conn.prepareStatement("INSERT INTO channel (discord_id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE discord_id=discord_id;");
            insertChannel.setLong(1, event.getChannel().getIdLong());
            insertChannel.setString(2, event.getChannel().getName());
            insertChannel.executeUpdate();
            insertChannel.close();

            PreparedStatement insertMessage = conn.prepareStatement("INSERT INTO message (message_id, fk_channel) VALUES (?, ?);");
            insertMessage.setLong(1, event.getMessageIdLong());
            insertMessage.setLong(2, event.getChannel().getIdLong());
            insertMessage.executeUpdate();
            insertMessage.close();

            PreparedStatement insertEvent = conn.prepareStatement("INSERT INTO message_event (fk_user, fk_message, timestamp, action, content) VALUES (?, ?, ?, ?, ?)");
            insertEvent.setLong(1, event.getAuthor().getIdLong());
            insertEvent.setLong(2, event.getMessageIdLong());
            insertEvent.setLong(3, event.getMessage().getTimeCreated().toEpochSecond());
            insertEvent.setString(4, "send");
            insertEvent.setString(5, event.getMessage().getContentRaw());
            insertEvent.executeUpdate();
            insertEvent.close();
        } catch (SQLException e) {
             Messaging.logException("MemberEventListener", "onGuildMemberJoin", e);
        } finally {
            MySQL.closeConnection(conn);
        }
        
        if (HifumiBot.getSelf().getPermissionManager().hasPermission(PermissionLevel.GUEST, event.getMember())) {
            if (Messaging.hasEmulog(event.getMessage())) {
                EmulogParser ep = new EmulogParser(event.getMessage());
                HifumiBot.getSelf().getScheduler().runOnce(ep);
            }

            if (Messaging.hasPnach(event.getMessage())) {
                PnachParser pp = new PnachParser(event.getMessage());
                HifumiBot.getSelf().getScheduler().runOnce(pp);
            }

            if (Messaging.hasCrashLog(event.getMessage())) {
                CrashParser crashp = new CrashParser(event.getMessage());
                HifumiBot.getSelf().getScheduler().runOnce(crashp);
            }
        }

        if (!HifumiBot.getSelf().getPermissionManager().hasPermission(PermissionLevel.MOD, event.getMember())) {
            HifumiBot.getSelf().getScheduler().runOnce(new FilterRunnable(event.getMessage(), now));
            
            if (Messaging.hasBotPing(event.getMessage())) {
                Messaging.sendMessage(event.getChannel(), "You are pinging a bot.", event.getMessage(), false);
            }
        } else {
            HifumiBot.getSelf().getMessageHistoryManager().store(event.getMessage());
        }

        if (!event.getAuthor().getId().equals(HifumiBot.getSelf().getJDA().getSelfUser().getId())) {
            if (Messaging.hasGhostPing(event.getMessage())) {
                Messaging.sendMessage(event.getChannel(), ":information_source: The user you tried to mention has left the server.", event.getMessage(), false);
            }
        }
        
        if (event.getMember() != null && event.getMember().getRoles().isEmpty()) {
            Instant joinTime = event.getMember().getGuild().retrieveMemberById(event.getAuthor().getId()).complete().getTimeJoined().toInstant();
            
            if (Duration.between(joinTime, now).toSeconds() >= HifumiBot.getSelf().getConfig().roles.autoAssignMemberTimeSeconds) {
                event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(HifumiBot.getSelf().getConfig().roles.autoAssignMemberRoleId)).complete();
            }
        }
        
        PixivSourceFetcher.getPixivLink(event.getMessage());
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        OffsetDateTime now = OffsetDateTime.now();
        
        // Store channel, message and event records
        Connection conn = null;
        long userId = 0;

        try {
            conn = HifumiBot.getSelf().getMySQL().getConnection();

            PreparedStatement getUser = conn.prepareStatement("SELECT fk_user, fk_message FROM message_event WHERE fk_message = ? LIMIT 1;");
            getUser.setLong(1, event.getMessageIdLong());
            ResultSet res = getUser.executeQuery();

            if (res.next()) {
                userId = res.getLong("fk_user");
            }
            
            getUser.close();

            PreparedStatement insertChannel = conn.prepareStatement("INSERT INTO channel (discord_id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE discord_id=discord_id;");
            insertChannel.setLong(1, event.getChannel().getIdLong());
            insertChannel.setString(2, event.getChannel().getName());
            insertChannel.executeUpdate();
            insertChannel.close();

            PreparedStatement insertMessage = conn.prepareStatement("INSERT INTO message (message_id, fk_channel) VALUES (?, ?) ON DUPLICATE KEY UPDATE message_id=message_id;");
            insertMessage.setLong(1, event.getMessageIdLong());
            insertMessage.setLong(2, event.getChannel().getIdLong());
            insertMessage.executeUpdate();
            insertMessage.close();

            PreparedStatement insertEvent = conn.prepareStatement("INSERT INTO message_event (fk_user, fk_message, timestamp, action) VALUES (?, ?, ?, ?)");
            insertEvent.setLong(1, userId);
            insertEvent.setLong(2, event.getMessageIdLong());
            insertEvent.setLong(3, now.toEpochSecond());
            insertEvent.setString(4, "delete");
            insertEvent.executeUpdate();
            insertEvent.close();
        } catch (SQLException e) {
             Messaging.logException("MemberEventListener", "onGuildMemberJoin", e);
        } finally {
            MySQL.closeConnection(conn);
        }

        MessageHistoryEntry entry = HifumiBot.getSelf().getMessageHistoryManager().fetchMessage(event.getMessageId());

        if (entry != null) {
            if (!entry.getUserId().equals(HifumiBot.getSelf().getJDA().getSelfUser().getId())) {
                EventLogging.logMessageDeleteEvent(entry);
            }
        } else {
            EventLogging.logMessageDeleteEvent(event.getGuildChannel().getAsMention(), event.getMessageId());
        }
    }

    @Override 
    public void onMessageBulkDelete(MessageBulkDeleteEvent event) {
        OffsetDateTime now = OffsetDateTime.now();
        
        for (String messageId : event.getMessageIds()) {
            // Store channel, message and event records
            Connection conn = null;
            long userId = 0;

            try {
                conn = HifumiBot.getSelf().getMySQL().getConnection();

                PreparedStatement getUser = conn.prepareStatement("SELECT fk_user, fk_message FROM message_event WHERE fk_message = ? LIMIT 1;");
                getUser.setLong(1, Long.valueOf(messageId));
                ResultSet res = getUser.executeQuery();

                if (res.next()) {
                    userId = res.getLong("fk_user");
                }
                
                getUser.close();

                PreparedStatement insertChannel = conn.prepareStatement("INSERT INTO channel (discord_id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE discord_id=discord_id;");
                insertChannel.setLong(1, event.getChannel().getIdLong());
                insertChannel.setString(2, event.getChannel().getName());
                insertChannel.executeUpdate();
                insertChannel.close();

                PreparedStatement insertMessage = conn.prepareStatement("INSERT INTO message (message_id, fk_channel) VALUES (?, ?) ON DUPLICATE KEY UPDATE message_id=message_id;");
                insertMessage.setLong(1, Long.valueOf(messageId));
                insertMessage.setLong(2, event.getChannel().getIdLong());
                insertMessage.executeUpdate();
                insertMessage.close();

                PreparedStatement insertEvent = conn.prepareStatement("INSERT INTO message_event (fk_user, fk_message, timestamp, action) VALUES (?, ?, ?, ?)");
                insertEvent.setLong(1, userId);
                insertEvent.setLong(2, Long.valueOf(messageId));
                insertEvent.setLong(3, now.toEpochSecond());
                insertEvent.setString(4, "delete");
                insertEvent.executeUpdate();
                insertEvent.close();
            } catch (SQLException e) {
                Messaging.logException("MemberEventListener", "onGuildMemberJoin", e);
            } finally {
                MySQL.closeConnection(conn);
            }

            MessageHistoryEntry entry = HifumiBot.getSelf().getMessageHistoryManager().fetchMessage(messageId);

            if (entry != null) {
                EventLogging.logMessageDeleteEvent(entry);
                HifumiBot.getSelf().getMessageHistoryManager().removeMessage(messageId);
            } else {
                EventLogging.logMessageDeleteEvent(event.getChannel().getAsMention(), messageId);
            }
        }
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        // Store user, channel, message, and event records
        Connection conn = null;

        try {
            conn = HifumiBot.getSelf().getMySQL().getConnection();

            PreparedStatement insertUser = conn.prepareStatement("INSERT INTO user (discord_id, created_datetime, username) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE discord_id=discord_id;");
            insertUser.setLong(1, event.getAuthor().getIdLong());
            insertUser.setLong(2, event.getAuthor().getTimeCreated().toEpochSecond());
            insertUser.setString(3, event.getAuthor().getName());
            insertUser.executeUpdate();
            insertUser.close();

            PreparedStatement insertChannel = conn.prepareStatement("INSERT INTO channel (discord_id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE discord_id=discord_id;");
            insertChannel.setLong(1, event.getChannel().getIdLong());
            insertChannel.setString(2, event.getChannel().getName());
            insertChannel.executeUpdate();
            insertChannel.close();

            PreparedStatement insertMessage = conn.prepareStatement("INSERT INTO message (message_id, fk_channel) VALUES (?, ?) ON DUPLICATE KEY UPDATE message_id=message_id;");
            insertMessage.setLong(1, event.getMessageIdLong());
            insertMessage.setLong(2, event.getChannel().getIdLong());
            insertMessage.executeUpdate();
            insertMessage.close();

            PreparedStatement insertEvent = conn.prepareStatement("INSERT INTO message_event (fk_user, fk_message, timestamp, action, content) VALUES (?, ?, ?, ?, ?)");
            insertEvent.setLong(1, event.getAuthor().getIdLong());
            insertEvent.setLong(2, event.getMessageIdLong());
            insertEvent.setLong(3, event.getMessage().getTimeEdited().toEpochSecond());
            insertEvent.setString(4, "edit");
            insertEvent.setString(5, event.getMessage().getContentRaw());
            insertEvent.executeUpdate();
            insertEvent.close();
        } catch (SQLException e) {
             Messaging.logException("MemberEventListener", "onGuildMemberJoin", e);
        } finally {
            MySQL.closeConnection(conn);
        }

        MessageHistoryEntry entry = HifumiBot.getSelf().getMessageHistoryManager().fetchMessage(event.getMessageId());

        if (!entry.getUserId().equals(HifumiBot.getSelf().getJDA().getSelfUser().getId())) {
            EventLogging.logMessageUpdateEvent(event, entry);
            HifumiBot.getSelf().getMessageHistoryManager().store(event.getMessage());
        }
    }
}
