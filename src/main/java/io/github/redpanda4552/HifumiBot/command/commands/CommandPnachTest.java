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
import io.github.redpanda4552.HifumiBot.parse.PnachParser;
import net.dv8tion.jda.api.entities.Message.Attachment;

public class CommandPnachTest extends AbstractCommand {

    public CommandPnachTest() {
        super("pnachtest", CATEGORY_BUILTIN, false);
    }

    @Override
    protected void onExecute(CommandMeta cm) {
        List<Attachment> attachments = cm.getMessage().getAttachments();
        
        if (attachments.size() != 1) {
            HifumiBot.getSelf().sendMessage(cm.getChannel(), "No file attached! Please attach your PNACH to your message.");
            return;
        }
        
        Attachment attachment = attachments.get(0);
        
        if (!attachment.getFileExtension().equalsIgnoreCase("pnach")) {
            HifumiBot.getSelf().sendMessage(cm.getChannel(), "Attached file was not a PNACH!");
            return;
        }
        
        PnachParser pp = new PnachParser(cm.getMessage(), attachment);
        HifumiBot.getSelf().getScheduler().runOnce(pp);
    }

    @Override
    public String getHelpText() {
        return "Test if a pnach is valid";
    }

}
