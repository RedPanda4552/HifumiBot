package io.github.redpanda4552.HifumiBot.util;

import java.util.Optional;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class MemberUtils {

    public static Optional<Member> getOrRetrieveMember(Guild server, long userIdLong) {
        try {
            Member member = null;

            if ((member = server.getMemberById(userIdLong)) != null
                    || (member = server.retrieveMemberById(userIdLong).complete()) != null) {
                return Optional.of(member);
            }
        } catch (Exception e) {
            // Squelch
        }

        return Optional.empty();
    }
}
