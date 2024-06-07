package io.github.redpanda4552.HifumiBot.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.charting.MemberChartData;
import io.github.redpanda4552.HifumiBot.charting.WarezChartData;
import io.github.redpanda4552.HifumiBot.util.DateTimeUtils;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import io.github.redpanda4552.HifumiBot.util.TimeUtils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.automod.AutoModResponse;
import net.dv8tion.jda.api.events.automod.AutoModExecutionEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;

public class Database {

    /**
     * Store user, channel, message, attachment, and event records
     */
    public static void insertMessage(Message message, boolean skipEvent) {
        Connection conn = HifumiBot.getSelf().getSQLite().getConnection();

        try {
            PreparedStatement insertUser = conn.prepareStatement("""
                    INSERT INTO user (discord_id, created_datetime, username)
                    VALUES (?, ?, ?)
                    ON CONFLICT (discord_id) DO NOTHING;
                    """);
            insertUser.setLong(1, message.getAuthor().getIdLong());
            insertUser.setLong(2, message.getAuthor().getTimeCreated().toEpochSecond());
            insertUser.setString(3, message.getAuthor().getName());
            insertUser.executeUpdate();
            insertUser.close();

            PreparedStatement insertChannel = conn.prepareStatement("""
                    INSERT INTO channel (discord_id, name)
                    VALUES (?, ?)
                    ON CONFLICT (discord_id) DO NOTHING;
                    """);
            insertChannel.setLong(1, message.getChannel().getIdLong());
            insertChannel.setString(2, message.getChannel().getName());
            insertChannel.executeUpdate();
            insertChannel.close();

            // Check if the referenced message exists in the database; if not, try to add it first.
            if (message.getReferencedMessage() != null) {
                if (Database.getLatestMessage(message.getReferencedMessage().getIdLong()) == null) {
                    Database.insertMessage(message.getReferencedMessage(), skipEvent);
                }
            }

            PreparedStatement insertMessage = conn.prepareStatement("""
                    INSERT INTO message (message_id, fk_channel, jump_link, fk_reply_to_message, timestamp)
                    VALUES (?, ?, ?, ?, ?)
                    ON CONFLICT (message_id) DO NOTHING;
                    """);
            insertMessage.setLong(1, message.getIdLong());
            insertMessage.setLong(2, message.getChannel().getIdLong());
            insertMessage.setString(3, message.getJumpUrl());

            if (message.getReferencedMessage() != null) {
                insertMessage.setLong(4, message.getReferencedMessage().getIdLong());
            } else {
                insertMessage.setNull(4, Types.BIGINT);
            }
            
            insertMessage.setLong(5, message.getTimeCreated().toEpochSecond());
            insertMessage.executeUpdate();
            insertMessage.close();

            if (!skipEvent) {
                PreparedStatement insertEvent = conn.prepareStatement("""
                        INSERT INTO message_event (fk_user, fk_message, timestamp, action, content)
                        VALUES (?, ?, ?, ?, ?);
                        """);
                insertEvent.setLong(1, message.getAuthor().getIdLong());
                insertEvent.setLong(2, message.getIdLong());
                insertEvent.setLong(3, message.getTimeCreated().toEpochSecond());
                insertEvent.setString(4, "send");
                insertEvent.setString(5, message.getContentRaw());
                insertEvent.executeUpdate();
                insertEvent.close();
                
                // Check if this message had any attachments. No need to continue if not.
                List<Attachment> attachments = message.getAttachments();

                if (!attachments.isEmpty()) {
                    PreparedStatement insertAttachments = conn.prepareStatement("""
                            INSERT INTO message_attachment (discord_id, timestamp, fk_message, content_type, proxy_url, filename)
                            VALUES (?, ?, ?, ?, ?, ?);
                            """);

                    for (Attachment attachment : attachments) {
                        insertAttachments.setLong(1, attachment.getIdLong());
                        insertAttachments.setLong(2, attachment.getTimeCreated().toEpochSecond());
                        insertAttachments.setLong(3, message.getIdLong());
                        insertAttachments.setString(4, attachment.getContentType());
                        insertAttachments.setString(5, attachment.getProxyUrl());
                        insertAttachments.setString(6, attachment.getFileName());
                        insertAttachments.addBatch();
                    }
                    
                    insertAttachments.executeBatch();
                    insertAttachments.close();
                }
            }
        } catch (SQLException e) {
             Messaging.logException("Database", "insertMessageReceivedEvent", e);
        }
    }

