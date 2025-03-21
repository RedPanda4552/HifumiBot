package io.github.redpanda4552.HifumiBot.event;

import io.github.redpanda4552.HifumiBot.database.Database;
import net.dv8tion.jda.api.events.user.update.UserUpdateGlobalNameEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class UserEventListener extends ListenerAdapter {

    @Override 
    public void onUserUpdateName(UserUpdateNameEvent event) {
        Database.insertUsernameChangeEvent(event);
    }

    @Override
    public void onUserUpdateGlobalName(UserUpdateGlobalNameEvent event) {
        Database.insertDisplayNameChangeEvent(event);
    }
}
