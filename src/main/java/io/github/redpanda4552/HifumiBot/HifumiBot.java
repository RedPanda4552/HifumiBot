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

import java.time.Duration;
import java.time.Instant;

import com.deepl.api.Translator;

import io.github.redpanda4552.HifumiBot.command.CommandIndex;
import io.github.redpanda4552.HifumiBot.config.Config;
import io.github.redpanda4552.HifumiBot.config.ConfigManager;
import io.github.redpanda4552.HifumiBot.config.ConfigType;
import io.github.redpanda4552.HifumiBot.config.DynCmdConfig;
import io.github.redpanda4552.HifumiBot.config.EmulogParserConfig;
import io.github.redpanda4552.HifumiBot.config.SettingsIniParserConfig;
import io.github.redpanda4552.HifumiBot.database.SQLite;
import io.github.redpanda4552.HifumiBot.event.AutoModEventListener;
import io.github.redpanda4552.HifumiBot.event.ButtonEventListener;
import io.github.redpanda4552.HifumiBot.event.MemberEventListener;
import io.github.redpanda4552.HifumiBot.event.MessageContextCommandListener;
import io.github.redpanda4552.HifumiBot.event.MessageEventListener;
import io.github.redpanda4552.HifumiBot.event.ModalEventListener;
import io.github.redpanda4552.HifumiBot.event.RoleEventListener;
import io.github.redpanda4552.HifumiBot.event.SelectMenuEventListener;
import io.github.redpanda4552.HifumiBot.event.SlashCommandListener;
import io.github.redpanda4552.HifumiBot.event.UserEventListener;
import io.github.redpanda4552.HifumiBot.permissions.PermissionManager;
import io.github.redpanda4552.HifumiBot.util.Log;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import okhttp3.OkHttpClient;

public class HifumiBot {

    private static HifumiBot self;
    private static String discordBotToken;
    private static String superuserId;
    private static String deepLKey;
    private static boolean traceLogs = false;

