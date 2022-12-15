// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.filter;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.permissions.PermissionLevel;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import java.time.Instant;
import java.util.HashMap;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.lang3.tuple.Pair;

public class KickHandler {

  private HashMap<String, Pair<Instant, Integer>> indexes;

  public KickHandler() {
    indexes = new HashMap<String, Pair<Instant, Integer>>();
  }

  public synchronized void storeIncident(Member member, Instant newInstant) {
    if (HifumiBot.getSelf().getPermissionManager().hasPermission(PermissionLevel.MOD, member)) {
      return;
    }

    User user = member.getUser();
    String userId = user.getId();

    if (!indexes.containsKey(userId)) {
      indexes.put(userId, Pair.of(newInstant, 1));
      Messaging.sendPrivateMessage(user, HifumiBot.getSelf().getConfig().filterOptions.warnMessage);
    } else {
      Pair<Instant, Integer> p = indexes.get(userId);
      Instant oldInstant = p.getLeft();
      Instant cooldownEnd =
          oldInstant.plusMillis(HifumiBot.getSelf().getConfig().filterOptions.incidentCooldownMS);

      if (newInstant.isAfter(cooldownEnd)) {
        indexes.put(userId, Pair.of(newInstant, 1));
        Messaging.sendPrivateMessage(
            user, HifumiBot.getSelf().getConfig().filterOptions.warnMessage);
      } else {
        Integer toStore = p.getRight() + 1;

        if (toStore >= HifumiBot.getSelf().getConfig().filterOptions.maxIncidents) {
          indexes.remove(userId);

          try {
            doKick(member);
            Messaging.logInfo(
                "KickHandler",
                "storeIncident",
                "Successfully messaged and kicked "
                    + member.getUser().getAsMention()
                    + " ("
                    + member.getUser().getName()
                    + "#"
                    + member.getUser().getDiscriminator()
                    + ") for exceeding the maximum number of filter incidents.");
          } catch (Exception e) {
            Messaging.logException("KickHandler", "storeIncident", e);
          }
        } else {
          indexes.put(userId, Pair.of(newInstant, toStore));

          if (HifumiBot.getSelf().getConfig().filterOptions.enableWarningMessages) {
            Messaging.sendPrivateMessage(
                user, HifumiBot.getSelf().getConfig().filterOptions.warnMessage);
          }
        }
      }
    }
  }

  public synchronized void doKick(Member member) {
    Messaging.sendPrivateMessage(
        member.getUser(), HifumiBot.getSelf().getConfig().filterOptions.kickMessage);
    member.kick().complete();
  }

  public synchronized void doKickForBotJoin(Member member) {
    StringBuilder sb =
        new StringBuilder("**You have been automatically kicked from the PCSX2 server.**\n\n");
    sb.append(
        "Our bot has detected a raid by bot accounts, and you have attempted to join our server at"
            + " the same time as those bots.\n\n");
    sb.append(
        "**If you have no idea why you are receiving this message:** Your account is compromised"
            + " and being used as a spam bot on Discord. Change your password as soon as you"
            + " can.\n\n");
    sb.append(
        "**If you legitimately attempted to join our server:** Sorry, but we will continue to"
            + " automatically kick until the bot raid ends. Please wait for a bit and try to join"
            + " at a later time.\n\n");
    sb.append("Thank you for understanding, stay safe.");
    Messaging.sendPrivateMessage(member.getUser(), sb.toString());
    member.kick().queue();
  }

  public synchronized void flush() {
    Instant now = Instant.now();

    for (String key : indexes.keySet()) {
      Pair<Instant, Integer> p = indexes.get(key);
      Instant cooldownEnd =
          p.getLeft().plusMillis(HifumiBot.getSelf().getConfig().filterOptions.incidentCooldownMS);

      if (now.isAfter(cooldownEnd)) {
        indexes.remove(key);
      }
    }
  }
}
