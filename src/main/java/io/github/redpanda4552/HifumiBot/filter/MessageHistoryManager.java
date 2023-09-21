package io.github.redpanda4552.HifumiBot.filter;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.util.MessageBulkDeleteTargetedRunnable;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

public class MessageHistoryManager {
    
    // Map of User IDs to Message IDs, used to make accessing MessageHistoryEntries by user easier.
    private ConcurrentHashMap<String, String> duplicateMap;

    public MessageHistoryManager() {
        this.duplicateMap = new ConcurrentHashMap<String, String>();
    }

    public MessageHistoryEntry fetchMessage(String messageId) {
        return HifumiBot.getSelf().getMessageHistoryStorage().messageHistory.get(messageId);
    }

    public void removeMessage(String messageId) {
        HifumiBot.getSelf().getMessageHistoryStorage().messageHistory.remove(messageId);
    }

    public void store(Message msg) {
        MessageHistoryEntry entry = new MessageHistoryEntry(msg);
        HifumiBot.getSelf().getMessageHistoryStorage().messageHistory.put(msg.getId(), entry);
    }

    public boolean storeAndCheckDuplicate(Message msg) {
        MessageHistoryEntry entry = new MessageHistoryEntry(msg);
        HifumiBot.getSelf().getMessageHistoryStorage().messageHistory.put(msg.getId(), entry);
        
        // If it is a super short message, its probably just repeated emotes or acknowledgements.
        if (entry.getMessageContent().length() <= 16) {
            return false;
        }

        // If we have a prior entry, check it
        if (this.duplicateMap.containsKey(entry.getUserId())) {
            MessageHistoryEntry oldEntry = HifumiBot.getSelf().getMessageHistoryStorage().messageHistory.get(this.duplicateMap.get(entry.getUserId()));
            
            if (oldEntry != null && (checkMessage(oldEntry, entry) || checkAttachments(oldEntry, entry))) {
                return true;
            }
        }
        
        // Log this new entry
        this.duplicateMap.put(entry.getUserId(), entry.getMessageId());
        return false;
    }

    private boolean checkMessage(MessageHistoryEntry oldEntry, MessageHistoryEntry entry) {
        if (entry.getMessageContent() != null && entry.getMessageContent().length() > 0 && oldEntry.getMessageContent().equals(entry.getMessageContent())) {
            entry.setCount(oldEntry.getCount() + 1);

            if (entry.getCount() >= HifumiBot.getSelf().getConfig().filterOptions.maxIncidents) {
                doCleanup(oldEntry, entry);
                return true;
            }
        }

        return false;
    }

    private boolean checkAttachments(MessageHistoryEntry oldEntry, MessageHistoryEntry entry) {
        if (entry.getAttachmentUrls() != null && entry.getAttachmentUrls().size() > 0) {
            for (String attachmentUrl : entry.getAttachmentUrls()) {
                if (oldEntry.getAttachmentUrls().contains(attachmentUrl)) {
                    entry.setCount(oldEntry.getCount() + 1);

                    if (entry.getCount() >= HifumiBot.getSelf().getConfig().filterOptions.maxIncidents) {
                        doCleanup(oldEntry, entry);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void doCleanup(MessageHistoryEntry oldEntry, MessageHistoryEntry entry) {
        Message oldMessage = HifumiBot.getSelf().getJDA().getTextChannelById(oldEntry.getChannelId()).retrieveMessageById(oldEntry.getMessageId()).complete();
        Message message = HifumiBot.getSelf().getJDA().getTextChannelById(entry.getChannelId()).retrieveMessageById(entry.getMessageId()).complete();
        Member member = oldMessage.getMember();

        member.timeoutFor(Duration.ofHours(8)).complete();
        oldMessage.delete().complete();
        message.delete().complete();
        MessageBulkDeleteTargetedRunnable runnable = new MessageBulkDeleteTargetedRunnable(entry.getServerId(), member.getId(), entry.getChannelId(), entry.getMessageContent(), 1);
        HifumiBot.getSelf().getScheduler().runOnce(runnable);
        this.duplicateMap.remove(member.getId());
        
        Messaging.logInfo("ChatFilter", "applyFilters",
                "Message from user " + member.getUser().getAsMention() + " (" + member.getUser().getName() + "#" + member.getUser().getDiscriminator() + ")"
                        + " was removed from channel `" + message.getChannel().getName() + "`.\n\nUser's message (formatting stripped, truncated to 500 chars):\n```\n"
                        + StringUtils.truncate(message.getContentStripped(), 500)
                        + "\n```\nThis message was a duplicate - user might be a bot. An automated job is also sweeping up any other matches, but might take a moment to finish."
                        + "\n\nUser was automatically timed out for 8 hours; if this looks like a bot and they are still in the server, consider using `/spamkick` to kick them.");
    }

    public void flush() {
        // Yeah it's n^2, sue me
        while (HifumiBot.getSelf().getMessageHistoryStorage().messageHistory.size() > 131072) {
            String oldestId = null;
            OffsetDateTime oldestTime = null;

            for (String messageId : HifumiBot.getSelf().getMessageHistoryStorage().messageHistory.keySet()) {
                MessageHistoryEntry entry = HifumiBot.getSelf().getMessageHistoryStorage().messageHistory.get(messageId);
                if (oldestId == null || entry.getDateTime().isBefore(oldestTime)) {
                    oldestId = messageId;
                    oldestTime = entry.getDateTime();
                }
            }
            
            if (oldestId == null) {
                throw new IllegalStateException("Flush triggered sanity check");
            }

            HifumiBot.getSelf().getMessageHistoryStorage().messageHistory.remove(oldestId);
        }
        
        for (String key : this.duplicateMap.keySet()) {
            String messageId = this.duplicateMap.get(key);

            if (!HifumiBot.getSelf().getMessageHistoryStorage().messageHistory.containsKey(messageId)) {
                this.duplicateMap.remove(key);
            }
        }
    }
}
