// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

public class ConfigManager {

  public static void createConfigIfNotExists(ConfigType configType) {
    try {
      File file = new File(configType.getPath());
      if (!file.exists() && file.createNewFile()) {
        FileOutputStream oStream = new FileOutputStream(file);
        // Fragile, but if the config system reads an empty file it initializes the config to null!
        oStream.write("{}".getBytes());
        oStream.flush();
        oStream.close();
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
      iStream.close();
      Gson gson = new Gson();
      return gson.fromJson(json, TypeToken.get(configType.getClazz()).getType());
    } catch (IOException e) {
      Messaging.logException("ConfigManager", "read", e);
    }

    return null;
  }

  public static void write(IConfig config) {
    if (config == null) {
      System.out.print("Cannot write config, was given null!");
      return;
    }
    try {
      File file = new File(config.getConfigType().getPath());
      OutputStream oStream = Files.newOutputStream(file.toPath());
      GsonBuilder builder = new GsonBuilder();
      Gson gson = config.usePrettyPrint() ? builder.setPrettyPrinting().create() : builder.create();
      String json = gson.toJson(config, TypeToken.get(config.getConfigType().getClazz()).getType());
      oStream.write(json.getBytes());
      oStream.flush();
      oStream.close();
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
