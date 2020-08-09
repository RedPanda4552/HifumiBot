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
package io.github.redpanda4552.HifumiBot.wiki;

public class RegionSet {

    private String region = "", serial = "", release = "", crc = "", windowsStatus = "", linuxStatus = "";
    private int windowsStatusColor = -1, linuxStatusColor = -1;
    
    public RegionSet() {
        
    }
    
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
        return region != null && 
                serial != null && 
                crc != null && 
                windowsStatus != null && 
                windowsStatusColor != -1 && 
                linuxStatus != null && 
                linuxStatusColor != -1;
    }
}
