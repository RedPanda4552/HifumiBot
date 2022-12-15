// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BuildCommitMap implements IConfig {

  @Override
  public ConfigType getConfigType() {
    return ConfigType.BUILDMAP;
  }

  @Override
  public boolean usePrettyPrint() {
    return false;
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
