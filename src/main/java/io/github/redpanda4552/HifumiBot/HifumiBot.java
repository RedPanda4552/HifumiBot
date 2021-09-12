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
package io.github.redpanda4552.HifumiBot;

import javax.security.auth.login.LoginException;

import io.github.redpanda4552.HifumiBot.command.CommandIndex;
import io.github.redpanda4552.HifumiBot.command.CommandInterpreter;
import io.github.redpanda4552.HifumiBot.config.Config;
import io.github.redpanda4552.HifumiBot.config.ConfigManager;
import io.github.redpanda4552.HifumiBot.config.DynCmdConfig;
import io.github.redpanda4552.HifumiBot.config.DynCmdConfigManager;
import io.github.redpanda4552.HifumiBot.config.WarezTracking;
import io.github.redpanda4552.HifumiBot.config.WarezTrackingManager;
import io.github.redpanda4552.HifumiBot.event.EventListener;
import io.github.redpanda4552.HifumiBot.event.SlashCommandListener;
import io.github.redpanda4552.HifumiBot.filter.ChatFilter;
import io.github.redpanda4552.HifumiBot.permissions.PermissionManager;
import io.github.redpanda4552.HifumiBot.util.Internet;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import io.github.redpanda4552.HifumiBot.wiki.WikiIndex;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import okhttp3.OkHttpClient;

public class HifumiBot {

    private static HifumiBot self;
    private static String discordBotToken;
    private static String superuserId;
    private static boolean doSlashCommandUpsert = false;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java -jar HifumiBot-x.y.z.jar <discord-bot-token> <superuser-id> [-u]");
            return;
        }

        discordBotToken = args[0];
        superuserId = args[1];
        
        if (args.length >= 3) {
            if (args[2].equals("-u")) {
                doSlashCommandUpsert = true;
            }
        }

        System.out.println("Arguments parsed");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (self != null)
                self.shutdown(false);
        }));

        self = new HifumiBot();
    }

    public static HifumiBot getSelf() {
        return self;
    }
    
    public static String getSuperuserId() {
        return superuserId;
    }

    private JDA jda;
    private Config config;
    private WarezTracking warezTracking;
    private DynCmdConfig dynCmdConfig;
    private final OkHttpClient http;

    private Scheduler scheduler;
    private WikiIndex wikiIndex;
    private CpuIndex cpuIndex;
    private GpuIndex gpuIndex;
    private BuildMonitor buildMonitor;
    private CommandIndex commandIndex;
    private PermissionManager permissionManager;
    private CommandInterpreter commandInterpreter;
    private ChatFilter chatFilter;
    private EventListener eventListener;
    private SlashCommandListener slashCommandListener;

    public HifumiBot() {
        self = this;
        this.http = new OkHttpClient();

        if (discordBotToken == null || discordBotToken.isEmpty()) {
            System.out.println("Attempted to start with a null or empty Discord bot token!");
            return;
        }

        try {
            jda = JDABuilder.createDefault(discordBotToken).enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .setMemberCachePolicy(MemberCachePolicy.ALL).setAutoReconnect(true).build().awaitReady();
        } catch (LoginException | IllegalArgumentException | InterruptedException e) {
            Messaging.logException("HifumiBot", "(constructor)", e);
        }

        updateStatus("Starting...");

        ConfigManager.createConfigIfNotExists();
        config = ConfigManager.read();
        // Write back the config so that if any new fields were added after an
        // update, they are written to disk
        ConfigManager.write(config);

        WarezTrackingManager.createIfNotExists();
        warezTracking = WarezTrackingManager.read();
        WarezTrackingManager.write(warezTracking);

        DynCmdConfigManager.createIfNotExists();
        dynCmdConfig = DynCmdConfigManager.read();
        DynCmdConfigManager.write(dynCmdConfig);

        Internet.init();
        scheduler = new Scheduler();
        wikiIndex = new WikiIndex();
        cpuIndex = new CpuIndex();
        gpuIndex = new GpuIndex();
        buildMonitor = new BuildMonitor(
                jda.getTextChannelById(HifumiBot.getSelf().getConfig().channels.devBuildOutputChannelId));
        commandIndex = new CommandIndex();
        permissionManager = new PermissionManager(superuserId);
        commandInterpreter = new CommandInterpreter(this);
        chatFilter = new ChatFilter();
        jda.addEventListener(eventListener = new EventListener(this));
        jda.addEventListener(slashCommandListener = new SlashCommandListener());

        // Schedule repeating tasks
        scheduler.scheduleRepeating("wiki", () -> {
            HifumiBot.getSelf().getWikiIndex().refresh();
        }, 1000 * 60 * 60 * 24);

        scheduler.scheduleRepeating("cpu", () -> {
            HifumiBot.getSelf().getCpuIndex().refresh();
        }, 1000 * 60 * 60 * 24);

        scheduler.scheduleRepeating("gpu", () -> {
            HifumiBot.getSelf().getGpuIndex().refresh();
        }, 1000 * 60 * 60 * 24);

        scheduler.scheduleRepeating("dev", () -> {
            HifumiBot.getSelf().getBuildMonitor().refresh();
        }, 1000 * 60 * 10);
        
        scheduler.scheduleRepeating("ints", () -> {
            HifumiBot.getSelf().getSlashCommandListener().cleanInteractionElements();
        }, 1000 * getConfig().slashCommands.timeoutSeconds);

        if (doSlashCommandUpsert) {
            commandIndex.upsertSlashCommands("all");
        }

        updateStatus(">help");
    }

    public Config getConfig() {
        return config;
    }

    public WarezTracking getWarezTracking() {
        return warezTracking;
    }

    public DynCmdConfig getDynCmdConfig() {
        return dynCmdConfig;
    }

    public OkHttpClient getHttpClient() {
        return http;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public WikiIndex getWikiIndex() {
        return wikiIndex;
    }

    public CpuIndex getCpuIndex() {
        return cpuIndex;
    }

    public GpuIndex getGpuIndex() {
        return gpuIndex;
    }

    public BuildMonitor getBuildMonitor() {
        return buildMonitor;
    }

    public CommandIndex getCommandIndex() {
        return commandIndex;
    }

    private void updateStatus(String str) {
        jda.getPresence().setActivity(Activity.watching(str));
    }

    public JDA getJDA() {
        return jda;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public CommandInterpreter getCommandInterpreter() {
        return commandInterpreter;
    }

    public ChatFilter getChatFilter() {
        return chatFilter;
    }

    public EventListener getEventListener() {
        return eventListener;
    }
    
    public SlashCommandListener getSlashCommandListener() {
        return slashCommandListener;
    }

    public void shutdown(boolean reload) {
        this.getJDA().getPresence().setActivity(Activity.watching("Shutting Down..."));
        this.getScheduler().shutdown();
        jda.shutdown();

        if (reload)
            self = new HifumiBot();
    }

    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }
}
