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
import io.github.redpanda4552.HifumiBot.util.DateTimeUtils;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;

public class Database {

    /**
     * Store user, channel, message, attachment, and event records
     * @param event
     * @return
     */
    public static void insertMessage(Message message) {
        Connection conn = null;

        try {
            conn = HifumiBot.getSelf().getMySQL().getConnection();

            PreparedStatement insertUser = conn.prepareStatement("""
                    INSERT INTO user (discord_id, created_datetime, username)
                    VALUES (?, ?, ?)
                    ON DUPLICATE KEY UPDATE discord_id=discord_id;
                    """);
            insertUser.setLong(1, message.getAuthor().getIdLong());
            insertUser.setLong(2, message.getAuthor().getTimeCreated().toEpochSecond());
            insertUser.setString(3, message.getAuthor().getName());
            insertUser.executeUpdate();
            insertUser.close();

            PreparedStatement insertChannel = conn.prepareStatement("""
                    INSERT INTO channel (discord_id, name)
                    VALUES (?, ?)
                    ON DUPLICATE KEY UPDATE discord_id=discord_id;
                    """);
            insertChannel.setLong(1, message.getChannel().getIdLong());
            insertChannel.setString(2, message.getChannel().getName());
            insertChannel.executeUpdate();
            insertChannel.close();

            // Check if the referenced message exists in the database; if not, try to add it first.
            if (message.getReferencedMessage() != null) {
                if (Database.getLatestMessage(message.getReferencedMessage().getIdLong()) == null) {
                    Database.insertMessage(message.getReferencedMessage());
                }
            }

            PreparedStatement insertMessage = conn.prepareStatement("""
                    INSERT INTO message (message_id, fk_channel, jump_link, fk_reply_to_message, timestamp)
                    VALUES (?, ?, ?, ?, ?);
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
        } catch (SQLException e) {
             Messaging.logException("Database", "insertMessageReceivedEvent", e);
        } finally {
            MySQL.closeConnection(conn);
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
            conn = HifumiBot.getSelf().getMySQL().getConnection();

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
                    ON DUPLICATE KEY UPDATE discord_id=discord_id;
                    """);
            insertChannel.setLong(1, event.getChannel().getIdLong());
            insertChannel.setString(2, event.getChannel().getName());
            insertChannel.executeUpdate();
            insertChannel.close();

            PreparedStatement insertMessage = conn.prepareStatement("""
                    INSERT INTO message (message_id, fk_channel)
                    VALUES (?, ?)
                    ON DUPLICATE KEY UPDATE message_id=message_id;
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
        } finally {
            MySQL.closeConnection(conn);
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
                conn = HifumiBot.getSelf().getMySQL().getConnection();

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
                        ON DUPLICATE KEY UPDATE discord_id=discord_id;
                        """);
                insertChannel.setLong(1, event.getChannel().getIdLong());
                insertChannel.setString(2, event.getChannel().getName());
                insertChannel.executeUpdate();
                insertChannel.close();

                PreparedStatement insertMessage = conn.prepareStatement("""
                        INSERT INTO message (message_id, fk_channel)
                        VALUES (?, ?)
                        ON DUPLICATE KEY UPDATE message_id=message_id;
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
            } finally {
                MySQL.closeConnection(conn);
            }
        }
    }

    /**
     * Store user, channel, message, attachment, and event records
     * @param event
     */
    public static void insertMessageUpdateEvent(MessageUpdateEvent event) {
        Connection conn = null;

        try {
            conn = HifumiBot.getSelf().getMySQL().getConnection();

            PreparedStatement insertUser = conn.prepareStatement("""
                    INSERT INTO user (discord_id, created_datetime, username)
                    VALUES (?, ?, ?)
                    ON DUPLICATE KEY UPDATE discord_id=discord_id;
                    """);
            insertUser.setLong(1, event.getAuthor().getIdLong());
            insertUser.setLong(2, event.getAuthor().getTimeCreated().toEpochSecond());
            insertUser.setString(3, event.getAuthor().getName());
            insertUser.executeUpdate();
            insertUser.close();

            PreparedStatement insertChannel = conn.prepareStatement("""
                    INSERT INTO channel (discord_id, name)
                    VALUES (?, ?)
                    ON DUPLICATE KEY UPDATE discord_id=discord_id;
                    """);
            insertChannel.setLong(1, event.getChannel().getIdLong());
            insertChannel.setString(2, event.getChannel().getName());
            insertChannel.executeUpdate();
            insertChannel.close();

            PreparedStatement insertMessage = conn.prepareStatement("""
                    INSERT INTO message (message_id, fk_channel)
                    VALUES (?, ?)
                    ON DUPLICATE KEY UPDATE message_id=message_id;
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
                        ON DUPLICATE KEY UPDATE discord_id=discord_id;
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
        } finally {
            MySQL.closeConnection(conn);
        }
    }

    public static MessageObject getOriginalMessage(String messageId) {
        return Database.getOriginalMessage(Long.valueOf(messageId));
    }

    public static MessageObject getOriginalMessage(long messageIdLong) {
        MessageObject ret = null;
        Connection conn = null;

        try {
            conn = HifumiBot.getSelf().getMySQL().getConnection();

            // First get the original sent message
            PreparedStatement getSendEvent = conn.prepareStatement("""
                    SELECT
                        e.id, e.fk_user, e.fk_message, e.content,
                        m.fk_channel, m.jump_link, m.fk_reply_to_message, m.timestamp
                    FROM message_event e
                    INNER JOIN message m ON e.fk_message = m.message_id
                    WHERE fk_message = ?
                    AND action = 'send'
                    ORDER BY timestamp DESC
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
                    originalSendEvent.getLong("e.fk_user"), 
                    DateTimeUtils.longToOffsetDateTime(originalSendEvent.getLong("m.timestamp")), 
                    null, 
                    originalSendEvent.getLong("m.fk_channel"),
                    originalSendEvent.getString("e.content"),
                    originalSendEvent.getString("m.jump_link"), 
                    originalSendEvent.getString("m.fk_reply_to_message"), 
                    attachmentList
                );
            }

            getSendEvent.close();
        } catch (SQLException e) {
            Messaging.logException("Database", "getOriginalMessage", e);
        } finally {
            MySQL.closeConnection(conn);
        }
        
        return ret;
    }

    public static MessageObject getLatestMessage(String messageId) {
        return Database.getLatestMessage(Long.valueOf(messageId));
    }

    public static MessageObject getLatestMessage(long messageIdLong) {
        MessageObject ret = null;
        Connection conn = null;

        try {
            conn = HifumiBot.getSelf().getMySQL().getConnection();

            // First get the latest revision of the message
            PreparedStatement getMessageEvent = conn.prepareStatement("""
                    SELECT
                        e.id, e.fk_user, e.fk_message, e.content, e.timestamp, e.action,
                        m.fk_channel, m.jump_link, m.fk_reply_to_message, m.timestamp
                    FROM message_event e
                    INNER JOIN message m ON e.fk_message = m.message_id
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

                String action = latestEvent.getString("e.action");

                ret = new MessageObject(
                    messageIdLong, 
                    latestEvent.getLong("e.fk_user"), 
                    DateTimeUtils.longToOffsetDateTime(latestEvent.getLong("m.timestamp")),
                    (action != null && action.equals("edit") ? DateTimeUtils.longToOffsetDateTime(latestEvent.getLong("e.timestamp")) : null), 
                    latestEvent.getLong("m.fk_channel"),
                    latestEvent.getString("e.content"),
                    latestEvent.getString("m.jump_link"), 
                    latestEvent.getString("m.fk_reply_to_message"), 
                    attachmentList
                );
            }

            getMessageEvent.close();
        } catch (SQLException e) {
            Messaging.logException("Database", "getLatestMessage", e);
        } finally {
            MySQL.closeConnection(conn);
        }
        
        return ret;
    }

    public static ArrayList<MessageObject> getAllMessageRevisions(long messageIdLong) {
        ArrayList<MessageObject> ret = new ArrayList<MessageObject>();
        Connection conn = null;

        try {
            conn = HifumiBot.getSelf().getMySQL().getConnection();

            // First get the latest revision of the message
            PreparedStatement getMessageEvent = conn.prepareStatement("""
                    SELECT
                        e.id, e.fk_user, e.fk_message, e.content, e.timestamp, e.action,
                        m.fk_channel, m.jump_link, m.fk_reply_to_message, m.timestamp
                    FROM message_event e
                    INNER JOIN message m ON e.fk_message = m.message_id
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

                String action = latestEvent.getString("e.action");

                ret.add(new MessageObject(
                    messageIdLong, 
                    latestEvent.getLong("e.fk_user"), 
                    DateTimeUtils.longToOffsetDateTime(latestEvent.getLong("m.timestamp")),
                    (action != null && action.equals("edit") ? DateTimeUtils.longToOffsetDateTime(latestEvent.getLong("e.timestamp")) : null), 
                    latestEvent.getLong("m.fk_channel"),
                    latestEvent.getString("e.content"),
                    latestEvent.getString("m.jump_link"), 
                    latestEvent.getString("m.fk_reply_to_message"), 
                    attachmentList
                ));
            }

            getMessageEvent.close();
        } catch (SQLException e) {
            Messaging.logException("Database", "getAllMessageRevisions", e);
        } finally {
            MySQL.closeConnection(conn);
        }
        
        return ret;
    }

    /**
     * Insert a filter event to the database. It is assumed the message and user are already present.
     * @param filterEvent
     */
    public static void insertFilterEvent(FilterEventObject filterEvent) {
        Connection conn = null;

        try {
            conn = HifumiBot.getSelf().getMySQL().getConnection();

            PreparedStatement insertFilterEvent = conn.prepareStatement("""
                    INSERT INTO filter_event (fk_user, fk_message, timestamp, filter_name, filter_regex_name, informational)
                    VALUES (?, ?, ?, ?, ?, ?);
                    """);
            insertFilterEvent.setLong(1, filterEvent.getUserId());
            insertFilterEvent.setLong(2, filterEvent.getMessageId());
            insertFilterEvent.setLong(3, filterEvent.getTimestamp());
            insertFilterEvent.setString(4, filterEvent.getFilterName());
            insertFilterEvent.setString(5, filterEvent.getFilterRegexName());
            insertFilterEvent.setBoolean(6, filterEvent.isInformational());
            insertFilterEvent.executeUpdate();
            insertFilterEvent.close();
        } catch (SQLException e) {
             Messaging.logException("Database", "insertFilterEvent", e);
        } finally {
            MySQL.closeConnection(conn);
        }
    }

    public static ArrayList<FilterEventObject> getFilterEventsSinceTime(long userIdLong, long timestamp) {
        ArrayList<FilterEventObject> ret = new ArrayList<FilterEventObject>();
        Connection conn = null;

        try {
            conn = HifumiBot.getSelf().getMySQL().getConnection();

            // First get the latest revision of the message
            PreparedStatement getFilterEvent = conn.prepareStatement("""
                    SELECT
                        f.fk_user, f.fk_message, f.timestamp, f.filter_name, f.filter_regex_name, f.informational
                    FROM filter_event f
                    WHERE f.fk_user = ?
                    AND f.timestamp >= ?
                    AND f.informational = FALSE
                    ORDER BY f.timestamp DESC;
                    """);
            getFilterEvent.setLong(1, userIdLong);
            getFilterEvent.setLong(2, timestamp);
            ResultSet res = getFilterEvent.executeQuery();

            while (res.next()) {
                FilterEventObject filterEvent = new FilterEventObject(
                    res.getLong("f.fk_user"),
                    res.getLong("f.fk_message"),
                    res.getLong("f.timestamp"),
                    res.getString("f.filter_name"),
                    res.getString("f.filter_regex_name"),
                    res.getBoolean("f.informational")
                );

                ret.add(filterEvent);
            }

            getFilterEvent.close();
            return ret;
        } catch (SQLException e) {
            Messaging.logException("Database", "getFilterEventsSinceTime", e);
        } finally {
            MySQL.closeConnection(conn);
        }
        
        return ret;
    }

    public static ArrayList<MessageObject> getIdenticalMessagesSinceTime(String contentRaw, long timestamp) {
        ArrayList<MessageObject> ret = new ArrayList<MessageObject>();
        Connection conn = null;

        try {
            conn = HifumiBot.getSelf().getMySQL().getConnection();

            // First get the latest revision of the message
            PreparedStatement getMessageEvents = conn.prepareStatement("""
                    SELECT
                        e.id, e.fk_user, e.fk_message, e.content, e.timestamp,
                        m.fk_channel, m.jump_link, m.fk_reply_to_message, m.timestamp, m.fk_user
                    FROM message_event e
                    INNER JOIN message m ON e.fk_message = m.message_id
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
                    res.getLong("e.fk_message"),
                    res.getLong("m.fk_user"),
                    DateTimeUtils.longToOffsetDateTime(res.getLong("e.timestamp")),
                    null,
                    res.getLong("m.fk_channel"),
                    res.getString("e.content"),
                    res.getString("m.jump_link"),
                    null,
                    null
                );

                ret.add(messageObj);
            }

            getMessageEvents.close();
            return ret;
        } catch (SQLException e) {
            Messaging.logException("Database", "getIdenticalMessagesSinceTime", e);
        } finally {
            MySQL.closeConnection(conn);
        }
        
        return ret;
    }

