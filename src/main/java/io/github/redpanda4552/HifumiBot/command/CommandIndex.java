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

import java.text.Collator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.context.CommandReverseImage;
import io.github.redpanda4552.HifumiBot.command.context.CommandTranslate;
import io.github.redpanda4552.HifumiBot.command.slash.CommandAbout;
import io.github.redpanda4552.HifumiBot.command.slash.CommandBuildNumber;
import io.github.redpanda4552.HifumiBot.command.slash.CommandCPU;
import io.github.redpanda4552.HifumiBot.command.slash.CommandDynCmd;
import io.github.redpanda4552.HifumiBot.command.slash.CommandEmulog;
import io.github.redpanda4552.HifumiBot.command.slash.CommandFilter;
import io.github.redpanda4552.HifumiBot.command.slash.CommandGPU;
import io.github.redpanda4552.HifumiBot.command.slash.CommandGameDB;
import io.github.redpanda4552.HifumiBot.command.slash.CommandHelp;
import io.github.redpanda4552.HifumiBot.command.slash.CommandMemes;
import io.github.redpanda4552.HifumiBot.command.slash.CommandPFP;
import io.github.redpanda4552.HifumiBot.command.slash.CommandPanic;
import io.github.redpanda4552.HifumiBot.command.slash.CommandPerms;
import io.github.redpanda4552.HifumiBot.command.slash.CommandReload;
import io.github.redpanda4552.HifumiBot.command.slash.CommandRun;
import io.github.redpanda4552.HifumiBot.command.slash.CommandSay;
import io.github.redpanda4552.HifumiBot.command.slash.CommandShutdown;
import io.github.redpanda4552.HifumiBot.command.slash.CommandSpamKick;
import io.github.redpanda4552.HifumiBot.command.slash.CommandSupport;
import io.github.redpanda4552.HifumiBot.command.slash.CommandWarez;
import io.github.redpanda4552.HifumiBot.command.slash.CommandWiki;
import io.github.redpanda4552.HifumiBot.config.ConfigManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class CommandIndex {

    private static final int COMMANDS_PER_PAGE = 10;

    private CommandListUpdateAction commandsToRegister;
    private HashMap<String, AbstractSlashCommand> slashCommands;
    private HashMap<String, AbstractMessageContextCommand> messageCommands;
    private HashMap<String, DynamicCommand> dynamicCommands;
    private HashMap<String, ArrayList<MessageEmbed>> helpPages;
    private HashMap<String, HashMap<String, Instant>> history;

    /**
     * Create a new CommandIndex and invoke the {@link CommandIndex#rebuild
     * rebuild()} method.
     */
    public CommandIndex() {
        slashCommands = new HashMap<String, AbstractSlashCommand>();
        messageCommands = new HashMap<String, AbstractMessageContextCommand>();
        dynamicCommands = new HashMap<String, DynamicCommand>();
        history = new HashMap<String, HashMap<String, Instant>>();
        rebuild();
    }
    
    private void cleanupGuildCommands() {
        List<Guild> servers = HifumiBot.getSelf().getJDA().getGuilds();
        
        for (Guild server : servers) {
            List<Command> commands = server.retrieveCommands().complete();
            
            for (Command command : commands) {
                String appId = HifumiBot.getSelf().getJDA().retrieveApplicationInfo().complete().getId();
                String commandAppId = command.getApplicationId();
                
                if (appId.equals(commandAppId)) {
                    command.delete().complete();
                }
            }
        }
    }

    /**
     * Rebuild this CommandIndex from the Config object in HifumiBot.
     */
    public void rebuild() {
        commandsToRegister = HifumiBot.getSelf().getJDA().updateCommands();
        rebuildSlash();
        rebuildMessage();
        rebuildDynamic();
        //cleanupGuildCommands();
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
        registerSlashCommand(new CommandHelp());
        registerSlashCommand(new CommandBuildNumber());
        registerSlashCommand(new CommandMemes());
        registerSlashCommand(new CommandSupport());
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
        dynamicCommands.clear();

        for (DynamicCommand dynamicCommand : HifumiBot.getSelf().getDynCmdConfig().dynamicCommands) {
            dynamicCommands.put(dynamicCommand.getName(), dynamicCommand);
        }

        rebuildHelpPages();
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
    
    public HashMap<String, AbstractSlashCommand> getSlashCommands() {
        return slashCommands;
    }
    
    public HashMap<String, AbstractMessageContextCommand> getMessageCommands() {
        return messageCommands;
    }

    public Set<String> getAll() {
        return dynamicCommands.keySet();
    }

    public boolean isDynamicCommand(String name) {
        return dynamicCommands.get(name) != null;
    }

    public DynamicCommand getDynamicCommand(String name) {
        return dynamicCommands.get(name);
    }

    public void addDynamicCommand(DynamicCommand dyncmd) {
        // Insert it into the ArrayList in Config, then reload the CommandIndex.
        ArrayList<DynamicCommand> configDynamicCommands = HifumiBot.getSelf().getDynCmdConfig().dynamicCommands;
        Iterator<DynamicCommand> iter = configDynamicCommands.iterator();
        DynamicCommand configDynamicCommand = null;
        boolean commandExists = false;

        while (iter.hasNext()) {
            configDynamicCommand = iter.next();

            if (configDynamicCommand.getName().equals(dyncmd.getName())) {
                configDynamicCommand = dyncmd;
                commandExists = true;
            }
        }

        // If no command exists in the iterator, just add it
        if (!commandExists) {
            configDynamicCommands.add(dyncmd);
        }

        ConfigManager.write(HifumiBot.getSelf().getDynCmdConfig());
        HifumiBot.getSelf().getCommandIndex().rebuildDynamic();
    }

    public void deleteDynamicCommand(String name) {
        ArrayList<DynamicCommand> dynamicCommands = HifumiBot.getSelf().getDynCmdConfig().dynamicCommands;
        Iterator<DynamicCommand> iter = dynamicCommands.iterator();
        DynamicCommand toDelete = null;

        while (iter.hasNext()) {
            DynamicCommand dyncmd = iter.next();

            if (dyncmd.getName().equals(name)) {
                toDelete = dyncmd;
                break;
            }
        }

        if (toDelete != null) {
            dynamicCommands.remove(toDelete);
            ConfigManager.write(HifumiBot.getSelf().getDynCmdConfig());
            HifumiBot.getSelf().getCommandIndex().rebuildDynamic();
        }
    }

    /**
     * Get a HashMap<String, TreeSet<String>> organizing commands by their
     * categories. Currently only used to simplify help page generation.
     */
    public HashMap<String, TreeSet<String>> getCategorizedCommandNames() {
        Set<String> commandNames = new HashSet<String>();
        commandNames.addAll(getAll());
        HashMap<String, TreeSet<String>> ret = new HashMap<String, TreeSet<String>>();

        for (String commandName : commandNames) {
            DynamicCommand command = dynamicCommands.get(commandName);
            TreeSet<String> categoryCommands = null;

            if (ret.containsKey(command.getCategory())) {
                categoryCommands = ret.get(command.getCategory());
            } else {
                categoryCommands = new TreeSet<String>(Collator.getInstance());
            }

            categoryCommands.add(commandName);
            ret.put(command.getCategory(), categoryCommands);
        }

        return ret;
    }

    /**
     * Fully rebuilds the help page lists.
     */
    private void rebuildHelpPages() {
        helpPages = new HashMap<String, ArrayList<MessageEmbed>>();
        HashMap<String, TreeSet<String>> commandMap = this.getCategorizedCommandNames();

        for (String category : commandMap.keySet()) {
            int pageCount = (int) Math.ceil((double) commandMap.get(category).size() / COMMANDS_PER_PAGE);
            helpPages.put(category, new ArrayList<MessageEmbed>());
            EmbedBuilder eb = new EmbedBuilder();

            for (String command : commandMap.get(category)) {
                eb.addField(command, this.getDynamicCommand(command).getHelpText(), false);

                if (eb.getFields().size() >= COMMANDS_PER_PAGE) {
                    addToPages(category, eb, pageCount);
                    eb = new EmbedBuilder();
                }
            }

            if (eb.getFields().size() > 0)
                addToPages(category, eb, pageCount);
        }
    }

    private void addToPages(String category, EmbedBuilder eb, int pageCount) {
        eb.setTitle("Help - " + category);
        eb.setDescription("============================ ============================");
        eb.setFooter((helpPages.get(category).size() + 1) + " / " + pageCount);
        helpPages.get(category).add(eb.build());
    }

    public HashMap<String, ArrayList<MessageEmbed>> getHelpPages() {
        return helpPages;
    }
    
    /**
     * Check if this is a ninja command, update command history if not.
     * @param newHistory
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
            } else if (now.minusMillis(HifumiBot.getSelf().getConfig().ninjaInterval).isBefore(subMap.get(channelId))) {
                return true;
            } else {
                subMap.put(channelId, now);
                return false;
            }
        }
    }
}
