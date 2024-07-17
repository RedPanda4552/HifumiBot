package io.github.redpanda4552.HifumiBot.event;

import java.time.Duration;
import java.time.Instant;

import org.apache.commons.lang3.StringUtils;

import io.github.redpanda4552.HifumiBot.EventLogging;
import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.async.EntryBarrierRunnable;
import io.github.redpanda4552.HifumiBot.async.SpamReviewRunnable;
import io.github.redpanda4552.HifumiBot.async.UrlChangeReviewRunnable;
import io.github.redpanda4552.HifumiBot.database.Database;
import io.github.redpanda4552.HifumiBot.database.MessageObject;
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
        // Store the time of this event handler firing
        Instant now = Instant.now();

        // Ignore private messages
        if (event.getChannelType() == ChannelType.PRIVATE) {
            if (!event.getAuthor().getId().equals(HifumiBot.getSelf().getJDA().getSelfUser().getId())) {
                Messaging.logInfo("EventListener", "onMessageReceived", "DM sent to Hifumi by user " + event.getAuthor().getAsMention() + " (" + event.getAuthor().getName() + ")\n\n```\n" + StringUtils.truncate(event.getMessage().getContentRaw(), 500) + "\n```\nMessage content displayed raw format, truncated to 500 chars. Original length: " + event.getMessage().getContentRaw().length());
                Messaging.sendMessage(event.getChannel(), "I am a bot. If you need something, please ask a human in the server.", event.getMessage(), false);
            }
            
            return;
        }

        // Store all messages, exclude highly privileged users
        boolean skipEvent = HifumiBot.getSelf().getPermissionManager().hasPermission(PermissionLevel.ADMIN, event.getMember());
        Database.insertMessage(event.getMessage(), skipEvent);

        // If the sender was the bot, do not process any further.
        if (event.getAuthor().getId().equals(HifumiBot.getSelf().getJDA().getSelfUser().getId())) {
            return;
        }

        // Do an entry barrier check
        if (HifumiBot.getSelf().getConfig().entryBarrierOptions.enabled && event.getChannel().getId().equals(HifumiBot.getSelf().getConfig().entryBarrierOptions.userInputChannelId)) {
            HifumiBot.getSelf().getScheduler().runOnce(new EntryBarrierRunnable(event));
        }
        
        // If the user has at least guest permissions (is not BLOCKED due to warez or other reasons),
        // then check for emulog/pnach/crash dump
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

        // If the user is not considered privileged, then filter messages and do bot ping nags
        if (!HifumiBot.getSelf().getPermissionManager().hasPermission(PermissionLevel.MOD, event.getMember())) {
            HifumiBot.getSelf().getScheduler().runOnce(new SpamReviewRunnable(event.getMessage(), event.getMessage().getTimeCreated()));
            
            if (Messaging.hasBotPing(event.getMessage())) {
                Messaging.sendMessage(event.getChannel(), "You are pinging a bot.", event.getMessage(), false);
            }
        }

        // For all users, if they tried to ping someone who left the server, let them know.
        if (Messaging.hasGhostPing(event.getMessage())) {
            Messaging.sendMessage(event.getChannel(), ":information_source: The user you tried to mention has left the server.", event.getMessage(), false);
        }
        
        // If the user does not have member role yet and qualifies, give it to them.
        if (HifumiBot.getSelf().getConfig().roles.autoAssignMemberEnabled && event.getMember() != null && event.getMember().getRoles().isEmpty()) {
            Instant joinTime = event.getMember().getGuild().retrieveMemberById(event.getAuthor().getId()).complete().getTimeJoined().toInstant();
            
            if (Duration.between(joinTime, now).toSeconds() >= HifumiBot.getSelf().getConfig().roles.autoAssignMemberTimeSeconds) {
                event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(HifumiBot.getSelf().getConfig().roles.autoAssignMemberRoleId)).complete();
            }
        }
        
        PixivSourceFetcher.getPixivLink(event.getMessage());
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        Database.insertMessageDeleteEvent(event);
        MessageObject deletedMessage = Database.getLatestMessage(event.getMessageId());

        // Don't log the bot's own deletes.
        if (deletedMessage != null && deletedMessage.getAuthorId() == HifumiBot.getSelf().getJDA().getSelfUser().getIdLong()) {
            return;   
        }

        EventLogging.logMessageDeleteEvent(deletedMessage, event.getMessageId());
    }

    @Override 
    public void onMessageBulkDelete(MessageBulkDeleteEvent event) {
        Database.insertMessageBulkDeleteEvent(event);

        for (String messageId : event.getMessageIds()) {
            MessageObject deletedMessage = Database.getLatestMessage(messageId);

            // Don't log the bot's own deletes.
            if (deletedMessage.getAuthorId() == HifumiBot.getSelf().getJDA().getSelfUser().getIdLong()) {
                return;
            }

            EventLogging.logMessageDeleteEvent(deletedMessage, messageId);
        }
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        MessageObject beforeEditMessage = Database.getLatestMessage(event.getMessageId());
        
        if (!HifumiBot.getSelf().getPermissionManager().hasPermission(PermissionLevel.ADMIN, event.getMember())) {
            Database.insertMessageUpdateEvent(event);
            
            // Don't log updates from bots
            if (!event.getAuthor().isBot()) {
                EventLogging.logMessageUpdateEvent(event, beforeEditMessage);
            }
        }

        // If the user is not considered privileged, then filter messages
        if (!HifumiBot.getSelf().getPermissionManager().hasPermission(PermissionLevel.MOD, event.getMember())) {
            HifumiBot.getSelf().getScheduler().runOnce(new UrlChangeReviewRunnable(event.getMessage()));
        }
    }
}
