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

import java.util.ArrayList;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;

public class NewMemberMessage {

    private String title, body;
    private ArrayList<Field> fields = new ArrayList<Field>();
    private transient MessageEmbed embed;
    
    public void rebuild() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(title);
        eb.setDescription(body);
        
        for (Field field : fields) {
            eb.addField(field);
        }
        
        if ((title != null && !title.trim().isEmpty()) || (body != null && !body.trim().isEmpty()) || fields.size() > 0) {
            embed = eb.build();
        }
    }
    
    public MessageEmbed asEmbed() {
        return embed;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
        rebuild();
    }
    
    public String getBody() {
        return body;
    }
    
    public void setBody(String body) {
        this.body = body;
        rebuild();
    }
    
    public ArrayList<Field> getFields() {
        return fields;
    }
    
    public void addField(Field field) {
        fields.add(field);
        rebuild();
    }
    
    public void removeField(Field field) {
        fields.remove(field);
        rebuild();
    }
}