    /**
     * Store channel, message and event records
     * @param event
     */
    public static void insertMessageDeleteEvent(MessageDeleteEvent event) {
        OffsetDateTime now = OffsetDateTime.now();
        Connection conn = null;
        long userId = 0;

        try {
            conn = HifumiBot.getSelf().getSQLite().getConnection();

            PreparedStatement getUser = conn.prepareStatement("""
                    SELECT fk_user, fk_message
                    FROM message_event
                    WHERE fk_message = ?
                    LIMIT 1;
                    """);
            getUser.setLong(1, event.getMessageIdLong());
            ResultSet res = getUser.executeQuery();

            if (res.next()) {
                userId = res.getLong("fk_user");
            }
            
            getUser.close();

            PreparedStatement insertChannel = conn.prepareStatement("""
                    INSERT INTO channel (discord_id, name)
                    VALUES (?, ?)
                    ON CONFLICT (discord_id) DO NOTHING;
                    """);
            insertChannel.setLong(1, event.getChannel().getIdLong());
            insertChannel.setString(2, event.getChannel().getName());
            insertChannel.executeUpdate();
            insertChannel.close();

            PreparedStatement insertMessage = conn.prepareStatement("""
                    INSERT INTO message (message_id, fk_channel)
                    VALUES (?, ?)
                    ON CONFLICT (message_id) DO NOTHING;
                    """);
            insertMessage.setLong(1, event.getMessageIdLong());
            insertMessage.setLong(2, event.getChannel().getIdLong());
            insertMessage.executeUpdate();
            insertMessage.close();

            PreparedStatement insertEvent = conn.prepareStatement("""
                    INSERT INTO message_event (fk_user, fk_message, timestamp, action)
                    VALUES (?, ?, ?, ?);
                    """);
            insertEvent.setLong(1, userId);
            insertEvent.setLong(2, event.getMessageIdLong());
            insertEvent.setLong(3, now.toEpochSecond());
            insertEvent.setString(4, "delete");
            insertEvent.executeUpdate();
            insertEvent.close();
        } catch (SQLException e) {
             Messaging.logException("Database", "insertMessageDeleteEvent", e);
        }
    }

    /**
     * Store channel, message and event records
     * @param event
     */
    public static void insertMessageBulkDeleteEvent(MessageBulkDeleteEvent event) {
        OffsetDateTime now = OffsetDateTime.now();
        
        for (String messageId : event.getMessageIds()) {
            Connection conn = null;
            long userId = 0;

            try {
                conn = HifumiBot.getSelf().getSQLite().getConnection();

                PreparedStatement getUser = conn.prepareStatement("""
                        SELECT fk_user, fk_message
                        FROM message_event
                        WHERE fk_message = ?
                        LIMIT 1;
                        """);
                getUser.setLong(1, Long.valueOf(messageId));
                ResultSet res = getUser.executeQuery();

                if (res.next()) {
                    userId = res.getLong("fk_user");
                }
                
                getUser.close();

                PreparedStatement insertChannel = conn.prepareStatement("""
                        INSERT INTO channel (discord_id, name)
                        VALUES (?, ?)
                        ON CONFLICT (discord_id) DO NOTHING;
                        """);
                insertChannel.setLong(1, event.getChannel().getIdLong());
                insertChannel.setString(2, event.getChannel().getName());
                insertChannel.executeUpdate();
                insertChannel.close();

                PreparedStatement insertMessage = conn.prepareStatement("""
                        INSERT INTO message (message_id, fk_channel)
                        VALUES (?, ?)
                        ON CONFLICT (message_id) DO NOTHING;
                        """);
                insertMessage.setLong(1, Long.valueOf(messageId));
                insertMessage.setLong(2, event.getChannel().getIdLong());
                insertMessage.executeUpdate();
                insertMessage.close();

                PreparedStatement insertEvent = conn.prepareStatement("""
                        INSERT INTO message_event (fk_user, fk_message, timestamp, action)
                        VALUES (?, ?, ?, ?);
                        """);
                insertEvent.setLong(1, userId);
                insertEvent.setLong(2, Long.valueOf(messageId));
                insertEvent.setLong(3, now.toEpochSecond());
                insertEvent.setString(4, "delete");
                insertEvent.executeUpdate();
                insertEvent.close();
            } catch (SQLException e) {
                Messaging.logException("Database", "insertMessageBulkDeleteEvent", e);
            }
        }
    }

