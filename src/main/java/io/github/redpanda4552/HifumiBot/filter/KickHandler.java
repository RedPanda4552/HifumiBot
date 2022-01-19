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
package io.github.redpanda4552.HifumiBot.filter;

import java.time.Instant;
import java.util.HashMap;

import org.apache.commons.lang3.tuple.Pair;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.permissions.PermissionLevel;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class KickHandler {

    private HashMap<String, Pair<Instant, Integer>> indexes;
    
    public KickHandler() {
        indexes = new HashMap<String, Pair<Instant, Integer>>();
    }
    
    public synchronized void storeIncident(Member member, Instant newInstant) {
        if (HifumiBot.getSelf().getPermissionManager().hasPermission(PermissionLevel.MOD, member)) {
            return;
        }
        
        User user = member.getUser();
        String userId = user.getId();
        
        if (!indexes.containsKey(userId)) {
            indexes.put(userId, Pair.of(newInstant, 1));
        } else {
            Pair<Instant, Integer> p = indexes.get(userId);
            Instant oldInstant = p.getLeft();
            Instant cooldownEnd = oldInstant.plusMillis(HifumiBot.getSelf().getConfig().filterOptions.incidentCooldownMS);
            
            if (newInstant.isAfter(cooldownEnd)) {
                indexes.put(userId, Pair.of(newInstant, 1));
            } else {
                Integer toStore = p.getRight() + 1;
                
                if (toStore >= HifumiBot.getSelf().getConfig().filterOptions.maxIncidents) {
                    indexes.remove(userId);
                    
                    try {
                        doKick(member);
                        Messaging.logInfo("KickHandler", "storeIncident", "Successfully messaged and kicked " + member.getUser().getAsMention() + " (" + member.getUser().getName() + "#" + member.getUser().getDiscriminator() + ") for exceeding the maximum number of filter incidents.");
                    } catch (Exception e) {
                        Messaging.logException("KickHandler", "storeIncident", e);
                    }
                } else {
                    indexes.put(userId, Pair.of(newInstant, toStore));
                }
            }
        }
    }
    
    public synchronized void doKick(Member member) {
        Messaging.sendPrivateMessage(member.getUser(), HifumiBot.getSelf().getConfig().filterOptions.kickMessage);
        member.kick().complete();
    }
    
    public synchronized void doKickForBotJoin(Member member) {
        StringBuilder sb = new StringBuilder("**You have been automatically kicked from the PCSX2 server.**\n\n");
        sb.append("Our bot has detected a raid by bot accounts, and you have attempted to join our server at the same time as those bots.\n\n");
        sb.append("**If you have no idea why you are receiving this message:** Your account is compromised and being used as a spam bot on Discord. Change your password as soon as you can.\n\n");
        sb.append("**If you legitimately attempted to join our server:** Sorry, but we will continue to automatically kick until the bot raid ends. Please wait for a bit and try to join at a later time.\n\n");
        sb.append("Thank you for understanding, stay safe.");
        Messaging.sendPrivateMessage(member.getUser(), sb.toString());
        member.kick().complete();
    }
    
    public synchronized void flush() {
        Instant now = Instant.now();
        
        for (String key : indexes.keySet()) {
            Pair<Instant, Integer> p = indexes.get(key);
            Instant cooldownEnd = p.getLeft().plusMillis(HifumiBot.getSelf().getConfig().filterOptions.incidentCooldownMS);
            
            if (now.isAfter(cooldownEnd)) {
                indexes.remove(key);
            }
        }
    }
}
