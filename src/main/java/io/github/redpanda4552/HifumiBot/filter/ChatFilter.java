// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.filter;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.permissions.PermissionLevel;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ChatFilter {
  HashMap<String, ArrayList<Pattern>> patternMap = new HashMap<String, ArrayList<Pattern>>();

  public ChatFilter() {
    this.compile();
  }

  public synchronized void compile() {
    patternMap.clear();

    for (Filter filter : HifumiBot.getSelf().getConfig().filters.values()) {
      ArrayList<Pattern> patterns = new ArrayList<Pattern>();

      for (String regex : filter.regexes.values()) {
        patterns.add(Pattern.compile(regex));
      }

      patternMap.put(filter.name, patterns);
    }
  }

  /**
   * Applies any filters specified in the filters property of the config. <br>
   * <i>Note: All messages are flushed to lower case; regular expressions should be written for
   * lower case, or be case-agnostic.</i>
   *
   * @param event - The MessageReceivedEvent to filter.
   * @return True if a regular expression matched and the message was filtered out, false otherwise.
   */
  public synchronized boolean applyFilters(MessageReceivedEvent event) {
    if (event.getAuthor().getId().equals(HifumiBot.getSelf().getJDA().getSelfUser().getId())) {
      return false;
    }

    if (HifumiBot.getSelf()
        .getPermissionManager()
        .hasPermission(PermissionLevel.MOD, event.getMember())) {
      return false;
    }

    for (String filterName : patternMap.keySet()) {
      for (Pattern p : patternMap.get(filterName)) {
        String filteredMessage =
            event.getMessage().getContentDisplay().toLowerCase().replaceAll("[\n\r\t]", " ");
        Matcher m = p.matcher(filteredMessage);
        boolean matches = m.matches();
        boolean find = m.find();

        if (matches || find) {
          event.getMessage().delete().complete();
          String replyMessage =
              HifumiBot.getSelf().getConfig().filters.get(filterName).replyMessage;

          if (!replyMessage.isBlank()) {
            Messaging.sendMessage(event.getChannel(), replyMessage);
          }

          User usr = event.getMessage().getAuthor();
          Messaging.logInfo(
              "ChatFilter",
              "applyFilters",
              "Message from user "
                  + usr.getAsMention()
                  + " ("
                  + usr.getName()
                  + "#"
                  + usr.getDiscriminator()
                  + ")"
                  + " was filtered from channel `"
                  + event.getChannel().getName()
                  + "`.\n\nUser's message (formatting stripped):\n```\n"
                  + event.getMessage().getContentStripped()
                  + "\n```\nMatched this regular expression in filter `"
                  + filterName
                  + "` :\n```\n"
                  + p.pattern()
                  + "\n```");
          return true;
        }
      }
    }

    return false;
  }
}
