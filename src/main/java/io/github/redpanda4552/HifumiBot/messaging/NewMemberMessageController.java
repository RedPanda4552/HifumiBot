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
package io.github.redpanda4552.HifumiBot.messaging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import net.dv8tion.jda.api.entities.User;

public class NewMemberMessageController {

    private static final String FILE_PATH = "./NewMemberMessage.cfg";
    
    private File file;
    private NewMemberMessage newMemberMessage;
    
    public NewMemberMessageController() {
        file = new File(FILE_PATH);
        
        try {
            if (file.exists()) {
                Gson gson = new Gson();
                newMemberMessage = gson.fromJson(new FileReader(file), NewMemberMessage.class);
            } else {
                newMemberMessage = new NewMemberMessage();
            }
            
            newMemberMessage.rebuild();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public boolean saveConfig() {
        newMemberMessage.rebuild();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        
        try {
            BufferedWriter writer = Files.newBufferedWriter(file.toPath());
            writer.write(gson.toJson(newMemberMessage));
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    public boolean isEnabled() {
        String title = newMemberMessage.getTitle();
        String body = newMemberMessage.getBody();
        return (title != null && !title.trim().isEmpty()) || (body != null && !body.trim().isEmpty());
    }
    
    public void sendMessage(User user) {
        if (isEnabled()) {
            HifumiBot.getSelf().sendMessage(user.openPrivateChannel().complete(), newMemberMessage.asEmbed());
        }
    }
    
    public NewMemberMessage getNewMemberMessage() {
        return newMemberMessage;
    }
}
