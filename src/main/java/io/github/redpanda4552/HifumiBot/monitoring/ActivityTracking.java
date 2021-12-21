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
package io.github.redpanda4552.HifumiBot.monitoring;

import java.util.ArrayList;
import java.util.HashMap;

import net.dv8tion.jda.api.entities.Message;

public class ActivityTracking {

    private HashMap<String, HeuristicsPackage> heuristics = new HashMap<String, HeuristicsPackage>();
    
    public ActivityTracking() {
        
    }
    
    public synchronized void addToHeuristics(Message message) {
        String userId = message.getAuthor().getId();
        
        if (!heuristics.containsKey(userId)) {
            heuristics.put(userId, new HeuristicsPackage(userId));
        }
        
        HeuristicsPackage hp = heuristics.get(userId);
        hp.addActivity(message);
    }
    
    public synchronized void cleanHeuristics() {
        ArrayList<String> userIds = new ArrayList<String>();
        
        for (String userId : heuristics.keySet()) {
            userIds.add(userId);
        }
        
        for (String userId : userIds) {
            HeuristicsPackage hp = heuristics.get(userId);
            hp.cleanActivities();
            
            if (!hp.hasActivities()) {
                heuristics.remove(userId);
            }
        }
    }
}
