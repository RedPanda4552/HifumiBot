package io.github.redpanda4552.HifumiBot.event;

import io.github.redpanda4552.HifumiBot.EventLogging;
import io.github.redpanda4552.HifumiBot.HifumiBot;
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
        
        // Reassign warez
        if (HifumiBot.getSelf().getWarezTracking().warezUsers.containsKey(event.getUser().getId())) {
            Role role = event.getGuild().getRoleById(HifumiBot.getSelf().getConfig().roles.warezRoleId);
            event.getGuild().addRoleToMember(event.getMember(), role).queue();
        }

        EventLogging.logGuildMemberJoinEvent(event);
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