    /**
     * Store user, channel, message, attachment, and event records
     * @param event
     */
    public static void insertMessageUpdateEvent(MessageUpdateEvent event) {
        Connection conn = HifumiBot.getSelf().getSQLite().getConnection();

        try {
            PreparedStatement insertUser = conn.prepareStatement("""
                    INSERT INTO user (discord_id, created_datetime, username)
                    VALUES (?, ?, ?)
                    ON CONFLICT (discord_id) DO NOTHING;
                    """);
            insertUser.setLong(1, event.getAuthor().getIdLong());
            insertUser.setLong(2, event.getAuthor().getTimeCreated().toEpochSecond());
            insertUser.setString(3, event.getAuthor().getName());
            insertUser.executeUpdate();
            insertUser.close();

            PreparedStatement insertChannel = conn.prepareStatement("""
                    INSERT INTO channel (discord_id, name)
                    VALUES (?, ?)
                    ON CONFLICT (discord_id) DO NOTHING;
                    """);
            insertChannel.setLong(1, event.getChannel().getIdLong());
            insertChannel.setString(2, event.getChannel().getName());
            insertChannel.executeUpdate();
            insertChannel.close();

            PreparedStatement insertMessage = conn.prepareStatement("""
                    INSERT INTO message (message_id, fk_channel)
                    VALUES (?, ?)
                    ON CONFLICT (message_id) DO NOTHING;
                    """);
            insertMessage.setLong(1, event.getMessageIdLong());
            insertMessage.setLong(2, event.getChannel().getIdLong());
            insertMessage.executeUpdate();
            insertMessage.close();

            List<Attachment> attachments = event.getMessage().getAttachments();

            if (!attachments.isEmpty()) {
                PreparedStatement insertAttachment = conn.prepareStatement("""
                        INSERT INTO message_attachment (discord_id, timestamp, fk_message, content_type, proxy_url)
                        VALUES (?, ?, ?, ?, ?)
                        ON CONFLICT (discord_id) DO NOTHING;
                        """);

                for (Attachment attachment : attachments) {
                    insertAttachment.setLong(1, attachment.getIdLong());
                    insertAttachment.setLong(2, attachment.getTimeCreated().toEpochSecond());
                    insertAttachment.setLong(3, event.getMessageIdLong());
                    insertAttachment.setString(4, attachment.getContentType());
                    insertAttachment.setString(5, attachment.getProxyUrl());
                    insertAttachment.addBatch();
                }
                
                insertAttachment.executeBatch();
                insertAttachment.close();
            }

            PreparedStatement insertEvent = conn.prepareStatement("""
                    INSERT INTO message_event (fk_user, fk_message, timestamp, action, content)
                    VALUES (?, ?, ?, ?, ?);
                    """);
            insertEvent.setLong(1, event.getAuthor().getIdLong());
            insertEvent.setLong(2, event.getMessageIdLong());
            insertEvent.setLong(3, (event.getMessage().getTimeEdited() != null ? event.getMessage().getTimeEdited() : event.getMessage().getTimeCreated()).toEpochSecond());
            insertEvent.setString(4, "edit");
            insertEvent.setString(5, event.getMessage().getContentRaw());
            insertEvent.executeUpdate();
            insertEvent.close();
        } catch (SQLException e) {
             Messaging.logException("Database", "insertMessageUpdateEvent", e);
        }
    }

    public static MessageObject getOriginalMessage(String messageId) {
        return Database.getOriginalMessage(Long.valueOf(messageId));
    }

    public static MessageObject getOriginalMessage(long messageIdLong) {
        MessageObject ret = null;
        Connection conn = HifumiBot.getSelf().getSQLite().getConnection();

        try {
            // First get the original sent message
            PreparedStatement getSendEvent = conn.prepareStatement("""
                    SELECT
                        e.id, e.fk_user, e.fk_message, e.content, e.timestamp,
                        m.fk_channel, m.jump_link, m.fk_reply_to_message
                    FROM message_event AS e
                    INNER JOIN message AS m ON e.fk_message = m.message_id
                    WHERE e.fk_message = ?
                    AND e.action = 'send'
                    ORDER BY m.timestamp ASC
                    LIMIT 1;
                    """);
            getSendEvent.setLong(1, messageIdLong);
            ResultSet originalSendEvent = getSendEvent.executeQuery();

            // If we got a hit...
            if (originalSendEvent.next()) {
                // ... then look for attachments
                PreparedStatement getAttachments = conn.prepareStatement("""
                        SELECT discord_id, timestamp, fk_message, content_type, proxy_url, filename
                        FROM message_attachment
                        WHERE fk_message = ?;
                        """);
                getAttachments.setLong(1, messageIdLong);
                ResultSet attachments = getAttachments.executeQuery();

                ArrayList<AttachmentObject> attachmentList = new ArrayList<AttachmentObject>();

                while (attachments.next()) {
                    AttachmentObject attachment = new AttachmentObject(
                        String.valueOf(attachments.getLong("discord_id")),
                        DateTimeUtils.longToOffsetDateTime(attachments.getLong("created")), 
                        String.valueOf(messageIdLong),
                        attachments.getString("filename"),
                        attachments.getString("content_type"),
                        attachments.getString("proxy_url")
                    );

                    attachmentList.add(attachment);
                }

                ret = new MessageObject(
                    messageIdLong, 
                    originalSendEvent.getLong("fk_user"), 
                    DateTimeUtils.longToOffsetDateTime(originalSendEvent.getLong("timestamp")), 
                    null, 
                    originalSendEvent.getLong("fk_channel"),
                    originalSendEvent.getString("content"),
                    originalSendEvent.getString("jump_link"), 
                    originalSendEvent.getString("fk_reply_to_message"), 
                    attachmentList
                );
            }

            getSendEvent.close();
        } catch (SQLException e) {
            Messaging.logException("Database", "getOriginalMessage", e);
        }
        
        return ret;
    }

