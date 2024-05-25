package io.github.redpanda4552.HifumiBot.util;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class MemberUtils {

    public static Member getOrRetrieveMember(Guild server, long userIdLong) {
        Member member = server.getMemberById(userIdLong);

        if (member == null) {
            member = server.retrieveMemberById(userIdLong).complete();
        }

        return member;
    }
}
