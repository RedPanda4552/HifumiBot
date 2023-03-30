package io.github.redpanda4552.HifumiBot.event;

import java.time.format.DateTimeFormatter;

import io.github.redpanda4552.HifumiBot.EventLogging;
import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.util.EmbedUtil;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MemberEventListener extends ListenerAdapter {
    
    private boolean lockdown = false;

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (lockdown) {
            if (event.getMember() != null) {
                HifumiBot.getSelf().getKickHandler().doKickForBotJoin(event.getMember());
                return;
            }
        }
        
        EventLogging.logGuildMemberJoinEvent(event);

        if (HifumiBot.getSelf().getWarezTracking().warezUsers.containsKey(event.getUser().getId())) {
            // First assign the warez role
            Role role = event.getGuild().getRoleById(HifumiBot.getSelf().getConfig().roles.warezRoleId);
            event.getGuild().addRoleToMember(event.getMember(), role).complete();

            // Then send a notification
            EmbedBuilder eb = EmbedUtil.newFootedEmbedBuilder(HifumiBot.getSelf().getJDA().getSelfUser());
            eb.setTitle("Warez Member Rejoined");
            eb.setDescription("A user who was previously warez'd has rejoined the server.");
            eb.addField("User Name", event.getUser().getName(), true);
            eb.addField("Display Name", event.getMember().getEffectiveName(), true);
            String dateStr = HifumiBot.getSelf().getWarezTracking().warezUsers.get(event.getUser().getId())
                    .format(DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm:ss")) + " UTC";
            eb.addField("Warez Date", dateStr, true);
            Messaging.sendMessageEmbed(
                    event.getGuild().getTextChannelById(HifumiBot.getSelf().getConfig().channels.systemOutputChannelId),
                    eb.build());
        }
    }

    @Override 
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        EventLogging.logGuildMemberRemoveEvent(event);
    }

    @Override
    public void onGuildBan(GuildBanEvent event) {
        EventLogging.logGuildBanEvent(event);
    }

    public void setLockdown(boolean lockdown) {
        this.lockdown = lockdown;
    }
    
    public boolean getLockdown() {
        return lockdown;
    }
}