    public static MessageObject getLatestMessage(String messageId) {
        return Database.getLatestMessage(Long.valueOf(messageId));
    }

    public static MessageObject getLatestMessage(long messageIdLong) {
        MessageObject ret = null;
        Connection conn = HifumiBot.getSelf().getSQLite().getConnection();

        try {
            // First get the latest revision of the message
            PreparedStatement getMessageEvent = conn.prepareStatement("""
                    SELECT
                        e.id, e.fk_user, e.fk_message, e.content, e.timestamp, e.action,
                        m.fk_channel, m.jump_link, m.fk_reply_to_message
                    FROM message_event AS e
                    INNER JOIN message AS m ON e.fk_message = m.message_id
                    WHERE e.fk_message = ?
                    AND (
                        e.action = 'send'
                        OR e.action = 'edit'
                    )
                    ORDER BY e.timestamp DESC
                    LIMIT 1;
                    """);
            getMessageEvent.setLong(1, messageIdLong);
            ResultSet latestEvent = getMessageEvent.executeQuery();

            // If we got a hit...
            if (latestEvent.next()) {
                // ... then look for attachments
                PreparedStatement getAttachments = conn.prepareStatement("""
                        SELECT discord_id, timestamp, fk_message, content_type, proxy_url, filename
                        FROM message_attachment
                        WHERE fk_message = ?;
                        """);
                getAttachments.setLong(1, messageIdLong);
                ResultSet attachments = getAttachments.executeQuery();

                ArrayList<AttachmentObject> attachmentList = new ArrayList<AttachmentObject>();

                while (attachments.next()) {
                    AttachmentObject attachment = new AttachmentObject(
                        String.valueOf(attachments.getLong("discord_id")),
                        DateTimeUtils.longToOffsetDateTime(attachments.getLong("timestamp")), 
                        String.valueOf(messageIdLong),
                        attachments.getString("filename"),
                        attachments.getString("content_type"),
                        attachments.getString("proxy_url")
                    );

                    attachmentList.add(attachment);
                }

                String action = latestEvent.getString("action");

                ret = new MessageObject(
                    messageIdLong, 
                    latestEvent.getLong("fk_user"), 
                    DateTimeUtils.longToOffsetDateTime(latestEvent.getLong("timestamp")),
                    (action != null && action.equals("edit") ? DateTimeUtils.longToOffsetDateTime(latestEvent.getLong("timestamp")) : null), 
                    latestEvent.getLong("fk_channel"),
                    latestEvent.getString("content"),
                    latestEvent.getString("jump_link"), 
                    latestEvent.getString("fk_reply_to_message"), 
                    attachmentList
                );
            }

            getMessageEvent.close();
        } catch (SQLException e) {
            Messaging.logException("Database", "getLatestMessage", e);
        }
        
        return ret;
    }

