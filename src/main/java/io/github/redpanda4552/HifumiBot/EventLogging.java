package io.github.redpanda4552.HifumiBot;

import java.awt.Color;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;

import io.github.redpanda4552.HifumiBot.database.AttachmentObject;
import io.github.redpanda4552.HifumiBot.database.Database;
import io.github.redpanda4552.HifumiBot.database.MessageObject;
import io.github.redpanda4552.HifumiBot.database.WarezEventObject;
import io.github.redpanda4552.HifumiBot.database.WarezEventObject.Action;
import io.github.redpanda4552.HifumiBot.util.DateTimeUtils;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public class EventLogging {
    
    public static void logGuildMemberJoinEvent(GuildMemberJoinEvent event, WarezEventObject lastWarezEvent) {
        String channelId = HifumiBot.getSelf().getConfig().channels.logging.memberJoin;

        if (channelId == null || channelId.isBlank()) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();
        Duration diff = Duration.between(event.getUser().getTimeCreated(), now);
        
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.BLUE);
        eb.setTitle("Member Joined");
        eb.setThumbnail(event.getUser().getAvatarUrl());
        
        if (diff.toHours() < 1) {
            eb.appendDescription(":warning: Account appears to be less than an hour old\n");
            eb.setColor(Color.GREEN);
        }

        if (lastWarezEvent != null && lastWarezEvent.getAction().equals(Action.ADD)) {
            OffsetDateTime warezDate = DateTimeUtils.longToOffsetDateTime(lastWarezEvent.getTimestamp());
            String dateStr = warezDate.format(DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm:ss")) + " UTC";
            eb.appendDescription(":pirate_flag: This user was previously warez'd (" + dateStr + ")\n");
            eb.setColor(Color.GREEN);
        }

        eb.addField("Username (As Mention)", event.getUser().getAsMention(), true);
        eb.addField("Username (Plain Text)", event.getUser().getName(), true);
        eb.addField("User ID", event.getUser().getId(), true);
        eb.addField("Account Age", getAgeString(diff), true);
        eb.addField("Current Display Name", event.getMember().getEffectiveName(), true);

        MessageCreateBuilder mb = new MessageCreateBuilder();
        mb.setEmbeds(eb.build());
        /*
        mb.addActionRow(
            Button.of(ButtonStyle.PRIMARY, "timeout:" + retrievedMember.getId(), "Timeout (1 hr)"),
            Button.of(ButtonStyle.SECONDARY, "kick:" + retrievedMember.getId(), "Kick"),
            Button.of(ButtonStyle.DANGER, "ban:" + retrievedMember.getId(), "Ban (And delete msgs from 24 hrs)")
        );
         */
        Messaging.sendMessage(channelId, mb.build());
    }

    public static void logGuildMemberRemoveEvent(GuildMemberRemoveEvent event) {
        String channelId = HifumiBot.getSelf().getConfig().channels.logging.memberLeave;

        if (channelId == null || channelId.isBlank()) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();
        Duration diff = Duration.between(event.getUser().getTimeCreated(), now);

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.ORANGE);
        eb.setTitle("Member Left");
        eb.setThumbnail(event.getUser().getAvatarUrl());
        eb.addField("Username (As Mention)", event.getUser().getAsMention(), true);
        eb.addField("Username (Plain Text)", event.getUser().getName(), true);
        eb.addField("User ID", event.getUser().getId(), true);
        eb.addField("Account Age", getAgeString(diff), true);

        if (event.getMember() != null) {
            eb.addField("Current Display Name", event.getMember().getEffectiveName(), true);
        }
        
        MessageCreateBuilder mb = new MessageCreateBuilder();
        mb.setEmbeds(eb.build());
        Messaging.sendMessage(channelId, mb.build());
    }

    public static void logGuildBanEvent(GuildBanEvent event) {
        String channelId = HifumiBot.getSelf().getConfig().channels.logging.memberBan;

        if (channelId == null || channelId.isBlank()) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();
        Duration diff = Duration.between(event.getUser().getTimeCreated(), now);

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.RED);
        eb.setTitle("Member Banned");
        eb.addField("Username (As Mention)", event.getUser().getAsMention(), true);
        eb.addField("Username (Plain Text)", event.getUser().getName(), true);
        eb.addField("User ID", event.getUser().getId(), true);
        eb.addField("Account Age", getAgeString(diff), true);

        MessageCreateBuilder mb = new MessageCreateBuilder();
        mb.setEmbeds(eb.build());
        Messaging.sendMessage(channelId, mb.build());
    }

    public static void logMessageDeleteEvent(MessageObject deletedMessage, String messageId) {
        String channelId = HifumiBot.getSelf().getConfig().channels.logging.messageDelete;

        if (channelId == null || channelId.isBlank()) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();
        Duration diff = null;
        User user = null;
        
        if (deletedMessage != null) {
            diff = Duration.between(deletedMessage.getCreatedTime(), now);

            try {
                user = HifumiBot.getSelf().getJDA().retrieveUserById(deletedMessage.getAuthorId()).complete();
            } catch (Exception e) {
                // Squelch
            }
        }

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.MAGENTA);
        eb.setTitle("Message Deleted");

        if (user != null) {
            eb.addField("Username (As Mention)", user.getAsMention(), true);
            eb.addField("Username (Plain Text)", user.getName(), true);
            eb.addField("User ID", user.getId(), true);
        }

        eb.addField("Message ID", messageId, true);

        if (diff != null) {
            eb.addField("Message Age", getAgeString(diff), true);
        }
        
        if (deletedMessage != null) {
            eb.addField("Channel", HifumiBot.getSelf().getJDA().getTextChannelById(deletedMessage.getChannelId()).getAsMention(), true);
            eb.addField("Message Content (Truncated to 512 chars)", StringUtils.truncate(deletedMessage.getBodyContent(), 512), false);

            if (deletedMessage.getReferencedMessageId() != null) {
                MessageObject referencedMessage = Database.getLatestMessage(deletedMessage.getReferencedMessageId());

                if (referencedMessage != null) {
                    eb.addField("Replied to Message", referencedMessage.getJumpUrl(), false);
                }
            }

            if (!deletedMessage.getAttachments().isEmpty()) {
                StringBuilder sb = new StringBuilder();

                for (AttachmentObject attachment : deletedMessage.getAttachments()) {
                    sb.append(attachment.getProxyUrl()).append("\n");
                }

                eb.addField("Old Attachments", sb.toString().trim(), false);
            }
        } else {
            return;
        }

        MessageCreateBuilder mb = new MessageCreateBuilder();
        mb.setEmbeds(eb.build());
        Messaging.sendMessage(channelId, mb.build());
    }

    public static void logMessageUpdateEvent(MessageUpdateEvent event, MessageObject beforeEditMessage) {
        String channelId = HifumiBot.getSelf().getConfig().channels.logging.messageUpdate;

        if (channelId == null || channelId.isBlank()) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();
        Duration diff = Duration.between(event.getMessage().getTimeCreated(), now);
        User user = event.getAuthor();

        String newContent = event.getMessage().getContentRaw();

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.CYAN);
        eb.setTitle("Message Edited");
        eb.addField("Username (As Mention)", user.getAsMention(), true);
        eb.addField("Username (Plain Text)", user.getName(), true);
        eb.addField("User ID", user.getId(), true);
        eb.addField("Message ID", event.getMessageId(), true);
        eb.addField("Channel", event.getChannel().getAsMention(), true);
        eb.addField("Message Age", getAgeString(diff), true);
        eb.addField("Message Jump Link", event.getJumpUrl(), true);

        if (beforeEditMessage != null) {
            eb.addField("Old Message Content (Truncated to 512 chars)", StringUtils.truncate(beforeEditMessage.getBodyContent(), 512), false);
        } else {
            eb.addField("Old Message Content (Not Found)", "Original message (or its last edit) were not found in database.", false);
        }
        
        eb.addField("New Message Content (Truncated to 512 chars)", StringUtils.truncate(newContent, 512), false);

        if (event.getMessage().getReferencedMessage() != null) {
            eb.addField("Replied to Message", event.getMessage().getReferencedMessage().getJumpUrl(), false);
        }

        if (!event.getMessage().getAttachments().isEmpty()) {
            StringBuilder sb = new StringBuilder();

            for (Attachment attachment : event.getMessage().getAttachments()) {
                sb.append(attachment.getProxyUrl()).append("\n");
            }

            eb.addField("Current Attachments", sb.toString().trim(), false);
        }

        MessageCreateBuilder mb = new MessageCreateBuilder();
        mb.setEmbeds(eb.build());
        Messaging.sendMessage(channelId, mb.build());
    }

    private static String getAgeString(Duration diff) {
        String ageStr = "";

        if (diff.toSeconds() < 60) {
            ageStr = diff.toSeconds() + "s";
        } else if (diff.toMinutes() < 60) {
            ageStr = diff.toMinutes() + "m " + diff.toSecondsPart() + "s";
        } else if (diff.toHours() < 24) {
            ageStr = diff.toHours() + "h " + diff.toMinutesPart() + "m";
        } else {
            ageStr = diff.toDays() + "d " + diff.toHoursPart() + "h";
        }

        return ageStr;
    }
}
