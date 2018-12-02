/**
 * This file is part of HifumiBot, licensed under the MIT License (MIT)
 * 
 * Copyright (c) 2017 Brian Wood
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
package io.github.redpanda4552.HifumiBot.command;

import java.util.ArrayList;

import org.apache.commons.lang3.ArrayUtils;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;

public class CommandDynCmd extends AbstractCommand {

    private final MessageEmbed usage;
    
    public CommandDynCmd(HifumiBot hifumiBot) {
        super(hifumiBot, true);
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("DynCmd Usage");
        eb.addField("Add an empty command", "`dyncmd add <name> <helpText>`", false);
        eb.addField("Modify an attribute of a command", "`dyncmd set <name> <attribute> <value>`", false);
        eb.addField("Remove a command", "`dyncmd remove <name>`", false);
        eb.addField("Command attributes (description) [accepted values]","admin (controls if admin permissions are needed) [true/false]\ntitle (display title) [any text]\nbody (display body) [any text]\nimageUrl (attached image) [url to image]", false);
        usage = eb.build();
    }

    @Override
    protected void onExecute(MessageChannel channel, Member sender, String[] args) {
        if (args.length < 2) {
            hifumiBot.sendMessage(channel, usage);
            return;
        }
        
        String subCommand = args[0];
        String name = args[1].toLowerCase();
        
        switch (subCommand.toLowerCase()) {
        case "add":
            if (args.length < 3) {
                hifumiBot.sendMessage(channel, usage);
                return;
            }
            
            args = ArrayUtils.remove(args, 0);
            args = ArrayUtils.remove(args, 0);
            
            String helpText = stringify(args);
            
            if (hifumiBot.getDynamicCommandLoader().insertCommand(name, helpText)) {
                hifumiBot.sendMessage(channel, "Successfully created empty command `" + name + "`. Changes will apply after the next `>reload`.");
            } else {
                hifumiBot.sendMessage(channel, "Failed to create command `" + name + "`.");
            }
            
            break;
        case "set":
            if (args.length < 4) {
                hifumiBot.sendMessage(channel, usage);
                return;
            }
            
            String attribute = args[2];
            args = ArrayUtils.remove(args, 0);
            args = ArrayUtils.remove(args, 0);
            args = ArrayUtils.remove(args, 0);
            String value = stringify(args);
            
            if (hifumiBot.getDynamicCommandLoader().updateCommand(name, attribute, value)) {
                hifumiBot.sendMessage(channel, "Successfully updated command `" + name + "`. Changes will apply after the next `>reload`.");
            } else {
                hifumiBot.sendMessage(channel, "Failed to update command `" + name + "`.");
            }
            
            break;
        case "remove":
            if (hifumiBot.getDynamicCommandLoader().removeCommand(name)) {
                hifumiBot.sendMessage(channel, "Successfully removed command `" + name + "`. Changes will apply after the next `>reload`.");
            } else {
                hifumiBot.sendMessage(channel, "Failed to remove command `" + name + "`.");
            }
            
            break;
        }
    }

    @Override
    protected String getHelpText() {
        return "Add a dynamic command to HifumiBot";
    }

}
