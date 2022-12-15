// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.util;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class PixivSourceFetcher {

  private static final String PIXIV_BASE_URL = "https://www.pixiv.net/artworks/";
  private static final String[] PIXIV_PATTERN = {
    ".*illust_([0-9]+)_.+", "[a-zA-Z_]*([0-9]+)_p[0-9]+.+"
  };
  private static final Pattern[] p = {
    Pattern.compile(PIXIV_PATTERN[0]), Pattern.compile(PIXIV_PATTERN[1])
  };

  public static void getPixivLink(Message message) {
    if (!message
        .getChannel()
        .getId()
        .equals(HifumiBot.getSelf().getConfig().channels.pixivChannelId)) {
      return;
    }

    ArrayList<String> imageUrls = new ArrayList<String>();
    MessageBuilder mb = new MessageBuilder();

    for (Attachment attach : message.getAttachments()) {
      int pos = 0;
      Matcher m = p[pos].matcher(attach.getFileName());

      while (!m.matches()) {
        if (++pos >= p.length) {
          return;
        }

        m = p[pos].matcher(attach.getFileName());
      }

      try {
        URL url = new URL(PIXIV_BASE_URL + m.group(1));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        if (connection.getResponseCode() == 200) {
          imageUrls.add(PIXIV_BASE_URL + m.group(1));
          break;
        }
      } catch (IOException e) {
      }
    }

    for (int i = 0; i < imageUrls.size(); i++) {
      if (message.getContentDisplay().contains(imageUrls.get(i))) {
        imageUrls.remove(i--);
      }
    }

    if (!imageUrls.isEmpty()) {
      mb.append("Found the sauce! ");

      if (imageUrls.size() > 1) {
        mb.append("(button order matches image order)");
      }

      ArrayList<Button> buttons = new ArrayList<Button>();

      for (String imageUrl : imageUrls) {
        if (buttons.size() < 5) {
          buttons.add(Button.link(imageUrl, "Go to Pixiv"));
        } else {
          mb.append(" (max button count reached, other images will not be linked)");
          break;
        }
      }

      mb.setActionRows(ActionRow.of(buttons));
      message.reply(mb.build()).mentionRepliedUser(false).queue();
    }
  }
}
