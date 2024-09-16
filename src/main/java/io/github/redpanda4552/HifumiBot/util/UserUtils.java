package io.github.redpanda4552.HifumiBot.util;

import java.util.Optional;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import net.dv8tion.jda.api.entities.User;

public class UserUtils {

    public static Optional<User> getOrRetrieveUser(long userIdLong) {
        return UserUtils.getOrRetrieveUser(String.valueOf(userIdLong));
    }

    public static Optional<User> getOrRetrieveUser(String userId) {
        try {
            User usr = null;

            if ((usr = HifumiBot.getSelf().getJDA().getUserById(userId)) != null
                    || (usr = HifumiBot.getSelf().getJDA().retrieveUserById(userId).complete()) != null) {
                return Optional.of(usr);
            }
        } catch (Exception e) {
            // Squelch
        }

        return Optional.empty();
    }
}