    public static ArrayList<MessageObject> getAllMessageRevisions(long messageIdLong) {
        ArrayList<MessageObject> ret = new ArrayList<MessageObject>();
        Connection conn = HifumiBot.getSelf().getSQLite().getConnection();

        try {
            // First get the latest revision of the message
            PreparedStatement getMessageEvent = conn.prepareStatement("""
                    SELECT
                        e.id, e.fk_user, e.fk_message, e.content, e.timestamp AS e_timestamp, e.action,
                        m.fk_channel, m.jump_link, m.fk_reply_to_message, m.timestamp AS m_timestamp
                    FROM message_event AS e
                    INNER JOIN message AS m ON e.fk_message = m.message_id
                    WHERE e.fk_message = ?
                    AND (
                        e.action = 'send'
                        OR e.action = 'edit'
                    )
                    ORDER BY e.timestamp DESC;
                    """);
            getMessageEvent.setLong(1, messageIdLong);
            ResultSet latestEvent = getMessageEvent.executeQuery();

            // If we got a hit...
            while (latestEvent.next()) {
                // ... then look for attachments
                PreparedStatement getAttachments = conn.prepareStatement("""
                        SELECT discord_id, timestamp, fk_message, content_type, proxy_url, filename
                        FROM message_attachment
                        WHERE fk_message = ?;
                        """);
                getAttachments.setLong(1, messageIdLong);
                ResultSet attachments = getAttachments.executeQuery();

                ArrayList<AttachmentObject> attachmentList = new ArrayList<AttachmentObject>();

                while (attachments.next()) {
                    AttachmentObject attachment = new AttachmentObject(
                        String.valueOf(attachments.getLong("discord_id")),
                        DateTimeUtils.longToOffsetDateTime(attachments.getLong("timestamp")), 
                        String.valueOf(messageIdLong),
                        attachments.getString("filename"),
                        attachments.getString("content_type"),
                        attachments.getString("proxy_url")
                    );

                    attachmentList.add(attachment);
                }

                String action = latestEvent.getString("action");

                ret.add(new MessageObject(
                    messageIdLong, 
                    latestEvent.getLong("fk_user"), 
                    DateTimeUtils.longToOffsetDateTime(latestEvent.getLong("m_timestamp")),
                    (action != null && action.equals("edit") ? DateTimeUtils.longToOffsetDateTime(latestEvent.getLong("e_timestamp")) : null), 
                    latestEvent.getLong("fk_channel"),
                    latestEvent.getString("content"),
                    latestEvent.getString("jump_link"), 
                    latestEvent.getString("fk_reply_to_message"), 
                    attachmentList
                ));
            }

            getMessageEvent.close();
        } catch (SQLException e) {
            Messaging.logException("Database", "getAllMessageRevisions", e);
        }
        
        return ret;
    }

    public static ArrayList<MessageObject> getIdenticalMessagesSinceTime(String contentRaw, long timestamp) {
        ArrayList<MessageObject> ret = new ArrayList<MessageObject>();
        Connection conn = HifumiBot.getSelf().getSQLite().getConnection();

        try {
            // First get the latest revision of the message
            PreparedStatement getMessageEvents = conn.prepareStatement("""
                    SELECT
                        e.id, e.fk_user, e.fk_message, e.content, e.timestamp,
                        m.fk_channel, m.jump_link, m.fk_reply_to_message
                    FROM message_event AS e
                    INNER JOIN message AS m ON e.fk_message = m.message_id
                    WHERE e.content = ?
                    AND e.action = 'send'
                    AND e.timestamp >= ?
                    AND e.fk_message NOT IN (
                        SELECT fk_message
                        FROM message_event
                        WHERE fk_message = e.fk_message
                        AND action = 'delete'
                    )
                    ORDER BY e.timestamp DESC;
                    """);
            getMessageEvents.setString(1, contentRaw);
            getMessageEvents.setLong(2, timestamp);
            ResultSet res = getMessageEvents.executeQuery();

            while (res.next()) {
                MessageObject messageObj = new MessageObject(
                    res.getLong("fk_message"),
                    res.getLong("fk_user"),
                    DateTimeUtils.longToOffsetDateTime(res.getLong("timestamp")),
                    null,
                    res.getLong("fk_channel"),
                    res.getString("content"),
                    res.getString("jump_link"),
                    null,
                    null
                );

                ret.add(messageObj);
            }

            getMessageEvents.close();
            return ret;
        } catch (SQLException e) {
            Messaging.logException("Database", "getIdenticalMessagesSinceTime", e);
        }
        
        return ret;
    }

    public static ArrayList<MessageObject> getAllMessagesSinceTime(long userIdLong, long timestamp) {
        ArrayList<MessageObject> ret = new ArrayList<MessageObject>();
        Connection conn = HifumiBot.getSelf().getSQLite().getConnection();

        try {
            // First get the latest revision of the message
            PreparedStatement getMessageEvents = conn.prepareStatement("""
                    SELECT
                        e.id, e.fk_user, e.fk_message, e.content, e.timestamp,
                        m.fk_channel, m.jump_link, m.fk_reply_to_message
                    FROM message_event AS e
                    INNER JOIN message AS m ON e.fk_message = m.message_id
                    WHERE e.fk_user = ?
                    AND e.action = 'send'
                    AND e.timestamp >= ?
                    ORDER BY e.timestamp DESC;
                    """);
            getMessageEvents.setLong(1, userIdLong);
            getMessageEvents.setLong(2, timestamp);
            ResultSet res = getMessageEvents.executeQuery();

            while (res.next()) {
                MessageObject messageObj = new MessageObject(
                    res.getLong("fk_message"),
                    res.getLong("fk_user"),
                    DateTimeUtils.longToOffsetDateTime(res.getLong("timestamp")),
                    null,
                    res.getLong("fk_channel"),
                    res.getString("content"),
                    res.getString("jump_link"),
                    null,
                    null
                );

                ret.add(messageObj);
            }

            getMessageEvents.close();
            return ret;
        } catch (SQLException e) {
            Messaging.logException("Database", "getAllMessagesSinceTime", e);
        }
        
        return ret;
    }

