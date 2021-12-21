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
import java.util.HashMap;

import io.github.redpanda4552.HifumiBot.filter.Filter;

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
    public Activities activities;

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
        activities = new Activities();
    }
    
    public class Server {
        public String id;
        
        public Server() {
            id = new String("");
        }
    }

    public class Channels {
        public String devBuildOutputChannelId;
        public String systemOutputChannelId;
        public String rulesChannelId;
        public String restrictedCommandChannelId;
        public String pixivChannelId;

        public Channels() {
            devBuildOutputChannelId = new String("");
            systemOutputChannelId = new String("");
            rulesChannelId = new String("");
            restrictedCommandChannelId = new String("");
            pixivChannelId = new String("");
        }
    }
    
    public class SlashCommands {
        public int timeoutSeconds;
        
        public SlashCommands() {
            timeoutSeconds = 60 * 15;
        }
    }
    
    public class Dev {
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

    public class Roles {
        public String warezRoleId;

        public Roles() {
            warezRoleId = new String("");
        }
    }

    public class Integrations {
        public String pastebinApiKey;

        public Integrations() {
            pastebinApiKey = new String("");
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
        public String kickMessage;
        
        public FilterOptions() {
            incidentCooldownMS = 1000 * 10;
            maxIncidents = 5;
            kickMessage = new String("");
        }
    }
    
    public class Activities {
        public Heuristics heuristics;
        public int activityExpirationMS;
        
        
        public Activities() {
            heuristics = new Heuristics();
            activityExpirationMS = 1000 * 30;
        }
    }
    
    public class Heuristics {
        public int failingScore;
        public long minMessageIntervalMS;
        public int minMessageInterval_Points;
        public long minimumConsistentIntervalMS;
        public int minimumConsistentInterval_Points;
        public int excessivePings;
        public int excessivePings_Points;
        public boolean considerMentionEveryone;
        public int considerMentionEveryone_Points;
        public int duplicatesCount;
        public int duplicatesCount_Points;
        public float channelSwitchFrequency;
        public int channelSwitchFrequency_Points;
        
        public Heuristics() {
            failingScore = 10;
            minMessageIntervalMS = 100;
            minMessageInterval_Points = 6;
            minimumConsistentIntervalMS = 50;
            minimumConsistentInterval_Points = 4;
            excessivePings = 5;
            excessivePings_Points = 4;
            considerMentionEveryone = true;
            considerMentionEveryone_Points = 4;
            duplicatesCount = 2;
            duplicatesCount_Points = 4;
            channelSwitchFrequency = 0.85f;
            channelSwitchFrequency_Points = 3;
        }
    }
}