    public static ArrayList<MessageObject> getAllMessagesSinceTime(long userIdLong, long timestamp) {
        ArrayList<MessageObject> ret = new ArrayList<MessageObject>();
        Connection conn = null;

        try {
            conn = HifumiBot.getSelf().getMySQL().getConnection();

            // First get the latest revision of the message
            PreparedStatement getMessageEvents = conn.prepareStatement("""
                    SELECT
                        e.id, e.fk_user, e.fk_message, e.content, e.timestamp,
                        m.fk_channel, m.jump_link, m.fk_reply_to_message, m.timestamp, m.fk_user
                    FROM message_event e
                    INNER JOIN message m ON e.fk_message = m.message_id
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
                    res.getLong("e.fk_message"),
                    res.getLong("m.fk_user"),
                    DateTimeUtils.longToOffsetDateTime(res.getLong("e.timestamp")),
                    null,
                    res.getLong("m.fk_channel"),
                    res.getString("e.content"),
                    res.getString("m.jump_link"),
                    null,
                    null
                );

                ret.add(messageObj);
            }

            getMessageEvents.close();
            return ret;
        } catch (SQLException e) {
            Messaging.logException("Database", "getAllMessagesSinceTime", e);
        } finally {
            MySQL.closeConnection(conn);
        }
        
        return ret;
    }

    public static boolean insertWarezEvent(WarezEventObject warezEvent) {
        Connection conn = null;

        try {
            conn = HifumiBot.getSelf().getMySQL().getConnection();
            User usr = HifumiBot.getSelf().getJDA().getUserById(warezEvent.getUserId());

            PreparedStatement insertUser = conn.prepareStatement("""
                    INSERT INTO user (discord_id, created_datetime, username)
                    VALUES (?, ?, ?)
                    ON DUPLICATE KEY UPDATE discord_id=discord_id;
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
        } finally {
            MySQL.closeConnection(conn);
        }

        return false;
    }

    public static WarezEventObject getLatestWarezAction(long userIdLong) {
        WarezEventObject ret = null;
        Connection conn = null;

        try {
            conn = HifumiBot.getSelf().getMySQL().getConnection();

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
        } finally {
            MySQL.closeConnection(conn);
        }
        
        return ret;
    }

    public static void insertMemberJoinEvent(GuildMemberJoinEvent event) {
        Connection conn = null;

        try {
            conn = HifumiBot.getSelf().getMySQL().getConnection();
            
            PreparedStatement insertUser = conn.prepareStatement("""
                    INSERT INTO user (discord_id, created_datetime, username)
                    VALUES (?, ?, ?)
                    ON DUPLICATE KEY UPDATE discord_id=discord_id;
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
        } finally {
            MySQL.closeConnection(conn);
        }
    }

    public static ArrayList<MemberEventObject> getRecentMemberEvents(long userId) {
        ArrayList<MemberEventObject> ret = new ArrayList<MemberEventObject>();
        Connection conn = null;

        try {
            conn = HifumiBot.getSelf().getMySQL().getConnection();
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
        } finally {
            MySQL.closeConnection(conn);
        }

        return ret;
    }

    public static void insertMemberRemoveEvent(GuildMemberRemoveEvent event, OffsetDateTime time) {
        Connection conn = null;

        try {
            conn = HifumiBot.getSelf().getMySQL().getConnection();
            
            PreparedStatement insertUser = conn.prepareStatement("""
                    INSERT INTO user (discord_id, created_datetime, username)
                    VALUES (?, ?, ?)
                    ON DUPLICATE KEY UPDATE discord_id=discord_id;
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
        } finally {
            MySQL.closeConnection(conn);
        }
    }

    public static void insertMemberBanEvent(GuildBanEvent event, OffsetDateTime time) {
        Connection conn = null;

        try {
            conn = HifumiBot.getSelf().getMySQL().getConnection();

            PreparedStatement insertUser = conn.prepareStatement("""
                    INSERT INTO user (discord_id, created_datetime, username)
                    VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE discord_id=discord_id;
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
        } finally {
            MySQL.closeConnection(conn);
        }
    }
}