    public static boolean insertWarezEvent(WarezEventObject warezEvent) {
        Connection conn = HifumiBot.getSelf().getSQLite().getConnection();

        try {            User usr = HifumiBot.getSelf().getJDA().getUserById(warezEvent.getUserId());

            PreparedStatement insertUser = conn.prepareStatement("""
                    INSERT INTO user (discord_id, created_datetime, username)
                    VALUES (?, ?, ?)
                    ON CONFLICT (discord_id) DO NOTHING;
                    """);
            insertUser.setLong(1, warezEvent.getUserId());
            insertUser.setLong(2, usr.getTimeCreated().toEpochSecond());
            insertUser.setString(3, usr.getName());
            insertUser.executeUpdate();
            insertUser.close();

            PreparedStatement insertWarez = conn.prepareStatement("""
                    INSERT INTO warez_event (timestamp, fk_user, action)
                    VALUES (?, ?, ?);
                    """);
            insertWarez.setLong(1, warezEvent.getTimestamp());
            insertWarez.setLong(2, warezEvent.getUserId());
            insertWarez.setString(3, warezEvent.getAction().toString().toLowerCase());
            insertWarez.executeUpdate();
            insertWarez.close();

            return true;
        } catch (SQLException e) {
            Messaging.logException("Database", "insertWarezEvent", e);
        }

        return false;
    }

    public static WarezEventObject getLatestWarezAction(long userIdLong) {
        WarezEventObject ret = null;
        Connection conn = HifumiBot.getSelf().getSQLite().getConnection();

        try {
            PreparedStatement getWarezEvent = conn.prepareStatement("""
                    SELECT timestamp, fk_user, action
                    FROM warez_event
                    WHERE fk_user = ?
                    ORDER BY timestamp DESC
                    LIMIT 1;
                    """);
            getWarezEvent.setLong(1, userIdLong);
            ResultSet latestEvent = getWarezEvent.executeQuery();

            if (latestEvent.next()) {
                ret = new WarezEventObject(
                    latestEvent.getLong("timestamp"), 
                    latestEvent.getLong("fk_user"), 
                    WarezEventObject.Action.valueOf(latestEvent.getString("action").toUpperCase())
                );
            }

            getWarezEvent.close();
        } catch (SQLException e) {
            Messaging.logException("Database", "getLatestWarezAction", e);
        }
        
        return ret;
    }

    public static ArrayList<WarezChartData> getWarezAssignmentsSince(String timeUnit, long length) {
        ArrayList<WarezChartData> ret = new ArrayList<WarezChartData>();
        Connection conn = HifumiBot.getSelf().getSQLite().getConnection();
        long epochSeconds = TimeUtils.getEpochSecondStartOfUnit(timeUnit, length);
        String formatStr = TimeUtils.getSQLFormatStringFromTimeUnit(timeUnit);
        
        try {
            PreparedStatement getWarezEvent = conn.prepareStatement("""
                    SELECT COUNT(timestamp) AS events, STRFTIME(?, DATETIME(timestamp, 'unixepoch')) AS timeUnit, action
                    FROM warez_event
                    WHERE timestamp >= ?
                    GROUP BY STRFTIME(?, DATETIME(timestamp, 'unixepoch')), action
                    ORDER BY action ASC, timestamp ASC;
                    """);
            getWarezEvent.setString(1, formatStr);
            getWarezEvent.setLong(2, epochSeconds);
            getWarezEvent.setString(3, formatStr);
            ResultSet latestEvent = getWarezEvent.executeQuery();

            while (latestEvent.next()) {
                WarezChartData data = new WarezChartData();
                data.timeUnit = latestEvent.getString("timeUnit");
                data.events = latestEvent.getInt("events");
                data.action = latestEvent.getString("action");
                ret.add(data);
            }

            getWarezEvent.close();
        } catch (SQLException e) {
            Messaging.logException("Database", "getLatestWarezAction", e);
        }
        
        return ret;
    }

    public static void insertMemberJoinEvent(GuildMemberJoinEvent event) {
        Connection conn = HifumiBot.getSelf().getSQLite().getConnection();

        try {            
            PreparedStatement insertUser = conn.prepareStatement("""
                    INSERT INTO user (discord_id, created_datetime, username)
                    VALUES (?, ?, ?)
                    ON CONFLICT (discord_id) DO NOTHING;
                    """);
            insertUser.setLong(1, event.getMember().getIdLong());
            insertUser.setLong(2, event.getMember().getTimeCreated().toEpochSecond());
            insertUser.setString(3, event.getUser().getName());
            insertUser.executeUpdate();
            insertUser.close();

            PreparedStatement insertEvent = conn.prepareStatement("""
                    INSERT INTO member_event (timestamp, fk_user, action)
                    VALUES (?, ?, ?);
                    """);
            insertEvent.setLong(1, event.getGuild().retrieveMemberById(event.getMember().getId()).complete().getTimeJoined().toEpochSecond());
            insertEvent.setLong(2, event.getMember().getIdLong());
            insertEvent.setString(3, "join");
            insertEvent.executeUpdate();
            insertEvent.close();
        } catch (SQLException e) {
            Messaging.logException("Database", "insertMemberJoinEvent", e);
        }
    }

