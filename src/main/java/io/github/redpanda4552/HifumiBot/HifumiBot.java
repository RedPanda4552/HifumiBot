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

import com.deepl.api.Translator;

import io.github.redpanda4552.HifumiBot.command.CommandIndex;
import io.github.redpanda4552.HifumiBot.config.Config;
import io.github.redpanda4552.HifumiBot.config.ConfigManager;
import io.github.redpanda4552.HifumiBot.config.ConfigType;
import io.github.redpanda4552.HifumiBot.config.DynCmdConfig;
import io.github.redpanda4552.HifumiBot.config.EmulogParserConfig;
import io.github.redpanda4552.HifumiBot.database.MySQL;
import io.github.redpanda4552.HifumiBot.event.MemberEventListener;
import io.github.redpanda4552.HifumiBot.event.MessageContextCommandListener;
import io.github.redpanda4552.HifumiBot.event.MessageEventListener;
import io.github.redpanda4552.HifumiBot.event.ModalEventListener;
import io.github.redpanda4552.HifumiBot.event.RoleEventListener;
import io.github.redpanda4552.HifumiBot.event.SlashCommandListener;
import io.github.redpanda4552.HifumiBot.filter.FilterHandler;
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
    private static String deepLKey;

    public static void main(String[] args) {
        // Run via environment variables first, if not, fall-back to args
        if (System.getenv().containsKey("DISCORD_BOT_TOKEN") && System.getenv().containsKey("SUPERUSER_ID")) {
            System.out.println("Found environment variables, using those instead of cli-args!");
            discordBotToken = System.getenv("DISCORD_BOT_TOKEN");
            superuserId = System.getenv("SUPERUSER_ID");

            if (System.getenv().containsKey("DEEPL_KEY")) {
                deepLKey = System.getenv("DEEPL_KEY");
            }
        } else if (args.length < 2) {
            System.out.println("Usage: java -jar HifumiBot-x.y.z.jar <discord-bot-token> <superuser-id> [deepl-key]");
            return;
        } else {
            discordBotToken = args[0];
            superuserId = args[1];

            if (args.length >= 3) {
                deepLKey = args[2];
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
    private final OkHttpClient http;
    private MySQL mySQL;
    
    private Scheduler scheduler;
    private WikiIndex wikiIndex;
    private CpuIndex cpuIndex;
    private GpuIndex gpuIndex;
    private CommandIndex commandIndex;
    private PermissionManager permissionManager;
    private FilterHandler chatFilter;
    
    private RoleEventListener roleEventListener;
    private MessageEventListener messageEventListener;
    private MemberEventListener memberEventListener;
    private SlashCommandListener slashCommandListener;
    private MessageContextCommandListener messageCommandListener;
    private ModalEventListener modalEventListener;
    
    private GameIndex gameIndex;
    private Translator deepL;

    public HifumiBot() {
        self = this;
        this.http = new OkHttpClient();

        if (discordBotToken == null || discordBotToken.isEmpty()) {
            System.out.println("Attempted to start with a null or empty Discord bot token!");
            return;
        }

        try {
            jda = JDABuilder.createDefault(discordBotToken)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .setAutoReconnect(true)
                    .build().awaitReady();
        } catch (Exception e) {
            Messaging.logException("HifumiBot", "(constructor)", e);
        }

        updateStatus("Starting...");

        ConfigManager.createConfigIfNotExists(ConfigType.CORE);
        config = (Config) ConfigManager.read(ConfigType.CORE);
        // Write back the config so that if any new fields were added after an
        // update, they are written to disk
        ConfigManager.write(config);

        try {
            ConfigManager.createConfigIfNotExists(ConfigType.DYNCMD);
            dynCmdConfig = (DynCmdConfig) ConfigManager.read(ConfigType.DYNCMD);
            ConfigManager.write(dynCmdConfig);

            ConfigManager.createConfigIfNotExists(ConfigType.EMULOG_PARSER);
            emulogParserConfig = (EmulogParserConfig) ConfigManager.read(ConfigType.EMULOG_PARSER);
            ConfigManager.write(emulogParserConfig);
        } catch (Exception e) {
            Messaging.logException("HifumiBot", "(constructor)", e);
        }
        
        mySQL = new MySQL();
        Internet.init();
        deepL = new Translator(deepLKey);
        scheduler = new Scheduler();
        wikiIndex = new WikiIndex();
        cpuIndex = new CpuIndex();
        gpuIndex = new GpuIndex();
        commandIndex = new CommandIndex();
        permissionManager = new PermissionManager(superuserId);
        chatFilter = new FilterHandler();
        jda.addEventListener(roleEventListener = new RoleEventListener());
        jda.addEventListener(messageEventListener = new MessageEventListener());
        jda.addEventListener(memberEventListener = new MemberEventListener());
        jda.addEventListener(slashCommandListener = new SlashCommandListener());
        jda.addEventListener(messageCommandListener = new MessageContextCommandListener());
        jda.addEventListener(modalEventListener = new ModalEventListener());
        gameIndex = new GameIndex();

        scheduler.runOnce(() -> {
            wikiIndex.refresh();
            cpuIndex.refresh();
            gpuIndex.refresh();
            gameIndex.refresh();
        });

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
        
        scheduler.scheduleRepeating("gdb", () -> {
            HifumiBot.getSelf().getGameIndex().refresh();
        }, 1000 * 60 * 60 * 4);

        scheduler.scheduleRepeating("ints", () -> {
            HifumiBot.getSelf().getSlashCommandListener().cleanInteractionElements();
        }, 1000 * getConfig().slashCommands.timeoutSeconds);
        
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

    public OkHttpClient getHttpClient() {
        return http;
    }

    public MySQL getMySQL() {
        return mySQL;
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

    public FilterHandler getFilterHandler() {
        return chatFilter;
    }

    public RoleEventListener getRoleEventListener() {
        return roleEventListener;
    }

    public MessageEventListener getMessageEventListener() {
        return messageEventListener;
    }

    public MemberEventListener getMemberEventListener() {
        return memberEventListener;
    }
    
    public SlashCommandListener getSlashCommandListener() {
        return slashCommandListener;
    }
    
    public MessageContextCommandListener getMessageCommandListener() {
        return messageCommandListener;
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
        this.getMySQL().shutdown();

        if (reload)
            self = new HifumiBot();
    }

    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }
}
