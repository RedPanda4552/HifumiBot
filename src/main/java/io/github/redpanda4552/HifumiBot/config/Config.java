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
package io.github.redpanda4552.HifumiBot.config;

import java.util.ArrayList;

public class Config implements IConfig {
    
    @Override
    public ConfigType getConfigType() {
        return ConfigType.CORE;
    }
    
    @Override
    public boolean usePrettyPrint() {
        return true;
    }
    
    public Server server;
    public Channels channels;
    public SlashCommands slashCommands;
    public Roles roles;
    public Permissions permissions;
    public long ninjaInterval;
    public FilterOptions filterOptions;
    public MySQLOptions mysql;
    public EntryBarrierOptions entryBarrierOptions;

    public Config() {
        server = new Server();
        channels = new Channels();
        slashCommands = new SlashCommands();
        roles = new Roles();
        permissions = new Permissions();
        filterOptions = new FilterOptions();
        ninjaInterval = 500;
        mysql = new MySQLOptions();
        entryBarrierOptions = new EntryBarrierOptions();
    }
    
    public class Server {
        public String id;
        
        public Server() {
            id = new String("");
        }
    }

    public class Logging {
        public String memberJoin;
        public String memberLeave;
        public String memberBan;
        public String messageDelete;
        public String messageUpdate;

        public Logging() {
            memberJoin = new String("");
            memberLeave = new String("");
            memberBan = new String("");
            messageDelete = new String("");
            messageUpdate = new String("");
        }
    }

    public class Channels {
        public String systemOutputChannelId;
        public String rulesChannelId;
        public String appealsChannelId;
        public String restrictedCommandChannelId;
        public String pixivChannelId;
        public Logging logging;

        public Channels() {
            systemOutputChannelId = new String("");
            rulesChannelId = new String("");
            appealsChannelId = new String("");
            restrictedCommandChannelId = new String("");
            pixivChannelId = new String("");
            logging = new Logging();
        }
    }
    
    public class SlashCommands {
        public int timeoutSeconds;
        
        public SlashCommands() {
            timeoutSeconds = 60 * 15;
        }
    }

    public class Roles {
        public boolean autoAssignMemberEnabled;
        public String autoAssignMemberRoleId;
        public long autoAssignMemberTimeSeconds;
        public String warezRoleId;

        public Roles() {
            autoAssignMemberEnabled = false;
            autoAssignMemberRoleId = new String("");
            autoAssignMemberTimeSeconds = 60 * 15;
            warezRoleId = new String("");
        }
    }

    public class Permissions {
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
    
    public class FilterOptions {
        public long incidentCooldownMS;
        public int maxIncidents;
        public long blockUrlEditsAfterMinutes;
        public long timeoutDurationMinutes;
        public boolean useLocalDNSFiltering;
        public String localDNSFilterAddress;
        public String filterMessage;
        public String dnsMessage;
        public String spamMessage;
        public String timeoutMessage;
        public String kickMessage;
        
        public FilterOptions() {
            incidentCooldownMS = 1000 * 15;
            maxIncidents = 3;
            blockUrlEditsAfterMinutes = 20;
            timeoutDurationMinutes = 60 * 8;
            useLocalDNSFiltering = false;
            localDNSFilterAddress = new String("");
            filterMessage = new String("");
            dnsMessage = new String("");
            spamMessage = new String("");
            timeoutMessage = new String("");
            kickMessage = new String("");
        }
    }

    public class MySQLOptions {
        public String url;
        public String username;
        public String password;

        public MySQLOptions() {
            url = new String("");
            username = new String("");
            password = new String("");
        }
    }

    public class EntryBarrierOptions {
        public boolean enabled;
        public String userInputChannelId;
        public String expectedUserInput;
        public String entryRoleId;

        public EntryBarrierOptions() {
            enabled = false;
            userInputChannelId = new String("");
            expectedUserInput = new String("");
            entryRoleId = new String("");
        }
    }
}
