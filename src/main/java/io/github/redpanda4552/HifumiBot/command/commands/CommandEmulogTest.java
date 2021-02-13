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

import java.util.List;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.CommandMeta;
import io.github.redpanda4552.HifumiBot.parse.EmulogParser;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.entities.Message.Attachment;

public class CommandEmulogTest extends AbstractCommand {

    public CommandEmulogTest() {
        super("emulogtest", CATEGORY_BUILTIN, false);
    }

    @Override
    protected void onExecute(CommandMeta cm) {
        List<Attachment> attachments = cm.getMessage().getAttachments();
        
        if (attachments.size() != 1) {
            Messaging.sendMessage(cm.getChannel(), "No file attached! Please attach your emulog to your message.");
            return;
        }
        
        Attachment attachment = attachments.get(0);
        
        if (!attachment.getFileName().equalsIgnoreCase("emulog.txt")) {
            Messaging.sendMessage(cm.getChannel(), "Attached file was not an emulog!");
            return;
        }
        
        EmulogParser ep = new EmulogParser(cm.getMessage(), attachment);
        HifumiBot.getSelf().getScheduler().runOnce(ep);
    }

    @Override
    public String getHelpText() {
        return "Check an emulog for information/errors";
    }

}
