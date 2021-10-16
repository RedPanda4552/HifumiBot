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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.github.redpanda4552.HifumiBot.util.Messaging;

public class BuildCommitMap implements IConfig {

    @Override
    public ConfigType getConfigType() {
        return ConfigType.BUILDMAP;
    }

    private HashMap<Long, String> buildIdToCommit;

    public BuildCommitMap() {
        buildIdToCommit = new HashMap<Long, String>();
    }

    public void seedMap() {
        // Seed the map from a resources file
        try {
            InputStream stream = getClass().getResourceAsStream("/fixture-build-map.json");
            String content = new String(stream.readAllBytes());

            Gson gson = new Gson();
            var mapType = new TypeToken<Map<Long, String>>() {}.getType();
            Map<Long, String> seedMap = gson.fromJson(content, mapType);
            seedMap.forEach(this::putCommit);
        } catch (Exception e) {
            Messaging.logException(this.getClass().getSimpleName(), "seedMap", e);
        }
    }

    public Optional<String> getCommitSha(Long buildId) {
        if (buildIdToCommit.containsKey(buildId)) {
            return Optional.of(buildIdToCommit.get(buildId));
        }
        return Optional.empty();
    }

    public void putCommit(Long buildId, String commitSha) {
        buildIdToCommit.put(buildId, commitSha);
    }
}
