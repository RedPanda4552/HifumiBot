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
package io.github.redpanda4552.HifumiBot;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public class PermissionManager {

    private String superuserId;
    
    public PermissionManager(String superuserId) {
        this.superuserId = superuserId;
    }
    
    public boolean hasPermission(Member member, User user) {
        return isSuperuser(user) || isAdmin(member);
    }
    
    public boolean isSuperuser(User user) {
        if (user == null) {
            return false;
        }
        
        return user.getId().equals(superuserId);
    }
    
    private boolean isAdmin(Member member) {
        // Only commands requiring admin permissions will ever need to test with this method.
        // Thus, if we are in a private channel (where member is always null), where we don't
        // want admin commands to ever be an option, always return false.
        if (member == null) {
            return false;
        }
        
        for (Role role : member.getRoles()) {
            if (HifumiBot.getSelf().getConfig().adminRoles.contains(role.getName())) {
                return true;
            }
        }
        
        return false;
    }
}
