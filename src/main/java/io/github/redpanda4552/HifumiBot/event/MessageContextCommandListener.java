// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.event;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.AbstractMessageContextCommand;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import java.util.HashMap;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageContextCommandListener extends ListenerAdapter {

  private HashMap<String, AbstractMessageContextCommand> messageCommands =
      HifumiBot.getSelf().getCommandIndex().getMessageCommands();

  @Override
  public void onMessageContextInteraction(MessageContextInteractionEvent event) {
    if (!event.isFromGuild()) {
      event.reply("Message context commands are disabled in DMs.").setEphemeral(true).queue();
      return;
    }

    if (messageCommands.containsKey(event.getName())) {
      try {
        messageCommands.get(event.getName()).executeIfPermission(event);
      } catch (Exception e) {
        Messaging.logException("MessageContextCommandListener", "onMessageContextInteraction", e);
        event
            .reply("An internal exception occurred and has been reported to admins.")
            .setEphemeral(true)
            .queue();
      }
    }
  }
}
