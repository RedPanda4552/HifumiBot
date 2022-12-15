// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot;

import io.github.redpanda4552.HifumiBot.command.CommandIndex;
import io.github.redpanda4552.HifumiBot.config.BuildCommitMap;
import io.github.redpanda4552.HifumiBot.config.Config;
import io.github.redpanda4552.HifumiBot.config.ConfigManager;
import io.github.redpanda4552.HifumiBot.config.ConfigType;
import io.github.redpanda4552.HifumiBot.config.DynCmdConfig;
import io.github.redpanda4552.HifumiBot.config.EmulogParserConfig;
import io.github.redpanda4552.HifumiBot.config.ServerMetrics;
import io.github.redpanda4552.HifumiBot.config.WarezTracking;
import io.github.redpanda4552.HifumiBot.event.EventListener;
import io.github.redpanda4552.HifumiBot.event.MessageContextCommandListener;
import io.github.redpanda4552.HifumiBot.event.SlashCommandListener;
import io.github.redpanda4552.HifumiBot.filter.BotDetection;
import io.github.redpanda4552.HifumiBot.filter.ChatFilter;
import io.github.redpanda4552.HifumiBot.filter.KickHandler;
import io.github.redpanda4552.HifumiBot.permissions.PermissionManager;
import io.github.redpanda4552.HifumiBot.util.Internet;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import io.github.redpanda4552.HifumiBot.wiki.WikiIndex;
import java.time.OffsetDateTime;
import javax.security.auth.login.LoginException;

import lombok.AccessLevel;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import okhttp3.OkHttpClient;

@Getter
public class HifumiBot {

  @Getter(AccessLevel.NONE)
  private static String discordBotToken;

  @Getter private static HifumiBot self;
  @Getter private static String superuserId;

