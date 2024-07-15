package io.github.redpanda4552.HifumiBot.command.context;

import java.util.concurrent.TimeUnit;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.async.MessageBulkDeleteRunnable;
import io.github.redpanda4552.HifumiBot.command.AbstractUserContextCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class CommandBan extends AbstractUserContextCommand {
    
    @Override
    protected void onExecute(UserContextInteractionEvent event) {
        if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
            event.reply("You do not have permission to ban users.").setEphemeral(true);
            return;
        }

        event.deferReply(true).queue();
        User user = event.getTarget();

        if (user != null) {
            event.getGuild().ban(user, 0, TimeUnit.SECONDS).queue();
        }
        
        MessageBulkDeleteRunnable runnable = new MessageBulkDeleteRunnable(event.getGuild().getId(), event.getTarget().getId());
        HifumiBot.getSelf().getScheduler().runOnce(runnable);
        event.getHook().sendMessage("User has been banned, messages are being swept up now").queue();
    }

    @Override
    protected CommandData defineUserContextCommand() {
        return Commands.user("ban")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS));
    }
}
