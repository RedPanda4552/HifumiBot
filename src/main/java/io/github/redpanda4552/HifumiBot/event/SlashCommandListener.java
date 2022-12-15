// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.event;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import io.github.redpanda4552.HifumiBot.command.dynamic.DynamicChoice;
import io.github.redpanda4552.HifumiBot.command.dynamic.DynamicCommand;
import io.github.redpanda4552.HifumiBot.command.dynamic.DynamicSubcommand;
import io.github.redpanda4552.HifumiBot.command.slash.CommandEmulog;
import io.github.redpanda4552.HifumiBot.command.slash.CommandWiki;
import io.github.redpanda4552.HifumiBot.event.ButtonInteractionElement.ButtonType;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SlashCommandListener extends ListenerAdapter {

  private final HashMap<String, AbstractSlashCommand> slashCommands =
      HifumiBot.getSelf().getCommandIndex().getSlashCommands();
  private final HashMap<UUID, ButtonInteractionElement> buttonCache = new HashMap<>();
  private final HashMap<UUID, SelectionInteractionElement> selectionCache = new HashMap<>();

  @Override
  public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
    if (!event.isFromGuild()) {
      event.reply("Slash commands are disabled in DMs.").setEphemeral(true).queue();
      return;
    }

    if (slashCommands.containsKey(event.getName())) {
      try {
        slashCommands.get(event.getName()).executeIfPermission(event);
      } catch (Exception e) {
        Messaging.logException("SlashCommandListener", "onSlashCommand", e);
        event
            .reply("An internal exception occurred and has been reported to admins.")
            .setEphemeral(true)
            .queue();
      }
    } else if (HifumiBot.getSelf().getDynCmdConfig().dynamicCommands.containsKey(event.getName())) {
      DynamicCommand command =
          HifumiBot.getSelf().getDynCmdConfig().dynamicCommands.get(event.getName());

      if (command.getSubcommands().containsKey(event.getSubcommandName())) {
        DynamicSubcommand subcommand = command.getSubcommand(event.getSubcommandName());

        if (subcommand.getChoices().containsKey(event.getOption("choice").getAsString())) {
          DynamicChoice choice = subcommand.getChoice(event.getOption("choice").getAsString());
          choice.execute(event);
        }
      }
    } else {
      Messaging.logInfo(
          "SlashCommandListener",
          "onSlashCommandInteraction",
          "Reveived slash command `"
              + event.getName()
              + "`, but we don't have any kind of handler for it!");
    }
  }

  @Override
  public void onButtonInteraction(ButtonInteractionEvent event) {
    UUID uuid = null;

    try {
      uuid = UUID.fromString(event.getComponentId());
    } catch (IllegalArgumentException e) {
      Messaging.logException("SlashCommandListener", "onButtonClick", e);
      event
          .reply("Button tampering detected, admins have been notified.")
          .setEphemeral(true)
          .queue();
      return;
    }

    if (!buttonCache.containsKey(uuid)) {
      event.reply(
          "Whoops! This button has expired. You'll need to run the command again to get an active"
              + " button.");
      return;
    }

    ButtonInteractionElement button = buttonCache.get(uuid);

    if (!button.getUserId().equals(event.getUser().getId())) {
      event
          .reply(
              "You did not send this original command; you are not allowed to interact with this"
                  + " button.")
          .setEphemeral(true)
          .queue();
      return;
    }

    CommandEmulog commandEmulog = (CommandEmulog) slashCommands.get("emulog");

    switch (button.getCommandName()) {
      case "emulog_prev", "emulog_next" -> {
        event.deferEdit().queue();
        commandEmulog.onButtonEvent(event);
      }
    }
  }

  @Override
  public void onSelectMenuInteraction(SelectMenuInteractionEvent event) {
    UUID uuid = null;

    try {
      uuid = UUID.fromString(event.getComponentId());
    } catch (IllegalArgumentException e) {
      Messaging.logException("SlashCommandListener", "onSelectionMenu", e);
      event
          .reply("Selection tampering detected, admins have been notified.")
          .setEphemeral(true)
          .queue();
      return;
    }

    if (!selectionCache.containsKey(uuid)) {
      event
          .reply(
              "Whoops! This selection has expired. You'll need to run the command again to get an"
                  + " active selection.")
          .setEphemeral(true)
          .queue();
      return;
    }

    SelectionInteractionElement selection = selectionCache.get(uuid);

    if (!selection.getUserId().equals(event.getUser().getId())) {
      event
          .reply(
              "You did not send this original command; you are not allowed to interact with this"
                  + " selection.")
          .setEphemeral(true)
          .queue();
      return;
    }

    if ("wiki".equals(selection.getCommandName())) {
      event.deferEdit().queue();
      CommandWiki commandWiki = (CommandWiki) slashCommands.get(selection.getCommandName());
      commandWiki.onSelectionEvent(event);
    }
  }

  public synchronized ButtonInteractionElement newButton(
      String userId, String commandName, String label, ButtonType buttonType) {
    ButtonInteractionElement button =
        new ButtonInteractionElement(userId, commandName, label, buttonType);
    this.buttonCache.put(button.getUuid(), button);
    return button;
  }

  public synchronized void cleanInteractionElements() {
    for (UUID key : this.buttonCache.keySet()) {
      if (Duration.between(this.buttonCache.get(key).getCreatedInstant(), Instant.now()).toMillis()
          > HifumiBot.getSelf().getConfig().slashCommands.timeoutSeconds * 1000L) {
        this.buttonCache.remove(key);
      }
    }

    for (UUID key : this.selectionCache.keySet()) {
      if (Duration.between(this.selectionCache.get(key).getCreatedInstant(), Instant.now())
              .toMillis()
          > HifumiBot.getSelf().getConfig().slashCommands.timeoutSeconds * 1000L) {
        this.selectionCache.remove(key);
      }
    }
  }

  public synchronized SelectionInteractionElement newSelection(String userId, String commandName) {
    SelectionInteractionElement selection = new SelectionInteractionElement(userId, commandName);
    this.selectionCache.put(selection.getUuid(), selection);
    return selection;
  }

  public synchronized ButtonInteractionElement getButton(String uuidString) {
    return buttonCache.get(UUID.fromString(uuidString));
  }
}
