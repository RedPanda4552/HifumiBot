// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.filter;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.util.DNSQueryResult;
import io.github.redpanda4552.HifumiBot.util.Internet;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.entities.Message;

public class HyperlinkCleaner implements Runnable {
  private static final Pattern URL_PATTERN =
      Pattern.compile("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");

  private final Message message;
  private final Instant instant;

  public HyperlinkCleaner(Message message, Instant instant) {
    this.message = message;
    this.instant = instant;
  }

  @Override
  public void run() {
    Matcher m = URL_PATTERN.matcher(message.getContentDisplay().toLowerCase());

    while (m.find()) {
      if (Internet.nslookup(m.group()) == DNSQueryResult.BLOCKED) {
        message.delete().complete();
        Messaging.logInfo(
            "HyperlinkCleaner",
            "run",
            "Deleting message from user "
                + message.getAuthor().getAsMention()
                + " in channel <#"
                + message.getChannel().getId()
                + ">; DNS query on a URL inside failed and may be malicious.\n\n"
                + "User's message (formatting stripped):\n"
                + "```\n"
                + message.getContentStripped()
                + "\n```");

        if (instant != null) {
          HifumiBot.getSelf().getKickHandler().storeIncident(message.getMember(), instant);
        }

        return;
      }
    }
  }

  public static boolean hasHyperlink(Message msg) {
    return hasHyperlink(msg.getContentDisplay());
  }

  public static boolean hasHyperlink(String msgContent) {
    Matcher m = URL_PATTERN.matcher(msgContent);
    return m.find();
  }
}
