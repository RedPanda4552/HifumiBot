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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import io.github.redpanda4552.HifumiBot.util.Messaging;

public class ConfigManager {

    public static void createConfigIfNotExists(ConfigType configType) {
        try {
            File file = new File(configType.getPath());
            
            if (file.exists() == false) {
                file.createNewFile();
                write(new Config());
            }
        } catch (IOException e) {
            Messaging.logException("ConfigManager", "createConfigIfNotExists", e);
        }
    }

    public static IConfig read(ConfigType configType) {
        try {
            File file = new File(configType.getPath());
            InputStream iStream = Files.newInputStream(file.toPath());
            String json = new String(iStream.readAllBytes());
            Gson gson = new Gson();
            return gson.fromJson(json, TypeToken.get(configType.getConfigClass()).getType());
        } catch (IOException e) {
            Messaging.logException("ConfigManager", "read", e);
        }

        return null;
    }

    public static void write(IConfig config) {
        try {
            File file = new File(config.getConfigType().getPath());
            OutputStream oStream = Files.newOutputStream(file.toPath());
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(config, TypeToken.get(config.getConfigType().getConfigClass()).getType());
            oStream.write(json.getBytes());
        } catch (IOException e) {
            Messaging.logException("ConfigManager", "write", e);
        }
    }
    
    public static long getSizeBytes(ConfigType configType) {
        try {
            File file = new File(configType.getPath());
            return file.length();
        } catch (Exception e) {
            Messaging.logException("ConfigManager", "getSizeBytes", e);
        }
        
        return -1;
    }
}
