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

public class Config {
    public boolean useLocalDNSFiltering;
    public Server server;
    public Channels channels;
    public SlashCommands slashCommands;
    public Dev dev;
    public Roles roles;
    public Integrations integrations;
    public Permissions permissions;
    public HashMap<String, Filter> filters;

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

        public Channels() {
            devBuildOutputChannelId = new String("");
            systemOutputChannelId = new String("");
            rulesChannelId = new String("");
            restrictedCommandChannelId = new String("");
        }
    }
    
    public class SlashCommands {
        public int timeoutSeconds;
        
        public SlashCommands() {
            timeoutSeconds = 60 * 15;
        }
    }
    
    public class Dev {
        public String windows;
        public String ubuntu;
        public String linux;
        
        public Dev() {
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

        public Permissions() {
            superAdminRoleIds = new ArrayList<String>();
            adminRoleIds = new ArrayList<String>();
            modRoleIds = new ArrayList<String>();
        }
    }
}
