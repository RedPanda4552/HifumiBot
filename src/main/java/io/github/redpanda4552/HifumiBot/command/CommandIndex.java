/**
 * This file is part of HifumiBot, licensed under the MIT License (MIT)
 * 
 * Copyright (c) 2020 RedPanda4552 (https://github.com/RedPanda4552)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.redpanda4552.HifumiBot.command;

import java.util.HashMap;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.context.CommandBan;
import io.github.redpanda4552.HifumiBot.command.context.CommandReverseImage;
import io.github.redpanda4552.HifumiBot.command.context.CommandTranslateEN;
import io.github.redpanda4552.HifumiBot.command.dynamic.DynamicChoice;
import io.github.redpanda4552.HifumiBot.command.dynamic.DynamicCommand;
import io.github.redpanda4552.HifumiBot.command.dynamic.DynamicSubcommand;
import io.github.redpanda4552.HifumiBot.command.slash.CommandAbout;
import io.github.redpanda4552.HifumiBot.command.slash.CommandBulkDelete;
import io.github.redpanda4552.HifumiBot.command.slash.CommandCPU;
import io.github.redpanda4552.HifumiBot.command.slash.CommandChartGen;
import io.github.redpanda4552.HifumiBot.command.slash.CommandDynCmd;
import io.github.redpanda4552.HifumiBot.command.slash.CommandEmulog;
import io.github.redpanda4552.HifumiBot.command.slash.CommandGPU;
import io.github.redpanda4552.HifumiBot.command.slash.CommandGameIndex;
import io.github.redpanda4552.HifumiBot.command.slash.CommandPFP;
import io.github.redpanda4552.HifumiBot.command.slash.CommandPerms;
import io.github.redpanda4552.HifumiBot.command.slash.CommandPride;
import io.github.redpanda4552.HifumiBot.command.slash.CommandPrompt;
import io.github.redpanda4552.HifumiBot.command.slash.CommandReload;
import io.github.redpanda4552.HifumiBot.command.slash.CommandRun;
import io.github.redpanda4552.HifumiBot.command.slash.CommandSay;
import io.github.redpanda4552.HifumiBot.command.slash.CommandSerial;
import io.github.redpanda4552.HifumiBot.command.slash.CommandShutdown;
import io.github.redpanda4552.HifumiBot.command.slash.CommandSpamKick;
import io.github.redpanda4552.HifumiBot.command.slash.CommandTranslate;
import io.github.redpanda4552.HifumiBot.command.slash.CommandWarez;
import io.github.redpanda4552.HifumiBot.command.slash.CommandWiki;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class CommandIndex {

    private CommandListUpdateAction commandsToRegister;
    private HashMap<String, AbstractSlashCommand> slashCommands;
    private HashMap<String, AbstractMessageContextCommand> messageCommands;
    private HashMap<String, AbstractUserContextCommand> userCommands;

    /**
     * Create a new CommandIndex and invoke the {@link CommandIndex#rebuild
     * rebuild()} method.
     */
    public CommandIndex() {
        slashCommands = new HashMap<String, AbstractSlashCommand>();
        messageCommands = new HashMap<String, AbstractMessageContextCommand>();
        userCommands = new HashMap<String, AbstractUserContextCommand>();
        rebuild();
    }
    
    /**
     * Rebuild this CommandIndex from the Config object in HifumiBot.
     */
    public void rebuild() {
        commandsToRegister = HifumiBot.getSelf().getJDA().updateCommands();
        rebuildSlash();
        rebuildMessage();
        rebuildUser();
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
        registerSlashCommand(new CommandCPU());
        registerSlashCommand(new CommandGPU());
        registerSlashCommand(new CommandDynCmd());
        registerSlashCommand(new CommandSpamKick());
        registerSlashCommand(new CommandGameIndex());
        registerSlashCommand(new CommandEmulog());
        registerSlashCommand(new CommandTranslate());
        registerSlashCommand(new CommandBulkDelete());
        registerSlashCommand(new CommandSerial());
        registerSlashCommand(new CommandPrompt());
        registerSlashCommand(new CommandChartGen());
        registerSlashCommand(new CommandPride());
    }
    
    public void rebuildMessage() {
        messageCommands.clear();
        registerMessageCommand(new CommandTranslateEN());
        registerMessageCommand(new CommandReverseImage());
    }

    public void rebuildUser() {
        userCommands.clear();
        registerUserCommand(new CommandBan());
    }
    
    public void rebuildDynamic() {
        HashMap<String, DynamicCommand> commands = HifumiBot.getSelf().getDynCmdConfig().dynamicCommands;
        
        for (String commandName : commands.keySet()) {
            if (slashCommands.containsKey(commandName)) {
                Messaging.logInfo("CommandIndex", "rebuildDynamic", "Skipping dynamic command \"" + commandName + "\", found a built-in command with the same name");
                break;
            }
            
            DynamicCommand command = commands.get(commandName);
            SlashCommandData commandData = Commands.slash(command.getName(), command.getDescription());
            HashMap<String, DynamicSubcommand> subcommands = command.getSubcommands();
            
            for (String subcommandName : subcommands.keySet()) {
                DynamicSubcommand subcommand = subcommands.get(subcommandName);
                SubcommandGroupData subgroup = new SubcommandGroupData(subcommand.getName(), subcommand.getDescription());
                HashMap<String, DynamicChoice> choices = subcommand.getChoices();
                
                for (String choiceName : choices.keySet()) {
                    DynamicChoice choice = choices.get(choiceName);
                    SubcommandData subcommandData = new SubcommandData(choice.getName(), choice.getDescription());
                    subcommandData.addOption(OptionType.USER, "mention", "Mention");
                    subgroup.addSubcommands(subcommandData);
                }

                commandData.addSubcommandGroups(subgroup);
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

    private void registerUserCommand(AbstractUserContextCommand userCommand) {
        String name = userCommand.defineUserContextCommand().getName();
        userCommands.put(name, userCommand);
        commandsToRegister.addCommands(userCommand.defineUserContextCommand());
    }
    
    public HashMap<String, AbstractSlashCommand> getSlashCommands() {
        return slashCommands;
    }
    
    public HashMap<String, AbstractMessageContextCommand> getMessageCommands() {
        return messageCommands;
    }

    public HashMap<String, AbstractUserContextCommand> getUserCommands() {
        return userCommands;
    }
}
