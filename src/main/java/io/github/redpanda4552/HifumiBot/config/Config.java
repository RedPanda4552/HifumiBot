// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.config;

import io.github.redpanda4552.HifumiBot.filter.Filter;
import java.util.ArrayList;
import java.util.HashMap;

public class Config implements IConfig {

  @Override
  public ConfigType getConfigType() {
    return ConfigType.CORE;
  }

  @Override
  public boolean usePrettyPrint() {
    return true;
  }

  public boolean useLocalDNSFiltering;
  public Server server;
  public Channels channels;
  public SlashCommands slashCommands;
  public Dev dev;
  public Roles roles;
  public Integrations integrations;
  public Permissions permissions;
  public HashMap<String, Filter> filters;
  public long ninjaInterval;
  public FilterOptions filterOptions;
  public boolean enableBotKicker;

  public Config() {
    useLocalDNSFiltering = false;
    server = new Server();
    channels = new Channels();
    slashCommands = new SlashCommands();
    dev = new Dev();
    roles = new Roles();
    integrations = new Integrations();
    permissions = new Permissions();
    filters = new HashMap<String, Filter>();
    filterOptions = new FilterOptions();
    ninjaInterval = 500;
    enableBotKicker = true;
  }

  public static class Server {
    public String id;

    public Server() {
      id = new String("");
    }
  }

  public static class Channels {
    public String systemOutputChannelId;
    public String rulesChannelId;
    public String restrictedCommandChannelId;
    public String pixivChannelId;

    public Channels() {
      systemOutputChannelId = new String("");
      rulesChannelId = new String("");
      restrictedCommandChannelId = new String("");
      pixivChannelId = new String("");
    }
  }

  public static class SlashCommands {
    public int timeoutSeconds;

    public SlashCommands() {
      timeoutSeconds = 60 * 15;
    }
  }

  public static class Dev {
    public boolean sendEmbeds;
    public String windows;
    public String ubuntu;
    public String linux;

    public Dev() {
      sendEmbeds = false;
      windows = new String("");
      ubuntu = new String("");
      linux = new String("");
    }
  }

  public static class Roles {
    public String autoAssignMemberRoleId;
    public long autoAssignMemberTimeSeconds;
    public String warezRoleId;

    public Roles() {
      autoAssignMemberRoleId = new String("");
      autoAssignMemberTimeSeconds = 60 * 15;
      warezRoleId = new String("");
    }
  }

  public static class Integrations {
    public String pastebinApiKey;

    public Integrations() {
      pastebinApiKey = new String("");
    }
  }

  public static class Permissions {
    public ArrayList<String> superAdminRoleIds;
    public ArrayList<String> adminRoleIds;
    public ArrayList<String> modRoleIds;
    public ArrayList<String> blockedRoleIds;

    public Permissions() {
      superAdminRoleIds = new ArrayList<String>();
      adminRoleIds = new ArrayList<String>();
      modRoleIds = new ArrayList<String>();
      blockedRoleIds = new ArrayList<String>();
    }
  }

  public static class FilterOptions {
    public long incidentCooldownMS;
    public int maxIncidents;
    public boolean enableWarningMessages;
    public String warnMessage;
    public String kickMessage;

    public FilterOptions() {
      incidentCooldownMS = 1000 * 10;
      maxIncidents = 5;
      enableWarningMessages = true;
      warnMessage = new String("");
      kickMessage = new String("");
    }
  }
}
