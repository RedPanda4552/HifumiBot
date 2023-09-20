package io.github.redpanda4552.HifumiBot.filter;

import java.time.Instant;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.entities.Message;

public class FilterRunnable implements Runnable {
    
    private final Message message;
    private final Instant instant;

    public FilterRunnable(Message message, Instant instant) {
        this.message = message;
        this.instant = instant;
    }

    @Override 
    public void run() {
        try {
            // First run through our custom filters and a DNS check
            if (HifumiBot.getSelf().getChatFilter().applyFilters(message) || HifumiBot.getSelf().getHyperlinkCleaner().applyDNSFilter(message)) {
                HifumiBot.getSelf().getKickHandler().storeIncident(message.getMember(), instant);
            }

            // If that comes back clean, store a history entry so we can check for duplicates.
            HifumiBot.getSelf().getMessageHistoryManager().storeAndCheckDuplicate(message);
        } catch (Exception e) {
            Messaging.logException("FilterRunnable", "run", e);
        }
    }
}
