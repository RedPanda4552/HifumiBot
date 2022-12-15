// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.config;

public enum ConfigType {
  CORE("./hifumi-conf.json", Config.class),
  WAREZ("./warez-tracking.json", WarezTracking.class),
  DYNCMD("./dyncmd-config.json", DynCmdConfig.class),
  BUILDMAP("./build-map.json", BuildCommitMap.class),
  SERVER_METRICS("./server-metrics.json", ServerMetrics.class),
  EMULOG_PARSER("./emulog-parser.json", EmulogParserConfig.class);

  private String path;
  private Class<?> clazz;

  private ConfigType(String path, Class<?> clazz) {
    this.path = path;
    this.clazz = clazz;
  }

  public String getPath() {
    return path;
  }

  public Class<?> getConfigClass() {
    return clazz;
  }
}
