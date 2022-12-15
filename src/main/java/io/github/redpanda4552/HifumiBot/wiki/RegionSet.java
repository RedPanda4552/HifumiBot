// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.wiki;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegionSet {

  private String region = "";
  private String serial = "";
  private String release = "";
  private String crc = "";
  private String windowsStatus = "";
  private String linuxStatus = "";

  private int windowsStatusColor = -1;
  private int linuxStatusColor = -1;

  public boolean isComplete() {
    return region != null
        && serial != null
        && crc != null
        && windowsStatus != null
        && windowsStatusColor != -1
        && linuxStatus != null
        && linuxStatusColor != -1;
  }
}
