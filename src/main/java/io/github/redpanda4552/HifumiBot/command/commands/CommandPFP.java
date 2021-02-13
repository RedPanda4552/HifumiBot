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
package io.github.redpanda4552.HifumiBot.command.commands;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.CommandMeta;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class CommandPFP extends AbstractCommand {

    public CommandPFP() {
        super("pfp", CATEGORY_BUILTIN, true);
    }

    @Override
    protected void onExecute(CommandMeta cm) {
        if (cm.getUser() == null || !HifumiBot.getSelf().getPermissionManager().isSuperuser(cm.getUser())) {
            return;
        }
        
        List<Attachment> attachments = cm.getMessage().getAttachments();
        List<MessageEmbed> embeds = cm.getMessage().getEmbeds();
        
        if (attachments.size() > 0) {
            Attachment attachment = attachments.get(0);
            
            if (attachment.isImage()) {
                try {
                    setAvatar(attachment.getUrl());
                    Messaging.sendMessage(cm.getChannel(), "Avatar set!");
                } catch (IOException e) {
                    e.printStackTrace();
                    Messaging.sendMessage(cm.getChannel(), "An error occurred while setting the avatar: "+ e.getMessage());
                }
                
                return;
            }
        } else if (embeds.size() > 0) {
            MessageEmbed embed = embeds.get(0);
            
            if (embed.getType() == EmbedType.IMAGE) {
                try {
                    setAvatar(embed.getImage().getUrl());
                    Messaging.sendMessage(cm.getChannel(), "Avatar set!");
                } catch (IOException e) {
                    e.printStackTrace();
                    Messaging.sendMessage(cm.getChannel(), "An error occurred while setting the avatar: "+ e.getMessage());
                }
                
                return;
            }
        }
        
        Messaging.sendMessage(cm.getChannel(), "No images found in this message! Either attach one or include a link in the command arguments.");
    }

    @Override
    public String getHelpText() {
        return "Set Hifumi's avatar";
    }
    
    private void setAvatar(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        BufferedImage bImage = ImageIO.read(url);
        ByteArrayOutputStream oStream = new ByteArrayOutputStream();
        ImageIO.write(bImage, "png", oStream);
        HifumiBot.getSelf().getJDA().getSelfUser().getManager().setAvatar(Icon.from(oStream.toByteArray())).complete();
    }
}
