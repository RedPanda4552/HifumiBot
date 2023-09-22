package io.github.redpanda4552.HifumiBot;

import java.awt.Color;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.RowFilter.Entry;

import org.apache.commons.lang3.StringUtils;

import io.github.redpanda4552.HifumiBot.filter.MessageHistoryEntry;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public class EventLogging {
    
    public static void logGuildMemberJoinEvent(GuildMemberJoinEvent event) {
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

        if (HifumiBot.getSelf().getWarezTracking().warezUsers.containsKey(event.getUser().getId())) {
            String dateStr = HifumiBot.getSelf().getWarezTracking().warezUsers.get(event.getUser().getId())
                    .format(DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm:ss")) + " UTC";
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

    public static void logMessageDeleteEvent(MessageHistoryEntry entry) {
        String channelId = HifumiBot.getSelf().getConfig().channels.logging.messageDelete;

        if (channelId == null || channelId.isBlank()) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();
        Duration diff = Duration.between(entry.getDateTime(), now);
        User user = HifumiBot.getSelf().getJDA().getUserById(entry.getUserId());

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.MAGENTA);
        eb.setTitle("Message Deleted");
        eb.addField("Username (As Mention)", user.getAsMention(), true);
        eb.addField("Username (Plain Text)", user.getName(), true);
        eb.addField("User ID", user.getId(), true);
        eb.addField("Channel", HifumiBot.getSelf().getJDA().getTextChannelById(entry.getChannelId()).getAsMention(), true);
        eb.addField("Message Age", getAgeString(diff), true);
        eb.addField("Message Content (Truncated to 512 chars)", StringUtils.truncate(entry.getMessageContent(), 512), false);

        if (entry.referencedMessageLink != null) {
            eb.addField("Replied to Message", entry.referencedMessageLink, false);
        }

        if (!entry.getAttachmentUrls().isEmpty()) {
            StringBuilder sb = new StringBuilder();

            for (String attachmentUrl : entry.attachmentUrls) {
                sb.append(attachmentUrl).append("\n");
            }

            eb.addField("Attachments", sb.toString().trim(), false);
        }

        MessageCreateBuilder mb = new MessageCreateBuilder();
        mb.setEmbeds(eb.build());
        Messaging.sendMessage(channelId, mb.build());
    }

    public static void logMessageDeleteEvent(String channelMention, String messageId) {
        String channelId = HifumiBot.getSelf().getConfig().channels.logging.messageDelete;

        if (channelId == null || channelId.isBlank()) {
            return;
        }

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.MAGENTA);
        eb.setTitle("Message Deleted");
        eb.setDescription("The message could not be located in the message history cache. Typical reasons:\n- Message is too old and was not logged\n- The Discord API did not trigger an event when the message was first sent.");
        eb.addField("Channel", channelMention, true);
        eb.addField("Message ID", messageId, true);

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
