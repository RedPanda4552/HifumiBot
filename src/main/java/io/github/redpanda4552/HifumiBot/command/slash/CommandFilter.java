// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.command.slash;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import io.github.redpanda4552.HifumiBot.config.ConfigManager;
import io.github.redpanda4552.HifumiBot.filter.Filter;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class CommandFilter extends AbstractSlashCommand {

  private static final String NO_SUCH_FILTER = ":x: No such filter '%s' exists.";

  @Override
  protected void onExecute(SlashCommandInteractionEvent event) {
    event.deferReply().queue();
    EmbedBuilder eb = new EmbedBuilder();
    Filter filter = null;
    String filterName = null;
    String regexName = null;
    String regex = null;

    switch (event.getSubcommandName()) {
      case "new" -> {
        filterName = event.getOption("filter-name").getAsString();
        if (HifumiBot.getSelf().getConfig().filters.containsKey(filterName)) {
          event.getHook().sendMessage(":x: A filter already exists with this name.").queue();
          return;
        }
        filter = new Filter();
        filter.name = filterName;
        HifumiBot.getSelf().getConfig().filters.put(filter.name, filter);
        ConfigManager.write(HifumiBot.getSelf().getConfig());
        event
            .getHook()
            .sendMessage(":white_check_mark: Created empty filter '" + filter.name + "'.")
            .queue();
      }
      case "add" -> {
        filterName = event.getOption("filter-name").getAsString();
        regexName = event.getOption("regex-name").getAsString();
        regex = event.getOption("regex").getAsString();
        filter = HifumiBot.getSelf().getConfig().filters.get(filterName);
        if (filter == null) {
          event.getHook().sendMessage(String.format(NO_SUCH_FILTER, filterName)).queue();
          return;
        }
        try {
          Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
          event
              .getHook()
              .sendMessage(
                  ":x: Regular expression did not compile! \n\nException message: "
                      + e.getMessage())
              .queue();
          return;
        }
        filter.regexes.put(regexName, regex);
        HifumiBot.getSelf().getConfig().filters.put(filterName, filter);
        ConfigManager.write(HifumiBot.getSelf().getConfig());
        HifumiBot.getSelf().getChatFilter().compile();
        event
            .getHook()
            .sendMessage(
                ":white_check_mark: Created regex '"
                    + regexName
                    + "' on filter '"
                    + filter.name
                    + "'.")
            .queue();
      }
      case "remove" -> {
        filterName = event.getOption("filter-name").getAsString();
        regexName = event.getOption("regex-name").getAsString();
        filter = HifumiBot.getSelf().getConfig().filters.get(filterName);
        if (filter == null) {
          event.getHook().sendMessage(String.format(NO_SUCH_FILTER, filterName)).queue();
          return;
        }
        if (filter.regexes.containsKey(regexName)) {
          filter.regexes.remove(regexName);
        } else {
          event
              .getHook()
              .sendMessage(
                  ":x: No regex with name '"
                      + regexName
                      + "' found on filter '"
                      + filter.name
                      + "'.")
              .queue();
          break;
        }
        HifumiBot.getSelf().getConfig().filters.put(filter.name, filter);
        ConfigManager.write(HifumiBot.getSelf().getConfig());
        HifumiBot.getSelf().getChatFilter().compile();
        event
            .getHook()
            .sendMessage(
                ":white_check_mark: Removed regex '"
                    + regexName
                    + "' from filter '"
                    + filter.name
                    + "'.")
            .queue();
      }
      case "reply" -> {
        filterName = event.getOption("filter-name").getAsString();
        OptionMapping opt = event.getOption("reply");
        String reply = "";
        if (opt != null) {
          reply = opt.getAsString();
        }
        filter = HifumiBot.getSelf().getConfig().filters.get(filterName);
        if (filter == null) {
          event.getHook().sendMessage(String.format(NO_SUCH_FILTER, filterName)).queue();
          return;
        }
        filter.replyMessage = reply;
        HifumiBot.getSelf().getConfig().filters.put(filter.name, filter);
        ConfigManager.write(HifumiBot.getSelf().getConfig());
        event
            .getHook()
            .sendMessage(":white_check_mark: Set reply message on filter '" + filter.name + "'.")
            .queue();
      }
      case "get" -> {
        filterName = event.getOption("filter-name").getAsString();
        filter = HifumiBot.getSelf().getConfig().filters.get(filterName);
        if (filter == null) {
          event.getHook().sendMessage(String.format(NO_SUCH_FILTER, filterName)).queue();
          return;
        }
        eb.setTitle(filter.name);
        eb.setDescription(
            filter.replyMessage.isBlank()
                ? "This filter has no reply message."
                : "Replies with:\n```\n" + filter.replyMessage + "\n```");
        for (String str : filter.regexes.keySet()) {
          eb.addField(str, "`" + filter.regexes.get(str) + "`", false);
        }
        event.getHook().sendMessageEmbeds(eb.build()).queue();
      }
      case "delete" -> {
        filterName = event.getOption("filter-name").getAsString();
        filter = HifumiBot.getSelf().getConfig().filters.get(filterName);
        if (filter == null) {
          event.getHook().sendMessage(String.format(NO_SUCH_FILTER, filterName)).queue();
          return;
        }
        HifumiBot.getSelf().getConfig().filters.remove(filter.name);
        ConfigManager.write(HifumiBot.getSelf().getConfig());
        HifumiBot.getSelf().getChatFilter().compile();
        event
            .getHook()
            .sendMessage(":white_check_mark: Deleted filter '" + filter.name + "'.")
            .queue();
      }
      case "list" -> {
        eb.setTitle("Filter List");
        for (Filter f : HifumiBot.getSelf().getConfig().filters.values()) {
          eb.addField(
              f.name,
              f.regexes.size()
                  + " regular expressions // Reply Message = "
                  + !f.replyMessage.isBlank(),
              false);
        }
        if (eb.getFields().size() == 0) {
          eb.setDescription("oh yeah... THERE IS NONE.");
        }
        event.getHook().sendMessageEmbeds(eb.build()).queue();
      }
      case "compile" -> {
        HifumiBot.getSelf().getChatFilter().compile();
        event
            .getHook()
            .sendMessage(":white_check_mark: Compiled all filter regular expressions.")
            .queue();
      }
    }
  }

  @Override
  protected CommandData defineSlashCommand() {
    SubcommandData newFilter =
        new SubcommandData("new", "Create a new, empty filter")
            .addOption(OptionType.STRING, "filter-name", "Unique identifier for the filter", true);
    SubcommandData add =
        new SubcommandData("add", "Add a regular expression")
            .addOption(
                OptionType.STRING,
                "filter-name",
                "Unique identifier of the target filter to modify",
                true)
            .addOption(
                OptionType.STRING,
                "regex-name",
                "Unique identifier for the regular expression",
                true)
            .addOption(OptionType.STRING, "regex", "Regular expression", true);
    SubcommandData remove =
        new SubcommandData("remove", "Remove a regular expression")
            .addOption(
                OptionType.STRING,
                "filter-name",
                "Unique identifier of the target filter to modify",
                true)
            .addOption(
                OptionType.STRING,
                "regex-name",
                "Unique identifier for the regular expression",
                true);
    SubcommandData reply =
        new SubcommandData("reply", "Set or clear the reply message")
            .addOption(
                OptionType.STRING,
                "filter-name",
                "Unique identifier of the target filter to modify",
                true)
            .addOption(OptionType.STRING, "reply", "Message to reply with, leave blank to clear");
    SubcommandData get =
        new SubcommandData("get", "View configured regexes and reply for a filter")
            .addOption(
                OptionType.STRING,
                "filter-name",
                "Unique identifier of the target filter to modify",
                true);
    SubcommandData delete =
        new SubcommandData("delete", "Delete a filter")
            .addOption(
                OptionType.STRING,
                "filter-name",
                "Unique identifier of the target filter to modify",
                true);
    SubcommandData list = new SubcommandData("list", "List all filters");
    SubcommandData compile = new SubcommandData("compile", "Compile all filters");
    return Commands.slash("filter", "Create and manage chat filters")
        .addSubcommands(newFilter, add, remove, reply, get, delete, list, compile)
        .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
  }
}
