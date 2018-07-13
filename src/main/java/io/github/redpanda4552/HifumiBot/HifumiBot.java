/**
 * This file is part of HifumiBot, licensed under the MIT License (MIT)
 * 
 * Copyright (c) 2018 Brian Wood
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

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;


public class HifumiBot {

    private static HifumiBot self;
    private static String discordBotToken, outputChannelId;
    private static String superuserId;
    private static boolean debug = false;
    
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java -jar HifumiBot-x.y.z.jar <discord-bot-token> <output-channel-id> <superuser-id> [-d]");
            return;
        }
        
        discordBotToken = args[0];
        outputChannelId = args[1];
        superuserId = args[2];
        
        if (args.length >= 4 && args[3].equalsIgnoreCase("-d"))
            debug = true;
        
        System.out.println("Arguments parsed");
        
        while (true) {
            if (self == null) {
                System.out.println("No self instance, creating one");
                self = new HifumiBot();
            }
                
            try {
                Thread.sleep(1000 * 2);
            } catch (InterruptedException e) {
                System.out.println("HifumiBot Interrupt!");
            }
        }
    }
    
    public static HifumiBot getSelf() {
        return self;
    }
    
    private JDA jda;
    private Thread monitorThread;
    
    public HifumiBot() {
        self = this;
        
        if (discordBotToken == null || discordBotToken.isEmpty()) {
            System.out.println("Attempted to start with a null or empty Discord bot token!");
            return;
        }
        
        if (outputChannelId == null || outputChannelId.isEmpty()) {
            System.out.println("Output channel id is null or empty! I won't be able to send messages!");
        }
        
        try {
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(discordBotToken)
                    .setAutoReconnect(true)
                    .addEventListener(new CommandInterpreter(this))
                    .buildBlocking();
        } catch (LoginException | IllegalArgumentException | InterruptedException e) {
            e.printStackTrace();
        }
        
        startMonitor();
    }
    
    public JDA getJDA() {
        return jda;
    }
    
    public boolean isSuperuser(User user) {
        return user.getId().equals(superuserId);
    }
    
    public boolean isAdmin(Member member) {
        for (Role role : member.getRoles()) {
            switch (role.getName().toLowerCase()) {
            case "pcsx2 team":
            case "admin":
            case "bot admin":
            case "bug squad":
                return true;
            }
        }
        
        return false;
    }
    
    public void reinstance() {
    	HifumiBot.getSelf().getJDA().getPresence().setGame(Game.watching("Reinstancing..."));
    	stopMonitor();
        jda.shutdown();
        self = null;
    }
    
    public void stopMonitor() {
    	monitorThread.interrupt();
    }
    
    public void startMonitor() {
    	monitorThread = new Thread(new BuildMonitor(jda.getTextChannelById(outputChannelId), debug), "BuildMonitor");
        monitorThread.start();
        System.out.println("BuildMonitor thread started");
    }
}
