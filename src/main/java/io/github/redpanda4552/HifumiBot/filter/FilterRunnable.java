package io.github.redpanda4552.HifumiBot.filter;

import java.awt.Color;
import java.time.OffsetDateTime;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

public class FilterRunnable implements Runnable {
    
    private final Message message;
    private final OffsetDateTime eventTime;

    public FilterRunnable(Message message, OffsetDateTime eventTime) {
        this.message = message;
        this.eventTime = eventTime;
    }

    @Override 
    public void run() {
        try {
            long cooldownSeconds = HifumiBot.getSelf().getConfig().filterOptions.incidentCooldownMS / 1000;
            OffsetDateTime cooldownSubtracted = eventTime.minusSeconds(cooldownSeconds);
            long cooldownEpochSeconds = cooldownSubtracted.toEpochSecond();
            FilterHandler handler = HifumiBot.getSelf().getFilterHandler();

            // If the user set off a configured filter or the DNS filter
            if (handler.applyFilters(message) || handler.applyDNSFilter(message)) {
                // And this has happened multiple times now
                if (handler.reviewFilterEvents(message.getAuthor().getIdLong(), cooldownEpochSeconds)) {
                    // Then get rid of them and do not bother checking for spam.
                    if (handler.kickUser(message.getGuild(), message.getAuthor().getIdLong())) {
                        User usr = message.getAuthor();
                        
                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setTitle("User Automatically Kicked");
                        eb.setDescription("User was automatically kicked from the server for repeated filter incidents.");
                        eb.addField("User (As Mention)", usr.getAsMention(), true);
                        eb.addField("Username", usr.getName(), true);
                        eb.addField("User ID", usr.getId(), true);
                        eb.setColor(Color.YELLOW);

                        Messaging.logInfoEmbed(eb.build());
                    }
                    return;
                }
            }

            // If the user has spammed the same thing
            if (handler.reviewSpam(message, cooldownEpochSeconds)) {
                // Time them out
                if (handler.timeoutUser(message.getGuild(), message.getAuthor().getIdLong())) {
                    handler.deleteMessages(message, cooldownEpochSeconds);
                    User usr = message.getAuthor();
                        
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle("User Automatically Timed Out");
                    eb.setDescription("User was automatically timed out for spam.");
                    eb.addField("User (As Mention)", usr.getAsMention(), true);
                    eb.addField("Username", usr.getName(), true);
                    eb.addField("User ID", usr.getId(), true);
                    eb.setColor(Color.YELLOW);

                    Messaging.logInfoEmbed(eb.build());
                }
            }
        } catch (Exception e) {
            Messaging.logException("FilterRunnable", "run", e);
        }
    }
}
