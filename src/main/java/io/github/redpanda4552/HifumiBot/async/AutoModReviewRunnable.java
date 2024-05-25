package io.github.redpanda4552.HifumiBot.async;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.util.ArrayList;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.database.AutoModEventObject;
import io.github.redpanda4552.HifumiBot.database.Database;
import io.github.redpanda4552.HifumiBot.moderation.ModActions;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class AutoModReviewRunnable implements Runnable {

    private final Guild server;
    private final long userId;
    private final OffsetDateTime time;

    public AutoModReviewRunnable(Guild server, long userId, OffsetDateTime time) {
        this.server = server;
        this.userId = userId;
        this.time = time;
    }

    @Override
    public void run() {
        if (this.reviewAutoMod()) {
            User usr = HifumiBot.getSelf().getJDA().getUserById(this.userId);

            // Kick the user first
            ModActions.kickAndNotifyUser(this.server, this.userId);

            // Now report to staff
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("User Automatically Kicked");
            eb.setDescription("User was automatically kicked from the server for repeated filter incidents.");
            eb.addField("User (As Mention)", usr.getAsMention(), true);
            eb.addField("Username", usr.getName(), true);
            eb.addField("User ID", usr.getId(), true);
            eb.setColor(Color.ORANGE);
            Messaging.logInfoEmbed(eb.build());
        }
    }

    private boolean reviewAutoMod() {
        long cooldownSeconds = HifumiBot.getSelf().getConfig().autoModOptions.cooldownSeconds;
        OffsetDateTime cooldownSubtracted = this.time.minusSeconds(cooldownSeconds);
        ArrayList<AutoModEventObject> autoModEvents = Database.getAutoModEventsSinceTime(this.userId, cooldownSubtracted);
    
        if (autoModEvents == null || autoModEvents.isEmpty()) {
            return false;
        }

        if (autoModEvents.size() >= HifumiBot.getSelf().getConfig().autoModOptions.maxMessages) {
            return true;
        }

        return false;
    }
}
