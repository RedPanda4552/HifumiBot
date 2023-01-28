package io.github.redpanda4552.HifumiBot;

import java.awt.Color;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

public class EventLogging {
    
    public static void logGuildMemberJoinEvent(GuildMemberJoinEvent event) {
        String channelId = HifumiBot.getSelf().getConfig().channels.logging.memberJoin;

        if (channelId == null || channelId.isBlank()) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();
        long diff = Duration.between(event.getMember().getTimeCreated(), now).toMinutes();
        
        Member retrievedMember = event.getGuild().retrieveMemberById(event.getMember().getId()).complete();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.GREEN);
        eb.setTitle("Member Joined");
        
        if (diff < 5) {
            eb.appendDescription(":warning: Account appears to be less than 5 minutes old\n");
        }

        eb.addField("Username (As Mention)", event.getUser().getAsMention(), true);
        eb.addField("Username (Plain Text)", event.getUser().getName() + "#" + event.getUser().getDiscriminator(), true);
        eb.addField("User ID", event.getUser().getId(), true);
        eb.addField("Account Created", event.getUser().getTimeCreated().format(DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm:ss")) + " UTC", true);
        eb.addField("Joined Server", event.getMember().getTimeJoined().format(DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm:ss")) + " UTC", true);
        eb.addField("Current Display Name", event.getMember().getEffectiveName(), true);

        MessageBuilder mb = new MessageBuilder();
        mb.setEmbeds(eb.build());
        mb.setActionRows(ActionRow.of(
            Button.of(ButtonStyle.PRIMARY, "timeout:" + retrievedMember.getId(), "Timeout (1 hr)"),
            Button.of(ButtonStyle.SECONDARY, "kick:" + retrievedMember.getId(), "Kick"),
            Button.of(ButtonStyle.DANGER, "ban:" + retrievedMember.getId(), "Ban (And delete msgs from 24 hrs)")
        ));
        Messaging.sendMessage(channelId, mb.build());
    }

    public static void logGuildMemberRemoveEvent(GuildMemberRemoveEvent event) {
        String channelId = HifumiBot.getSelf().getConfig().channels.logging.memberLeave;

        if (channelId == null || channelId.isBlank()) {
            return;
        }

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.ORANGE);
        eb.setTitle("Member Left");
        eb.addField("Username (As Mention)", event.getUser().getAsMention(), true);
        eb.addField("Username (Plain Text)", event.getUser().getName() + "#" + event.getUser().getDiscriminator(), true);
        eb.addField("User ID", event.getUser().getId(), true);
        eb.addField("Account Created", event.getUser().getTimeCreated().format(DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm:ss")) + " UTC", true);

        if (event.getMember() != null) {
            eb.addField("Joined Server", event.getMember().getTimeJoined().format(DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm:ss")) + " UTC", true);
            eb.addField("Current Display Name", event.getMember().getEffectiveName(), true);
        }
        
        MessageBuilder mb = new MessageBuilder();
        mb.setEmbeds(eb.build());
        Messaging.sendMessage(channelId, mb.build());
    }

    public static void logGuildBanEvent(GuildBanEvent event) {
        String channelId = HifumiBot.getSelf().getConfig().channels.logging.memberBan;

        if (channelId == null || channelId.isBlank()) {
            return;
        }

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.RED);
        eb.setTitle("Member Banned");
        eb.addField("Username (As Mention)", event.getUser().getAsMention(), true);
        eb.addField("Username (Plain Text)", event.getUser().getName() + "#" + event.getUser().getDiscriminator(), true);
        eb.addField("User ID", event.getUser().getId(), true);
        eb.addField("Account Created", event.getUser().getTimeCreated().format(DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm:ss")) + " UTC", true);

        MessageBuilder mb = new MessageBuilder();
        mb.setEmbeds(eb.build());
        Messaging.sendMessage(channelId, mb.build());
    }
}
