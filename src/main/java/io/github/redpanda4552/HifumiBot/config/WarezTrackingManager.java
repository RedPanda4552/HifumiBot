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

import io.github.redpanda4552.HifumiBot.util.Messaging;

public class WarezTrackingManager
{
    private static final String WAREZ_PATH = "./warez-tracking.json";
    public static final File file = new File(WAREZ_PATH);

    public static void createIfNotExists()
    {
        try
        {
            if (file.exists() == false)
            {
                file.createNewFile();
                write(new WarezTracking());
            }
        }
        catch (IOException e)
        {
            Messaging.logException("WarezTrackingManager", "createIfNotExists", e);
        }
    }

    public static WarezTracking read()
    {
        try
        {
            InputStream iStream = Files.newInputStream(file.toPath());
            String json = new String(iStream.readAllBytes());
            Gson gson = new Gson();
            return gson.fromJson(json, WarezTracking.class);
        }
        catch (IOException e)
        {
            Messaging.logException("WarezTrackingManager", "read", e);
        }

        return null;
    }

    public static void write(WarezTracking warezTracking)
    {
        try
        {
            OutputStream oStream = Files.newOutputStream(file.toPath());
            Gson gson = new GsonBuilder().create();
            String json = gson.toJson(warezTracking);
            oStream.write(json.getBytes());
        }
        catch (IOException e)
        {
            Messaging.logException("WarezTrackingManager", "write", e);
        }
    }
}