  public static void main(String[] args) {
    // Run via environment variables first, if not, fall-back to args
    if (System.getenv().containsKey("DISCORD_BOT_TOKEN")
        && System.getenv().containsKey("SUPERUSER_ID")) {
      System.out.println("Found environment variables, using those instead of cli-args!");
      discordBotToken = System.getenv("DISCORD_BOT_TOKEN");
      superuserId = System.getenv("SUPERUSER_ID");
    } else if (args.length < 2) {
      System.out.println("Usage: java -jar HifumiBot-x.y.z.jar <discord-bot-token> <superuser-id>");
      return;
    } else {
      discordBotToken = args[0];
      superuserId = args[1];
      System.out.println("Parsed arguments from command line");
    }

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  if (self != null) self.shutdown(false);
                }));

    self = new HifumiBot();
  }

  private JDA jda;
  private Config config;
  private WarezTracking warezTracking;
  private DynCmdConfig dynCmdConfig;
  private BuildCommitMap buildCommitMap;
  private ServerMetrics serverMetrics;
  private EmulogParserConfig emulogParserConfig;
  private final OkHttpClient http;

  private Scheduler scheduler;
  private WikiIndex wikiIndex;
  private CpuIndex cpuIndex;
  private GpuIndex gpuIndex;
  private CommandIndex commandIndex;
  private PermissionManager permissionManager;
  private ChatFilter chatFilter;
  private EventListener eventListener;
  private SlashCommandListener slashCommandListener;
  private MessageContextCommandListener messageCommandListener;
  private KickHandler kickHandler;
  private GameDB gameDB;
  private BotDetection botDetection;

  public HifumiBot() {
    self = this;
    this.http = new OkHttpClient();

    if (discordBotToken == null || discordBotToken.isEmpty()) {
      System.out.println("Attempted to start with a null or empty Discord bot token!");
      return;
    }

    try {
      jda =
          JDABuilder.createDefault(discordBotToken)
              .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
              .setMemberCachePolicy(MemberCachePolicy.ALL)
              .setAutoReconnect(true)
              .build()
              .awaitReady();
    } catch (LoginException | IllegalArgumentException | InterruptedException e) {
      Messaging.logException("HifumiBot", "(constructor)", e);
    }

    updateStatus("Starting...");

    ConfigManager.createConfigIfNotExists(ConfigType.CORE);
    config = (Config) ConfigManager.read(ConfigType.CORE);
    // Write back the config so that if any new fields were added after an
    // update, they are written to disk
    ConfigManager.write(config);
    // TODO - check vital fields and fail if they aren't set (all the channel and serverIds)

    ConfigManager.createConfigIfNotExists(ConfigType.WAREZ);
    warezTracking = (WarezTracking) ConfigManager.read(ConfigType.WAREZ);
    ConfigManager.write(warezTracking);

    ConfigManager.createConfigIfNotExists(ConfigType.DYNCMD);
    dynCmdConfig = (DynCmdConfig) ConfigManager.read(ConfigType.DYNCMD);
    ConfigManager.write(dynCmdConfig);

    ConfigManager.createConfigIfNotExists(ConfigType.BUILDMAP);
    buildCommitMap = (BuildCommitMap) ConfigManager.read(ConfigType.BUILDMAP);
    if (buildCommitMap != null) {
      buildCommitMap.seedMap();
    }
    ConfigManager.write(buildCommitMap);

    ConfigManager.createConfigIfNotExists(ConfigType.SERVER_METRICS);
    serverMetrics = (ServerMetrics) ConfigManager.read(ConfigType.SERVER_METRICS);
    ConfigManager.write(serverMetrics);

    ConfigManager.createConfigIfNotExists(ConfigType.EMULOG_PARSER);
    emulogParserConfig = (EmulogParserConfig) ConfigManager.read(ConfigType.EMULOG_PARSER);
    ConfigManager.write(emulogParserConfig);

    Internet.init();
    scheduler = new Scheduler();
    wikiIndex = new WikiIndex();
    cpuIndex = new CpuIndex();
    gpuIndex = new GpuIndex();
    commandIndex = new CommandIndex();
    permissionManager = new PermissionManager(superuserId);
    chatFilter = new ChatFilter();
    jda.addEventListener(eventListener = new EventListener(this));
    jda.addEventListener(slashCommandListener = new SlashCommandListener());
    jda.addEventListener(messageCommandListener = new MessageContextCommandListener());
    kickHandler = new KickHandler();
    gameDB = new GameDB();
    botDetection = new BotDetection();

    // Schedule repeating tasks
    scheduler.scheduleRepeating(
        "wiki",
        () -> {
          HifumiBot.getSelf().getWikiIndex().refresh();
        },
        1000 * 60 * 60 * 24);

    scheduler.scheduleRepeating(
        "cpu",
        () -> {
          HifumiBot.getSelf().getCpuIndex().refresh();
        },
        1000 * 60 * 60 * 24);

    scheduler.scheduleRepeating(
        "gpu",
        () -> {
          HifumiBot.getSelf().getGpuIndex().refresh();
        },
        1000 * 60 * 60 * 24);

    scheduler.scheduleRepeating(
        "ints",
        () -> {
          HifumiBot.getSelf().getSlashCommandListener().cleanInteractionElements();
        },
        1000L * getConfig().slashCommands.timeoutSeconds);

    scheduler.scheduleRepeating(
        "fltr",
        () -> {
          kickHandler.flush();
        },
        1000 * 60 * 60);

    scheduler.scheduleRepeating(
        "pop",
        () -> {
          String serverId = HifumiBot.getSelf().getConfig().server.id;
          Guild server = HifumiBot.getSelf().getJda().getGuildById(serverId);
          serverMetrics.populationSnaps.put(
              OffsetDateTime.now().toString(), server.getMemberCount());
          ConfigManager.write(serverMetrics);
        },
        1000 * 60 * 60 * 6);

    scheduler.scheduleRepeating(
        "bot",
        () -> {
          botDetection.clean();
        },
        1000 * 60 * 15);

    updateStatus("New Game!");
  }

  private void updateStatus(String str) {
    jda.getPresence().setActivity(Activity.watching(str));
  }

  public void shutdown(boolean reload) {
    this.getJda().getPresence().setActivity(Activity.watching("Shutting Down..."));
    this.getScheduler().shutdown();
    jda.shutdown();

    if (reload) self = new HifumiBot();
  }

  public String getVersion() {
    return getClass().getPackage().getImplementationVersion();
  }
}
