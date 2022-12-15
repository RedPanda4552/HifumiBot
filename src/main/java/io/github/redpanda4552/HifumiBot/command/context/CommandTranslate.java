// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.command.context;

import io.github.redpanda4552.HifumiBot.command.AbstractMessageContextCommand;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class CommandTranslate extends AbstractMessageContextCommand {

  private static final String URL_FORMAT =
      "https://translate.google.com/?sl=auto&tl=en&text=%s&op=translate";

  @Override
  protected void onExecute(MessageContextInteractionEvent event) {
    event.deferReply(true).queue();
    String content = event.getTarget().getContentDisplay();

    try {
      String encoded = URLEncoder.encode(content, "UTF-8");
      String url = String.format(URL_FORMAT, encoded);
      event
          .getHook()
          .sendMessage(content)
          .addActionRow(
              Button.link(url, "Go to Google Translate"),
              Button.link(event.getTarget().getJumpUrl(), "Jump to Original Message"))
          .queue();
    } catch (UnsupportedEncodingException e) {
      event
          .getHook()
          .sendMessage(
              "Could not prepare a Google Translate link for this message, perhaps there are some"
                  + " weird characters in it?")
          .queue();
    }
  }

  @Override
  protected CommandData defineMessageContextCommand() {
    return Commands.message("translate").setDefaultPermissions(DefaultMemberPermissions.ENABLED);
  }
}
