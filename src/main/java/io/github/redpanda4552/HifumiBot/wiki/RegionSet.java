// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.wiki;

public class RegionSet {

  private String region = "",
      serial = "",
      release = "",
      crc = "",
      windowsStatus = "",
      linuxStatus = "";
  private int windowsStatusColor = -1, linuxStatusColor = -1;

  public RegionSet() {}

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getSerial() {
    return serial;
  }

  public void setSerial(String serial) {
    this.serial = serial;
  }

  public String getRelease() {
    return release;
  }

  public void setRelease(String release) {
    this.release = release;
  }

  public String getCRC() {
    return crc;
  }

  public void setCRC(String crc) {
    this.crc = crc;
  }

  public String getWindowsStatus() {
    return windowsStatus;
  }

  public void setWindowsStatus(String windowsStatus) {
    this.windowsStatus = windowsStatus;
  }

  public int getWindowsStatusColor() {
    return windowsStatusColor;
  }

  public void setWindowsStatusColor(int windowsStatusColor) {
    this.windowsStatusColor = windowsStatusColor;
  }

  public String getLinuxStatus() {
    return linuxStatus;
  }

  public void setLinuxStatus(String linuxStatus) {
    this.linuxStatus = linuxStatus;
  }

  public int getLinuxStatusColor() {
    return linuxStatusColor;
  }

  public void setLinuxStatusColor(int linuxStatusColor) {
    this.linuxStatusColor = linuxStatusColor;
  }

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