    public static ArrayList<MemberEventObject> getRecentMemberEvents(long userId) {
        ArrayList<MemberEventObject> ret = new ArrayList<MemberEventObject>();
        Connection conn = HifumiBot.getSelf().getSQLite().getConnection();

        try {
            PreparedStatement events = conn.prepareStatement("""
                    SELECT timestamp, fk_user, action
                    FROM member_event
                    WHERE fk_user = ?
                    ORDER BY timestamp DESC
                    LIMIT 10;
                    """);
            events.setLong(1, userId);
            ResultSet eventsRes = events.executeQuery();

            while (eventsRes.next()) {
                MemberEventObject event = new MemberEventObject(
                    eventsRes.getLong("timestamp"), 
                    eventsRes.getLong("fk_user"), 
                    MemberEventObject.Action.valueOf(eventsRes.getString("action").toUpperCase())
                );
                ret.add(event);
            }

            events.close();
        } catch (SQLException e) {
            Messaging.logException("Database", "getRecentMemberEvents", e);
        }

        return ret;
    }

    public static ArrayList<MemberChartData> getMemberEventsSince(String timeUnit, long length) {
        ArrayList<MemberChartData> ret = new ArrayList<MemberChartData>();
        Connection conn = HifumiBot.getSelf().getSQLite().getConnection();
        long epochSeconds = TimeUtils.getEpochSecondStartOfUnit(timeUnit, length);
        String formatStr = TimeUtils.getSQLFormatStringFromTimeUnit(timeUnit);

        try {
            PreparedStatement events = conn.prepareStatement("""
                    SELECT COUNT(timestamp) AS events, STRFTIME(?, DATETIME(timestamp, 'unixepoch')) AS timeUnit, action
                    FROM member_event
                    WHERE timestamp >= ?
                    GROUP BY STRFTIME(?, DATETIME(timestamp, 'unixepoch')), action
                    ORDER BY CASE
                        WHEN action = "join" THEN 1
                        WHEN action = "leave" THEN 2
                        WHEN action = "ban" THEN 3
                        END ASC,
                        timestamp ASC;
                    """);
            events.setString(1, formatStr);
            events.setLong(2, epochSeconds);
            events.setString(3, formatStr);
            ResultSet eventsRes = events.executeQuery();

            while (eventsRes.next()) {
                MemberChartData data = new MemberChartData();
                data.timeUnit = eventsRes.getString("timeUnit");
                data.events = eventsRes.getInt("events");
                data.action = eventsRes.getString("action");
                ret.add(data);
            }

            events.close();
        } catch (SQLException e) {
            Messaging.logException("Database", "getMemberEventsSince", e);
        }

        return ret;
    }

    public static void insertMemberRemoveEvent(GuildMemberRemoveEvent event, OffsetDateTime time) {
        Connection conn = HifumiBot.getSelf().getSQLite().getConnection();

        try {            
            PreparedStatement insertUser = conn.prepareStatement("""
                    INSERT INTO user (discord_id, created_datetime, username)
                    VALUES (?, ?, ?)
                    ON CONFLICT (discord_id) DO NOTHING;
                    """);
            insertUser.setLong(1, event.getUser().getIdLong());
            insertUser.setLong(2, event.getUser().getTimeCreated().toEpochSecond());
            insertUser.setString(3, event.getUser().getName());
            insertUser.executeUpdate();
            insertUser.close();

            PreparedStatement insertEvent = conn.prepareStatement("""
                    INSERT INTO member_event (timestamp, fk_user, action)
                    VALUES (?, ?, ?);
                    """);
            insertEvent.setLong(1, time.toEpochSecond());
            insertEvent.setLong(2, event.getUser().getIdLong());
            insertEvent.setString(3, "leave");
            insertEvent.executeUpdate();
            insertEvent.close();
        } catch (SQLException e) {
            Messaging.logException("Database", "insertMemberRemoveEvent", e);
        }
    }

