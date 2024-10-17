package io.github.redpanda4552.HifumiBot.util;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.util.Optional;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.config.Config.WarezPromptField;
import io.github.redpanda4552.HifumiBot.database.Database;
import io.github.redpanda4552.HifumiBot.database.WarezEventObject;
import io.github.redpanda4552.HifumiBot.permissions.PermissionLevel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public class WarezUtil {

    public static void applyWarez(GenericCommandInteractionEvent event, User user, Optional<Member> memberOpt, Optional<Message> messageOpt) {
        try {
            // Sanity check against the user itself
            if (user == null) {
                event.getHook()
                    .sendMessage("Could not record warez; user was null (were they deleted from Discord already?)")
                    .setEphemeral(true)
                    .queue();
                return;
            }

            // Log the event
            WarezEventObject warezEvent = new WarezEventObject(
                OffsetDateTime.now().toEpochSecond(), 
                user.getIdLong(),
                WarezEventObject.Action.ADD,
                messageOpt.isPresent() ? messageOpt.get().getIdLong() : null
            );

            if (!Database.insertWarezEvent(warezEvent)) {
                event.getHook()
                    .sendMessage("Warez record could not be stored (SQL error occurred, check logs for details)")
                    .setEphemeral(true)
                    .queue();
                return;
            }

            // Check if the member left the server, and generate output as appropriate
            if (memberOpt.isEmpty()) {
                event.getHook().sendMessage("Warez record logged (User has already left the server)").queue();
            } else {
                Member member = memberOpt.get();
                MessageCreateBuilder mb = new MessageCreateBuilder();
                mb.setContent(member.getAsMention());
                EmbedBuilder eb = new EmbedBuilder();
                
                if (!HifumiBot.getSelf().getPermissionManager().hasPermission(PermissionLevel.MOD, member)) {
                    try {
                        // Add the role. The role event will then add to the database.
                        Role warezRole = event.getGuild().getRoleById(HifumiBot.getSelf().getConfig().roles.warezRoleId);
                        event.getGuild().addRoleToMember(member, warezRole).queue();
                    } catch (InsufficientPermissionException e) {
                        Messaging.logInfo("WarezUtil", "applyWarez", "Failed to assign role to " + member.getAsMention() + " (insufficient permissions)");                        
                    }
                }
                
                eb.setTitle(HifumiBot.getSelf().getConfig().warezPrompt.title);
                eb.setDescription(HifumiBot.getSelf().getConfig().warezPrompt.body);
                eb.setColor(Color.RED);
                
                for (WarezPromptField field : HifumiBot.getSelf().getConfig().warezPrompt.fields) {
                    eb.addField(field.name, field.value, field.inline);
                }

                mb.setEmbeds(eb.build());
                

                String appealsChannelId = HifumiBot.getSelf().getConfig().channels.appealsChannelId;
                TextChannel appealsChannel = HifumiBot.getSelf().getJDA().getTextChannelById(appealsChannelId);

                if (messageOpt.isPresent()) {
                    Message forwardedMsg = messageOpt.get().forwardTo(appealsChannel).complete();
                    Message warezPrompt = forwardedMsg.reply(mb.build()).complete();
                    event.getHook().sendMessage("Warez applied to user: " + warezPrompt.getJumpUrl()).queue();
                } else {
                    Message warezPrompt = appealsChannel.sendMessage(mb.build()).complete();
                    event.getHook().sendMessage("Warez applied to user: " + warezPrompt.getJumpUrl()).queue();
                }
            }
        } catch (Exception e) {
            event.getHook()
                .sendMessage("Warez assignment failed; check bot output channel for error log.")
                .setEphemeral(true)
                .queue();
            Messaging.logException("WarezUtil", "applyWarez", e);
        }
    }
}
