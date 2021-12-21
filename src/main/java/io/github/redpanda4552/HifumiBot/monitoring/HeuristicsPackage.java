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

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.entities.Message;

public class HeuristicsPackage {

    private String userId;
    private ArrayList<ActivityBlob> activities = new ArrayList<ActivityBlob>();
    
    private ArrayList<Interval> intervals = new ArrayList<Interval>();
    private long sumIntervalMS;
    private long minIntervalMS;
    private long maxIntervalMS;
    private long meanIntervalMS;
    private long medianIntervalMS;
    private int channelSwitches = 0;
    private float channelSwitchFreq = 0f;
    private long sumIntervalDiffsMS = 0;
    private long meanIntervalDiffsMS = 0;
    private int identicalMessageSends = 0;
    private int maxMentions = 0;
    private boolean mentionsEveryone = false;
    private int identicalMessages = 0;
    
    public HeuristicsPackage(String userId) {
        this.userId = userId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public synchronized void addActivity(Message message) {
        activities.add(new ActivityBlob(message));
        recalculateHeuristics(true);
    }
    
    public synchronized void cleanActivities() {
        activities.removeIf(activityBlob -> ( Duration.between(OffsetDateTime.now(), activityBlob.getCreatedTime()).toMillis() > HifumiBot.getSelf().getConfig().activities.activityExpirationMS ));
    }
    
    public synchronized boolean hasActivities() {
        return activities.size() > 0;
    }
    
    private void recalculateHeuristics(boolean doDecision) {
        if (activities.size() < 2) {
            return;
        }
        
        activities.sort((act1, act2) -> {
            if (act1.getCreatedTime().isBefore(act2.getCreatedTime())) {
                return -1;
            } else if (act1.getCreatedTime().isAfter(act2.getCreatedTime())) {
                return 1;
            }
            
            return 0;
        });
        
        intervals = new ArrayList<Interval>();
        sumIntervalMS = 0;
        minIntervalMS = Long.MAX_VALUE;
        maxIntervalMS = Long.MIN_VALUE;
        meanIntervalMS = 0;
        medianIntervalMS = 0;
        channelSwitches = 0;
        channelSwitchFreq = 0;
        sumIntervalDiffsMS = 0;
        meanIntervalDiffsMS = 0;
        identicalMessageSends = 0;
        maxMentions = 0;
        mentionsEveryone = false;
        identicalMessages = 0;
        
        ActivityBlob lastActivity = activities.get(0);
        Interval lastInterval = null;
        
        for (ActivityBlob activity : activities) {
            if (activity == lastActivity) {
                continue;
            }
            
            Interval inter = new Interval(lastActivity, activity);
            intervals.add(inter);
            sumIntervalMS += inter.getTimeBetweenMS();
            
            if (inter.getTimeBetweenMS() < minIntervalMS) {
                minIntervalMS = inter.getTimeBetweenMS();
            }
            
            if (inter.getTimeBetweenMS() > maxIntervalMS) {
                maxIntervalMS = inter.getTimeBetweenMS();
            }
            
            if (inter.isDifferentChannel()) {
                channelSwitches++;
            }
            
            if (lastInterval != null) {
                sumIntervalDiffsMS += Math.abs(inter.getTimeBetweenMS() - lastInterval.getTimeBetweenMS());
            }
            
            if (activity.getTotalMentionCount() > maxMentions) {
                maxMentions = activity.getTotalMentionCount();
            }
            
            if (activity.hasMentionedEveryone()) {
                mentionsEveryone = true;
            }
            
            if (inter.isIdenticalMessage()) {
                identicalMessages++;
            }
            
            lastInterval = inter;
            lastActivity = activity;
        }
        
        channelSwitchFreq = channelSwitches / (activities.size() - 1);
        
        if (intervals.size() >= 2) {
            meanIntervalMS = sumIntervalMS / intervals.size();
            medianIntervalMS = intervals.get(intervals.size() / 2).getTimeBetweenMS();
            meanIntervalDiffsMS = sumIntervalDiffsMS / (intervals.size() - 1);
        }
                
        if (doDecision) {
            decideIfNeedsAction();
        }
    }
    
    private void decideIfNeedsAction() {
        int points = 0;
        
        if (minIntervalMS < HifumiBot.getSelf().getConfig().activities.heuristics.minMessageIntervalMS) {
            points += HifumiBot.getSelf().getConfig().activities.heuristics.minMessageInterval_Points; 
        }
        
        if (meanIntervalDiffsMS < HifumiBot.getSelf().getConfig().activities.heuristics.minimumConsistentIntervalMS) {
            points += HifumiBot.getSelf().getConfig().activities.heuristics.minimumConsistentInterval_Points;
        }
        
        if (maxMentions >= HifumiBot.getSelf().getConfig().activities.heuristics.excessivePings) {
            points += HifumiBot.getSelf().getConfig().activities.heuristics.excessivePings_Points * (maxMentions / HifumiBot.getSelf().getConfig().activities.heuristics.excessivePings);
        }
        
        if (mentionsEveryone && HifumiBot.getSelf().getConfig().activities.heuristics.considerMentionEveryone) {
            points += HifumiBot.getSelf().getConfig().activities.heuristics.considerMentionEveryone_Points;
        }
        
        if (identicalMessages >= HifumiBot.getSelf().getConfig().activities.heuristics.duplicatesCount) {
            points += HifumiBot.getSelf().getConfig().activities.heuristics.duplicatesCount_Points;
        }
        
        if (channelSwitchFreq > HifumiBot.getSelf().getConfig().activities.heuristics.channelSwitchFrequency) {
            points += HifumiBot.getSelf().getConfig().activities.heuristics.channelSwitchFrequency_Points;
        }
        
        if (points > HifumiBot.getSelf().getConfig().activities.heuristics.failingScore) {
            StringBuilder sb = new StringBuilder("User has failed heuristics and might be a bot.\n");
            sb.append(userId + " / " + HifumiBot.getSelf().getJDA().getUserById(userId).getName() + "\n");
            sb.append("```\n");
            sb.append("heuristic | value | threshold\n");
            sb.append("=====================================\n");
            sb.append("minMessageIntervalMS | " + minIntervalMS + " | " + HifumiBot.getSelf().getConfig().activities.heuristics.minMessageIntervalMS + "\n");
            sb.append("minimumConsistentIntervalMS | " + meanIntervalDiffsMS + " | " + HifumiBot.getSelf().getConfig().activities.heuristics.minimumConsistentIntervalMS + "\n");
            sb.append("excessivePings | " + maxMentions + " | " + HifumiBot.getSelf().getConfig().activities.heuristics.excessivePings + "\n");
            sb.append("considerMentionEveryone | " + mentionsEveryone + " | " + HifumiBot.getSelf().getConfig().activities.heuristics.considerMentionEveryone + "\n");
            sb.append("duplicatesCount | " + identicalMessages + " | " + HifumiBot.getSelf().getConfig().activities.heuristics.duplicatesCount + "\n");
            sb.append("channelSwitchFrequency | " + channelSwitchFreq + " | " + HifumiBot.getSelf().getConfig().activities.heuristics.channelSwitchFrequency + "\n");
            sb.append("```");
            sb.append("Points awarded: " + points + " / " + HifumiBot.getSelf().getConfig().activities.heuristics.failingScore);
            
            Messaging.logInfo("HeuristicsPackage", "decideIfNeedsAction", sb.toString());
        }
    }
}
