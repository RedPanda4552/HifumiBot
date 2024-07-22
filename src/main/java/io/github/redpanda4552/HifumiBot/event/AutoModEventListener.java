package io.github.redpanda4552.HifumiBot.event;

import java.time.OffsetDateTime;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.async.AutoModReviewRunnable;
import io.github.redpanda4552.HifumiBot.database.Database;
import io.github.redpanda4552.HifumiBot.permissions.PermissionLevel;
import io.github.redpanda4552.HifumiBot.util.Log;
import io.github.redpanda4552.HifumiBot.util.MemberUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.automod.AutoModResponse;
import net.dv8tion.jda.api.events.automod.AutoModExecutionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class AutoModEventListener extends ListenerAdapter {

    @Override
    public void onAutoModExecution(AutoModExecutionEvent event) {
        Log.info("AutoMod event");
        OffsetDateTime now = OffsetDateTime.now();
        Database.insertAutoModEvent(event, now);
        Log.info("AutoMod event inserted to db");

        // If user has elevated permissions, don't do review
        Member member = MemberUtils.getOrRetrieveMember(event.getGuild(), event.getUserIdLong());
        Log.info("AutoMod event member retrieved");
        
        if (member != null && HifumiBot.getSelf().getPermissionManager().hasPermission(PermissionLevel.MOD, member)) {
            Log.info("AutoMod event subject elevated permissions abort");
            return;
        }

        // Start review of automod incidents
        if (event.getResponse().getType() == AutoModResponse.Type.BLOCK_MESSAGE) {
            Log.info("AutoMod event kicking off review runnable");
            HifumiBot.getSelf().getScheduler().runOnce(new AutoModReviewRunnable(event.getGuild(), event.getUserIdLong(), now));
        }
    }
}