    public static void main(String[] args) {
        // Run via environment variables first, if not, fall-back to args
        if (System.getenv().containsKey("DISCORD_BOT_TOKEN") && System.getenv().containsKey("SUPERUSER_ID")) {
            System.out.println("Found environment variables, using those instead of cli-args!");
            discordBotToken = System.getenv("DISCORD_BOT_TOKEN");
            superuserId = System.getenv("SUPERUSER_ID");

            if (System.getenv().containsKey("DEEPL_KEY")) {
                deepLKey = System.getenv("DEEPL_KEY");
            }

            if (System.getenv().containsKey("HIFUMI_TRACE")) {
                traceLogs = Boolean.parseBoolean(System.getenv("HIFUMI_TRACE").toLowerCase());
            }
        } else if (args.length < 2) {
            System.out.println("Usage: java -jar HifumiBot-x.y.z.jar <discord-bot-token> <superuser-id> [deepl-key] [trace-logs]");
            return;
        } else {
            discordBotToken = args[0];
            superuserId = args[1];

            if (args.length >= 3) {
                deepLKey = args[2];
            }

            if (args.length >= 4) {
                traceLogs = Boolean.parseBoolean(args[3].toLowerCase());
            }
            System.out.println("Parsed arguments from command line");
        }

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
    private DynCmdConfig dynCmdConfig;
    private EmulogParserConfig emulogParserConfig;
    private SettingsIniParserConfig settingsIniParserConfig;
    private final OkHttpClient http;
    private SQLite sqlite;
    
    private Scheduler scheduler;
    private CpuIndex cpuIndex;
    private GpuIndex gpuIndex;
    private CommandIndex commandIndex;
    private PermissionManager permissionManager;
    
    private GameIndex gameIndex;
    private Translator deepL;

    public HifumiBot() {
        self = this;
        this.http = new OkHttpClient();

        if (discordBotToken == null || discordBotToken.isEmpty()) {
            System.out.println("Attempted to start with a null or empty Discord bot token!");
            return;
        }

        if (traceLogs) {
            Log.init();
        }
        
        Log.info("Initializing JDA instance");

        try {
            jda = JDABuilder.createDefault(discordBotToken)
                    .enableIntents(GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableCache(CacheFlag.CLIENT_STATUS)
                    .setAutoReconnect(true)
                    .build().awaitReady();
        } catch (Exception e) {
            Messaging.logException("HifumiBot", "(constructor)", e);
        }

        Log.info("Setting status to Starting...");
        updateStatus("Starting...");

        // Load configurations. Main config file is essential, if something goes wrong,
        // leave its exception unhandled so the constructor will halt.
        //
        // After loading any config, write it back immediately so that if any new fields
        // were added after an update, they are written to disk
        Log.info("Initializing main config");
        ConfigManager.createConfigIfNotExists(ConfigType.CORE);
        config = (Config) ConfigManager.read(ConfigType.CORE);
        ConfigManager.write(config);

        try {
            Log.info("Initializing dyncmd config");
            ConfigManager.createConfigIfNotExists(ConfigType.DYNCMD);
            dynCmdConfig = (DynCmdConfig) ConfigManager.read(ConfigType.DYNCMD);
            ConfigManager.write(dynCmdConfig);

            Log.info("Initializing emulog config");
            ConfigManager.createConfigIfNotExists(ConfigType.EMULOG_PARSER);
            emulogParserConfig = (EmulogParserConfig) ConfigManager.read(ConfigType.EMULOG_PARSER);
            ConfigManager.write(emulogParserConfig);

            Log.info("Initializing settings ini config");
            ConfigManager.createConfigIfNotExists(ConfigType.SETTINGS_PARSER);
            settingsIniParserConfig = (SettingsIniParserConfig) ConfigManager.read(ConfigType.SETTINGS_PARSER);
            ConfigManager.write(settingsIniParserConfig);
        } catch (Exception e) {
            Log.error(e);
            Messaging.logException("HifumiBot", "(constructor)", e);
        }
        
        Log.info("Calling constructors");
        sqlite = new SQLite();
        deepL = new Translator(deepLKey);
        scheduler = new Scheduler();
        cpuIndex = new CpuIndex();
        gpuIndex = new GpuIndex();
        commandIndex = new CommandIndex();
        permissionManager = new PermissionManager(superuserId);
        jda.addEventListener(new RoleEventListener());
        jda.addEventListener(new MessageEventListener());
        jda.addEventListener(new MemberEventListener());
        jda.addEventListener(new UserEventListener());
        jda.addEventListener(new ButtonEventListener());
        jda.addEventListener(new SelectMenuEventListener());
        jda.addEventListener(new SlashCommandListener());
        jda.addEventListener(new MessageContextCommandListener());
        jda.addEventListener(new ModalEventListener());
        jda.addEventListener(new AutoModEventListener());
        gameIndex = new GameIndex();

        Log.info("Refreshing anything refreshable");
        scheduler.runOnce(() -> {
            cpuIndex.refresh();
            gpuIndex.refresh();
            gameIndex.refresh();
        });

        // Schedule repeating tasks
        Log.info("Scheduling repeating tasks");

        scheduler.scheduleRepeating("cpu", () -> {
            HifumiBot.getSelf().getCpuIndex().refresh();
        }, 1000 * 60 * 60 * 24);

        scheduler.scheduleRepeating("gpu", () -> {
            HifumiBot.getSelf().getGpuIndex().refresh();
        }, 1000 * 60 * 60 * 24);
        
        scheduler.scheduleRepeating("gdb", () -> {
            HifumiBot.getSelf().getGameIndex().refresh();
        }, 1000 * 60 * 60 * 4);

        scheduler.scheduleRepeating("beb", () -> {
            Instant currentTime = Instant.now();

            for (Long eventIdLong : BrowsableEmbed.embedCache.keySet()) {
                BrowsableEmbed embed = BrowsableEmbed.embedCache.get(eventIdLong);
                Instant createdTime = Instant.ofEpochSecond(embed.getCreatedTimestamp());

                if (Duration.between(createdTime, currentTime).toHours() > 6) {
                    BrowsableEmbed.embedCache.remove(eventIdLong);
                }
            }
        }, 1000 * 60 * 60 * 6);

        Log.info("Setting status to New Game!");
        updateStatus("New Game!");
    }

    public Config getConfig() {
        return config;
    }

    public DynCmdConfig getDynCmdConfig() {
        return dynCmdConfig;
    }

    public EmulogParserConfig getEmulogParserConfig() {
        return emulogParserConfig;
    }

    public SettingsIniParserConfig getSettingsIniParserConfig() {
        return settingsIniParserConfig;
    }

    public OkHttpClient getHttpClient() {
        return http;
    }

    public SQLite getSQLite() {
        return sqlite;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public CpuIndex getCpuIndex() {
        return cpuIndex;
    }

    public GpuIndex getGpuIndex() {
        return gpuIndex;
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
    
    public GameIndex getGameIndex() {
        return gameIndex;
    }
    
    public Translator getDeepL() {
        return deepL;
    }

    public void shutdown(boolean reload) {
        this.getJDA().getPresence().setActivity(Activity.watching("Shutting Down..."));
        this.getScheduler().shutdown();
        jda.shutdown();
        this.getSQLite().shutdown();

        if (reload)
            self = new HifumiBot();
    }

    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }
}
