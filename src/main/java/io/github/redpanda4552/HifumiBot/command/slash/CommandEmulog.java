// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.command.slash;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import io.github.redpanda4552.HifumiBot.config.ConfigManager;
import io.github.redpanda4552.HifumiBot.config.EmulogParserConfig.Rule;
import io.github.redpanda4552.HifumiBot.event.ButtonInteractionElement;
import io.github.redpanda4552.HifumiBot.event.ButtonInteractionElement.ButtonType;
import io.github.redpanda4552.HifumiBot.util.CommandUtils;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import java.util.ArrayList;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class CommandEmulog extends AbstractSlashCommand {

  private static final int RULES_PER_PAGE = 5;

  private ArrayList<MessageEmbed> rulePages;

  public CommandEmulog() {
    rebuildRulePages();
  }

  @Override
  protected void onExecute(SlashCommandInteractionEvent event) {
    event.deferReply(true).queue();

    if (CommandUtils.replyIfBadSubcommand(event, "browse", "new", "update", "delete")) {
      return;
    }

    switch (event.getSubcommandName()) {
      case "browse" -> browse(event);
      case "new" -> newRule(event);
      case "update" -> update(event);
      case "delete" -> delete(event);
    }
  }

  private void rebuildRulePages() {
    rulePages = new ArrayList<MessageEmbed>();
    int pageCount =
        (int)
            Math.ceil(
                (double) HifumiBot.getSelf().getEmulogParserConfig().rules.size() / RULES_PER_PAGE);
    EmbedBuilder eb = new EmbedBuilder();

    for (Rule rule : HifumiBot.getSelf().getEmulogParserConfig().rules) {
      StringBuilder sb = new StringBuilder();
      sb.append("Matches: `").append(rule.toMatch).append("`\n");
      sb.append("Message: `").append(rule.message).append("`\n");
      sb.append("Severity: `").append(rule.severity).append("`");
      eb.addField(rule.name, sb.toString(), false);

      if (eb.getFields().size() >= RULES_PER_PAGE) {
        addToPages(eb, pageCount);
        eb = new EmbedBuilder();
      }
    }

    if (eb.getFields().size() > 0) {
      addToPages(eb, pageCount);
    }
  }

  @Override
  public void onButtonEvent(ButtonInteractionEvent event) {
    try {
      if (event.getMessage().getEmbeds().isEmpty()) {
        event
            .getHook()
            .sendMessage(
                "It looks like the emulog rules embed was deleted. Try using the emulog command"
                    + " again.")
            .setEphemeral(true)
            .queue();
        return;
      }

      if (event.getMessage().getEmbeds().size() > 1) {
        event
            .getHook()
            .sendMessage("Embed error. Try using the emulog command again.")
            .setEphemeral(true)
            .queue();
        return;
      }

      MessageEmbed embed = event.getMessage().getEmbeds().get(0);
      String[] pageParts = embed.getFooter().getText().split(" / ");

      if (pageParts.length != 2) {
        event
            .getHook()
            .sendMessage("Page error. Try using the emulog command again.")
            .setEphemeral(true)
            .queue();
        return;
      }

      int currentPage = 1;
      int totalPages = 1;

      try {
        currentPage = Integer.valueOf(pageParts[0]);
        totalPages = Integer.valueOf(pageParts[1]);
      } catch (NumberFormatException e) {
        event
            .getHook()
            .sendMessage("Page number error. Try using the emulog command again.")
            .setEphemeral(true)
            .queue();
        return;
      }

      ButtonInteractionElement button =
          HifumiBot.getSelf().getSlashCommandListener().getButton(event.getButton().getId());
      String buttonValue = button.getCommandName();

      switch (buttonValue) {
        case "emulog_prev":
          currentPage--;
          break;
        case "emulog_next":
          currentPage++;
          break;
      }

      if (currentPage < 1) {
        currentPage = 1;
      } else if (currentPage > totalPages) {
        currentPage = totalPages;
      }

      ButtonInteractionElement prev =
          HifumiBot.getSelf()
              .getSlashCommandListener()
              .newButton(event.getUser().getId(), "emulog_prev", "Previous", ButtonType.SECONDARY);
      ButtonInteractionElement next =
          HifumiBot.getSelf()
              .getSlashCommandListener()
              .newButton(event.getUser().getId(), "emulog_next", "Next", ButtonType.PRIMARY);
      event
          .getHook()
          .editOriginalEmbeds(rulePages.get((int) currentPage - 1))
          .setActionRow(prev.getButton(), next.getButton())
          .queue();
    } catch (Exception e) {
      event.getHook().editOriginal("An internal error occurred, aborting.").queue();
      Messaging.logException("CommandEmulog", "onButtonEvent", e);
    }
  }

  private void addToPages(EmbedBuilder eb, int pageCount) {
    eb.setTitle("Emulog Parser Rules");
    eb.setDescription("Severity levels: 0 = Information | 1 = Warning | 2 = Severe\n");
    eb.appendDescription("============================ ============================");
    eb.setFooter((rulePages.size() + 1) + " / " + pageCount);
    rulePages.add(eb.build());
  }

  private void browse(SlashCommandInteractionEvent event) {
    ButtonInteractionElement prev =
        HifumiBot.getSelf()
            .getSlashCommandListener()
            .newButton(
                event.getUser().getId(),
                event.getName() + "_prev",
                "Previous",
                ButtonType.SECONDARY);
    ButtonInteractionElement next =
        HifumiBot.getSelf()
            .getSlashCommandListener()
            .newButton(
                event.getUser().getId(), event.getName() + "_next", "Next", ButtonType.PRIMARY);
    event
        .getHook()
        .sendMessageEmbeds(rulePages.get(0))
        .addActionRow(prev.getButton(), next.getButton())
        .queue();
  }

  private void newRule(SlashCommandInteractionEvent event) {
    if (CommandUtils.replyIfMissingOptions(event, "name", "match", "message", "severity")) {
      return;
    }

    String name = event.getOption("name").getAsString();
    String match = event.getOption("match").getAsString();
    String message = event.getOption("message").getAsString();
    Integer severity = event.getOption("severity").getAsInt();

    Rule rule = new Rule();
    rule.name = name;
    rule.toMatch = match;
    rule.message = message;
    rule.severity = severity;

    HifumiBot.getSelf().getEmulogParserConfig().rules.add(rule);
    ConfigManager.write(HifumiBot.getSelf().getEmulogParserConfig());
    rebuildRulePages();

    event.getHook().sendMessage("Added rule `" + name + "`").setEphemeral(true).queue();
  }

  private void update(SlashCommandInteractionEvent event) {
    if (CommandUtils.replyIfMissingOptions(event, "name")) {
      return;
    }

    String name = event.getOption("name").getAsString();
    Rule rule = null;

    for (Rule r : HifumiBot.getSelf().getEmulogParserConfig().rules) {
      if (r.name.equals(name)) {
        rule = r;
        break;
      }
    }

    if (rule == null) {
      event.getHook().sendMessage("No rule `" + name + "` found").setEphemeral(true).queue();
      return;
    }

    OptionMapping matchOpt = event.getOption("match");

    if (matchOpt != null) {
      rule.toMatch = matchOpt.getAsString();
    }

    OptionMapping messageOpt = event.getOption("message");

    if (messageOpt != null) {
      rule.message = messageOpt.getAsString();
    }

    OptionMapping severityOpt = event.getOption("severity");

    if (severityOpt != null) {
      rule.severity = severityOpt.getAsInt();
    }

    ConfigManager.write(HifumiBot.getSelf().getEmulogParserConfig());
    rebuildRulePages();
    event.getHook().sendMessage("Updated rule `" + name + "`").setEphemeral(true).queue();
  }

  private void delete(SlashCommandInteractionEvent event) {
    if (CommandUtils.replyIfMissingOptions(event, "name")) {
      return;
    }

    String name = event.getOption("name").getAsString();
    ArrayList<Rule> rules = HifumiBot.getSelf().getEmulogParserConfig().rules;

    for (int i = 0; i < rules.size(); i++) {
      if (rules.get(i).name.equals(name)) {
        rules.remove(i);
        ConfigManager.write(HifumiBot.getSelf().getEmulogParserConfig());
        rebuildRulePages();
        event.getHook().sendMessage("Deleted rule `" + name + "`").setEphemeral(true).queue();
        return;
      }
    }

    event.getHook().sendMessage("No rule `" + name + "` found").setEphemeral(true).queue();
  }

  @Override
  protected CommandData defineSlashCommand() {
    SubcommandData browse =
        new SubcommandData("browse", "Browse an interactive list of all emulog parser rules");
    OptionData severityOpt =
        new OptionData(OptionType.INTEGER, "severity", "Severity level for the rule")
            .addChoice("Information", 0)
            .addChoice("Warning", 1)
            .addChoice("Critical", 2);
    SubcommandData newRule =
        new SubcommandData("new", "Create an emulog parser rule")
            .addOption(OptionType.STRING, "name", "Unique name of the emulog parser rule", true)
            .addOption(
                OptionType.STRING,
                "match",
                "Regular expression to search for in the emulog. Matches with a single line.",
                true)
            .addOption(
                OptionType.STRING,
                "message",
                "Message to print in the emulog parser results, if this rule is matched.",
                true)
            .addOptions(severityOpt.setRequired(true));
    SubcommandData update =
        new SubcommandData("update", "Update an emulog parser rule")
            .addOption(OptionType.STRING, "name", "Unique name of the emulog parser rule", true)
            .addOption(
                OptionType.STRING,
                "match",
                "Regular expression to search for in the emulog. Matches with a single line.")
            .addOption(
                OptionType.STRING,
                "message",
                "Message to print in the emulog parser results, if this rule is matched.")
            .addOptions(severityOpt.setRequired(false));
    SubcommandData delete =
        new SubcommandData("delete", "Delete an emulog parser rule")
            .addOption(OptionType.STRING, "name", "Name of the emulog parser rule to delete", true);
    return Commands.slash("emulog", "Configure emulog parser rules")
        .addSubcommands(browse, newRule, update, delete)
        .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
  }
}
