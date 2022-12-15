// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.command;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.context.CommandReverseImage;
import io.github.redpanda4552.HifumiBot.command.context.CommandTranslate;
import io.github.redpanda4552.HifumiBot.command.dynamic.DynamicChoice;
import io.github.redpanda4552.HifumiBot.command.dynamic.DynamicCommand;
import io.github.redpanda4552.HifumiBot.command.dynamic.DynamicSubcommand;
import io.github.redpanda4552.HifumiBot.command.slash.CommandAbout;
import io.github.redpanda4552.HifumiBot.command.slash.CommandBuildNumber;
import io.github.redpanda4552.HifumiBot.command.slash.CommandCPU;
import io.github.redpanda4552.HifumiBot.command.slash.CommandDynCmd;
import io.github.redpanda4552.HifumiBot.command.slash.CommandEmulog;
import io.github.redpanda4552.HifumiBot.command.slash.CommandFilter;
import io.github.redpanda4552.HifumiBot.command.slash.CommandGPU;
import io.github.redpanda4552.HifumiBot.command.slash.CommandGameDB;
import io.github.redpanda4552.HifumiBot.command.slash.CommandPFP;
import io.github.redpanda4552.HifumiBot.command.slash.CommandPanic;
import io.github.redpanda4552.HifumiBot.command.slash.CommandPerms;
import io.github.redpanda4552.HifumiBot.command.slash.CommandReload;
import io.github.redpanda4552.HifumiBot.command.slash.CommandRun;
import io.github.redpanda4552.HifumiBot.command.slash.CommandSay;
import io.github.redpanda4552.HifumiBot.command.slash.CommandShutdown;
import io.github.redpanda4552.HifumiBot.command.slash.CommandSpamKick;
import io.github.redpanda4552.HifumiBot.command.slash.CommandWarez;
import io.github.redpanda4552.HifumiBot.command.slash.CommandWiki;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import java.time.Instant;
import java.util.HashMap;
import lombok.Getter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class CommandIndex {

  private CommandListUpdateAction commandsToRegister;
  @Getter private HashMap<String, AbstractSlashCommand> slashCommands;
  @Getter private HashMap<String, AbstractMessageContextCommand> messageCommands;
  private HashMap<String, HashMap<String, Instant>> history;

  /** Create a new CommandIndex and invoke the {@link CommandIndex#rebuild rebuild()} method. */
  public CommandIndex() {
    slashCommands = new HashMap<String, AbstractSlashCommand>();
    messageCommands = new HashMap<String, AbstractMessageContextCommand>();
    history = new HashMap<String, HashMap<String, Instant>>();
    rebuild();
  }

  /** Rebuild this CommandIndex from the Config object in HifumiBot. */
  public void rebuild() {
    commandsToRegister = HifumiBot.getSelf().getJda().updateCommands();
    rebuildSlash();
    rebuildMessage();
    rebuildDynamic();
    commandsToRegister.queue();
  }

  public void rebuildSlash() {
    slashCommands.clear();
    registerSlashCommand(new CommandSay());
    registerSlashCommand(new CommandAbout());
    registerSlashCommand(new CommandWarez());
    registerSlashCommand(new CommandShutdown());
    registerSlashCommand(new CommandReload());
    registerSlashCommand(new CommandWiki());
    registerSlashCommand(new CommandRun());
    registerSlashCommand(new CommandPFP());
    registerSlashCommand(new CommandPerms());
    registerSlashCommand(new CommandFilter());
    registerSlashCommand(new CommandCPU());
    registerSlashCommand(new CommandGPU());
    registerSlashCommand(new CommandDynCmd());
    registerSlashCommand(new CommandBuildNumber());
    registerSlashCommand(new CommandSpamKick());
    registerSlashCommand(new CommandPanic());
    registerSlashCommand(new CommandGameDB());
    registerSlashCommand(new CommandEmulog());
  }

  public void rebuildMessage() {
    messageCommands.clear();
    registerMessageCommand(new CommandTranslate());
    registerMessageCommand(new CommandReverseImage());
  }

  public void rebuildDynamic() {
    HashMap<String, DynamicCommand> commands =
        HifumiBot.getSelf().getDynCmdConfig().dynamicCommands;

    for (String commandName : commands.keySet()) {
      if (slashCommands.containsKey(commandName)) {
        Messaging.logInfo(
            "CommandIndex",
            "rebuildDynamic",
            "Skipping dynamic command \""
                + commandName
                + "\", found a built-in command with the same name");
        break;
      }

      DynamicCommand command = commands.get(commandName);
      SlashCommandData commandData = Commands.slash(command.getName(), command.getDescription());
      HashMap<String, DynamicSubcommand> subcommands = command.getSubcommands();

      for (String subcommandName : subcommands.keySet()) {
        DynamicSubcommand subcommand = subcommands.get(subcommandName);
        SubcommandData subcommandData =
            new SubcommandData(subcommand.getName(), subcommand.getDescription());
        OptionData opt = new OptionData(OptionType.STRING, "choice", "Command choice", true);
        HashMap<String, DynamicChoice> choices = subcommand.getChoices();

        for (String choiceName : choices.keySet()) {
          DynamicChoice choice = choices.get(choiceName);
          opt.addChoice(choice.getName(), choice.getName());
        }

        subcommandData.addOptions(opt);
        commandData.addSubcommands(subcommandData);
      }

      commandsToRegister.addCommands(commandData);
    }
  }

  private void registerSlashCommand(AbstractSlashCommand slashCommand) {
    String name = slashCommand.defineSlashCommand().getName();
    slashCommands.put(name, slashCommand);
    commandsToRegister.addCommands(slashCommand.defineSlashCommand());
  }

  private void registerMessageCommand(AbstractMessageContextCommand messageCommand) {
    String name = messageCommand.defineMessageContextCommand().getName();
    messageCommands.put(name, messageCommand);
    commandsToRegister.addCommands(messageCommand.defineMessageContextCommand());
  }

  /**
   * Check if this is a ninja command, update command history if not.
   *
   * @return True if ninja, false if not ninja and updated.
   */
  public boolean isNinja(String commandName, String channelId) {
    if (channelId.equals(HifumiBot.getSelf().getConfig().channels.restrictedCommandChannelId)) {
      return false;
    }

    Instant now = Instant.now();

    if (!history.containsKey(commandName)) {
      HashMap<String, Instant> subMap = new HashMap<String, Instant>();
      subMap.put(channelId, now);
      history.put(commandName, subMap);
      return false;
    } else {
      HashMap<String, Instant> subMap = history.get(commandName);

      if (!subMap.containsKey(channelId)) {
        subMap.put(channelId, now);
        return false;
      } else if (now.minusMillis(HifumiBot.getSelf().getConfig().ninjaInterval)
          .isBefore(subMap.get(channelId))) {
        return true;
      } else {
        subMap.put(channelId, now);
        return false;
      }
    }
  }
}
