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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;

import io.github.redpanda4552.HifumiBot.command.DynamicCommand;
import io.github.redpanda4552.HifumiBot.filter.Filter;

public class Config
{

    public String devBuildOutputChannelId;
    public String systemOutputChannelId;
    public String rulesChannelId;
    public String warezRoleId;
    public String pastebinApiKey;
    public ArrayList<String> adminRoles;
    public String restrictedCommandChannelId;
    public ArrayList<String> restrictedCommandBypassRoles;
    public HashMap<String, Filter> filters;
    public ArrayList<DynamicCommand> dynamicCommands;
    public HashMap<String, OffsetDateTime> warezUsers;

    public Config()
    {
        devBuildOutputChannelId = new String("");
        systemOutputChannelId = new String("");
        rulesChannelId = new String("");
        warezRoleId = new String("");
        pastebinApiKey = new String("");
        adminRoles = new ArrayList<String>();
        restrictedCommandChannelId = new String("");
        restrictedCommandBypassRoles = new ArrayList<String>();
        filters = new HashMap<String, Filter>();
        dynamicCommands = new ArrayList<DynamicCommand>();
        warezUsers = new HashMap<String, OffsetDateTime>();
    }
}
