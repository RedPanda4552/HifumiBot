package io.github.redpanda4552.HifumiBot.command.context;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.AbstractUserContextCommand;
import io.github.redpanda4552.HifumiBot.permissions.PermissionLevel;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

public class CommandBan extends AbstractUserContextCommand {
    
    @Override
    protected void onExecute(UserContextInteractionEvent event) {
        if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
            event.reply("You do not have permission to ban users.").setEphemeral(true);
            return;
        }

        User user = event.getTarget();
        Member member = event.getTargetMember();

        if (member != null && HifumiBot.getSelf().getPermissionManager().hasPermission(PermissionLevel.MOD, member)) {
            event.reply("This user has elevated permissions. To minimize accidents, elevated users cannot be banned with this command.").setEphemeral(true).queue();
            return;
        }

        if (user != null) {
            TextInput.Builder userId = TextInput.create("userid", "User ID (DO NOT EDIT)", TextInputStyle.SHORT)
                    .setRequired(true)
                    .setValue(user.getId());
            TextInput.Builder username = TextInput.create("username", "Username (DO NOT EDIT)", TextInputStyle.SHORT)
                    .setRequired(true)
                    .setValue(user.getName());
            TextInput.Builder displayName = TextInput.create("displayname", "Current Display Name (DO NOT EDIT)", TextInputStyle.SHORT)
                    .setRequired(true)
                    .setValue(user.getEffectiveName());
            Modal.Builder modal = Modal.create("ban", "Confirm Banning Member")
                    .addComponents(
                        ActionRow.of(userId.build()),
                        ActionRow.of(username.build()),
                        ActionRow.of(displayName.build())
                    );
            event.replyModal(modal.build()).queue();
        }
    }

    @Override
    protected CommandData defineUserContextCommand() {
        return Commands.user("ban")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS));
    }
}
