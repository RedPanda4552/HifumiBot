package io.github.redpanda4552.HifumiBot.modal;

import java.util.concurrent.TimeUnit;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.async.MessageBulkDeleteRunnable;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

public class BanHandler {

    public static void handle(ModalInteractionEvent event) {
        if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
            Messaging.logInfo("BanHandler", "handle", "User " + event.getUser().getAsMention() + " tried to send a modal interaction for a ban, but does not have permission.");
            event.reply("Permissions error, staff have been notified").setEphemeral(true).queue();
            return;
        }

        String userId = event.getValue("userid").getAsString();
        User user = HifumiBot.getSelf().getJDA().getUserById(userId);

        event.getGuild().ban(user, 0, TimeUnit.SECONDS).queue();
        MessageBulkDeleteRunnable runnable = new MessageBulkDeleteRunnable(event.getGuild().getId(), user.getId());
        HifumiBot.getSelf().getScheduler().runOnce(runnable);

        event.reply("User " + user.getAsMention() + "(" + user.getName() + ") has been banned").setEphemeral(true).queue();
    }
}