    public static void insertMemberBanEvent(GuildBanEvent event, OffsetDateTime time) {
        Connection conn = HifumiBot.getSelf().getSQLite().getConnection();

        try {
            PreparedStatement insertUser = conn.prepareStatement("""
                    INSERT INTO user (discord_id, created_datetime, username)
                    VALUES (?, ?, ?) ON CONFLICT (discord_id) DO NOTHING;
                    """);
            insertUser.setLong(1, event.getUser().getIdLong());
            insertUser.setLong(2, event.getUser().getTimeCreated().toEpochSecond());
            insertUser.setString(3, event.getUser().getName());
            insertUser.executeUpdate();
            insertUser.close();

            PreparedStatement insertEvent = conn.prepareStatement("""
                    INSERT INTO member_event (timestamp, fk_user, action)
                    VALUES (?, ?, ?);
                    """);
            insertEvent.setLong(1, time.toEpochSecond());
            insertEvent.setLong(2, event.getUser().getIdLong());
            insertEvent.setString(3, "ban");
            insertEvent.executeUpdate();
            insertEvent.close();
        } catch (SQLException e) {
             Messaging.logException("Database", "insertMemberBanEvent", e);
        }
    }

    /**
     * Insert an automod event to the database.
     * @param automodEvent
     */
    public static void insertAutoModEvent(AutoModExecutionEvent event, OffsetDateTime time) {
        Connection conn = HifumiBot.getSelf().getSQLite().getConnection();

        try {
            PreparedStatement insertAutoModEvent = conn.prepareStatement("""
                    INSERT INTO automod_event (fk_user, fk_message, fk_channel, alert_message_id, rule_id, timestamp, trigger, content, matched_content, matched_keyword, response_type)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                    """);

            insertAutoModEvent.setLong(1, event.getUserIdLong());
            
            if (event.getMessageIdLong() != 0) {
                insertAutoModEvent.setLong(2, event.getMessageIdLong());
            } else {
                insertAutoModEvent.setNull(2, Types.BIGINT);
            }
            
            if (event.getChannel() != null) {
                insertAutoModEvent.setLong(3, event.getChannel().getIdLong());
            } else {
                insertAutoModEvent.setNull(3, Types.BIGINT);
            }
            
            if (event.getAlertMessageIdLong() != 0) {
                insertAutoModEvent.setLong(4, event.getAlertMessageIdLong());
            } else {
                insertAutoModEvent.setNull(4, Types.BIGINT);
            }

            insertAutoModEvent.setLong(5, event.getRuleIdLong());
            insertAutoModEvent.setLong(6, time.toEpochSecond());
            insertAutoModEvent.setString(7, event.getTriggerType().toString());
            insertAutoModEvent.setString(8, event.getContent());
            insertAutoModEvent.setString(9, event.getMatchedContent());
            insertAutoModEvent.setString(10, event.getMatchedKeyword());
            insertAutoModEvent.setString(11, event.getResponse().getType().toString());
            insertAutoModEvent.executeUpdate();
            insertAutoModEvent.close();
        } catch (SQLException e) {
             Messaging.logException("Database", "insertAutoModEvent", e);
        }
    }

    public static ArrayList<AutoModEventObject> getAutoModEventsSinceTime(long userIdLong, OffsetDateTime time) {
        ArrayList<AutoModEventObject> ret = new ArrayList<AutoModEventObject>();
        Connection conn = HifumiBot.getSelf().getSQLite().getConnection();

        try {
            // First get the latest revision of the message
            PreparedStatement getFilterEvent = conn.prepareStatement("""
                    SELECT
                    fk_user, fk_message, fk_channel, alert_message_id, rule_id, timestamp, trigger, content, matched_content, matched_keyword, response_type
                    FROM automod_event
                    WHERE fk_user = ?
                    AND timestamp >= ?
                    AND response_type = ?
                    ORDER BY timestamp DESC;
                    """);
            getFilterEvent.setLong(1, userIdLong);
            getFilterEvent.setLong(2, time.toEpochSecond());
            getFilterEvent.setString(3, AutoModResponse.Type.BLOCK_MESSAGE.toString());
            ResultSet res = getFilterEvent.executeQuery();

            while (res.next()) {
                AutoModEventObject autoModEventObject = new AutoModEventObject(
                    res.getLong("fk_user"),
                    res.getLong("fk_message"),
                    res.getLong("fk_channel"),
                    res.getLong("alert_message_id"),
                    res.getLong("rule_id"),
                    res.getLong("timestamp"),
                    res.getString("trigger"),
                    res.getString("content"),
                    res.getString("matched_content"),
                    res.getString("matched_keyword"),
                    res.getString("response_type")
                );

                ret.add(autoModEventObject);
            }

            getFilterEvent.close();
            return ret;
        } catch (SQLException e) {
            Messaging.logException("Database", "getAutoModEventsSinceTime", e);
        }
        
        return ret;
    }
}
