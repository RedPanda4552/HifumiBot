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
import io.github.redpanda4552.HifumiBot.command.slash.*;
import io.github.redpanda4552.HifumiBot.config.ConfigManager;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.Command;

public class CommandIndex {

    private static final int COMMANDS_PER_PAGE = 10;

    private HashMap<String, AbstractSlashCommand> slashCommands;
    private HashMap<String, DynamicCommand> commandMap;
    private HashMap<String, ArrayList<MessageEmbed>> helpPages;
    private HashMap<String, HashMap<String, Instant>> history;

    /**
     * Create a new CommandIndex and invoke the {@link CommandIndex#rebuild
     * rebuild()} method.
     */
    public CommandIndex() {
        slashCommands = new HashMap<String, AbstractSlashCommand>();
        commandMap = new HashMap<String, DynamicCommand>();
        rebuildAll();
        history = new HashMap<String, HashMap<String, Instant>>();
    }

    /**
     * Rebuild this CommandIndex from the Config object in HifumiBot.
     */
    public void rebuildAll() {
        rebuildSlash();
        rebuildDynamic();
    }
    
    public void rebuildSlash() {
        slashCommands.clear();
        registerSlashCommand(new CommandSay());
        registerSlashCommand(new CommandAbout());
        registerSlashCommand(new CommandUpsert());
        registerSlashCommand(new CommandWarez());
        registerSlashCommand(new CommandShutdown());
        registerSlashCommand(new CommandReload());
        registerSlashCommand(new CommandWiki());
        registerSlashCommand(new CommandBan());
        registerSlashCommand(new CommandDev());
        registerSlashCommand(new CommandPrompt());
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
    }
    
    public void rebuildDynamic() {
        commandMap.clear();

        for (DynamicCommand dynamicCommand : HifumiBot.getSelf().getDynCmdConfig().dynamicCommands) {
            commandMap.put(dynamicCommand.getName(), dynamicCommand);
        }

        rebuildHelpPages();
    }
    
    public void upsertAllSlashCommands(String mode) {
        String serverId = HifumiBot.getSelf().getConfig().server.id;
        List<Command> commands = HifumiBot.getSelf().getJDA().getGuildById(serverId).retrieveCommands().complete();
        HashMap<String, Command> uploadedCommands = new HashMap<String, Command>();
        
        for (Command command : commands) {
            if (!slashCommands.containsKey(command.getName())) {
                HifumiBot.getSelf().getJDA().getGuildById(serverId).deleteCommandById(command.getId()).complete();
            } else {
                uploadedCommands.put(command.getName(), command);
            }
        }
        
        for (String commandName : slashCommands.keySet()) {
            if (mode != null && mode.equals("all")) {
                upsertSlashCommand(commandName);
            } else if (mode != null && mode.equals("new")) {
                if (!uploadedCommands.containsKey(commandName)) {
                    upsertSlashCommand(commandName);
                }
            }
        }
    }
    
    public boolean upsertSlashCommand(String commandName) {
        return upsertSlashCommand(slashCommands.get(commandName));
    }
    
    public boolean upsertSlashCommand(AbstractSlashCommand slashCommand) {
        try {
            if (slashCommand != null) {
                slashCommand.upsertSlashCommand();
                return true;
            }
        } catch (Exception e) {
            Messaging.logException("CommandIndex", "upsertSlashCommand", e);
        }
        
        return false;
    }
    
    private void registerSlashCommand(AbstractSlashCommand slashCommand) {
        String name = slashCommand.defineSlashCommand().getName();
        slashCommands.put(name, slashCommand);
    }
    
    public HashMap<String, AbstractSlashCommand> getSlashCommands() {
        return slashCommands;
    }

    public Set<String> getAll() {
        return commandMap.keySet();
    }

    public boolean isCommand(String name) {
        return commandMap.get(name) != null;
    }

    public boolean isDynamicCommand(String name) {
        DynamicCommand dyncmd = commandMap.get(name);
        return dyncmd != null && dyncmd instanceof DynamicCommand;
    }

    public DynamicCommand getDynamicCommand(String name) {
        return commandMap.get(name);
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

    public void deleteCommand(String name) {
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
            HifumiBot.getSelf().getCommandIndex().rebuildAll();
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
            DynamicCommand command = commandMap.get(commandName);
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
                eb.addField(">" + command, this.getDynamicCommand(command).getHelpText(), false);

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
