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
package io.github.redpanda4552.HifumiBot.command;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.util.CommandMeta;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;

public class CommandDynCmd extends AbstractCommand {

    private final MessageEmbed usage;
    
    public CommandDynCmd(HifumiBot hifumiBot) {
        super(hifumiBot, true, CATEGORY_BUILTIN);
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("DynCmd Usage");
        eb.addField("Add an empty command", "`dyncmd add <name> <helpText> <category>`", false);
        eb.addField("Modify an attribute of a command", "`dyncmd set <name> <attribute> <value>`", false);
        eb.addField("Remove a command", "`dyncmd remove <name>`", false);
        eb.addField("Command attributes (description) [accepted values]","admin (controls if admin permissions are needed) [true/false]\ncategory (help category) [any text]\ntitle (display title) [any text]\nbody (display body) [any text]\nimageUrl (attached image) [url to image]", false);
        usage = eb.build();
    }

    @Override
    protected void onExecute(CommandMeta cm) {
        if (cm.getArgs().length < 2) {
            hifumiBot.sendMessage(cm.getChannel(), usage);
            return;
        }
        
        String subCommand = cm.getArgs()[0];
        String name = cm.getArgs()[1].toLowerCase();
        
        switch (subCommand.toLowerCase()) {
        case "add":
            if (cm.getArgs().length < 4) {
                hifumiBot.sendMessage(cm.getChannel(), usage);
                return;
            }
            
            if (hifumiBot.getDynamicCommandLoader().insertCommand(name, cm.getArgs()[2], cm.getArgs()[3])) {
                hifumiBot.sendMessage(cm.getChannel(), "Successfully created empty command `" + name + "`.");
            } else {
                hifumiBot.sendMessage(cm.getChannel(), "Failed to create command `" + name + "`.");
            }
            
            break;
        case "set":
            if (cm.getArgs().length < 4) {
                hifumiBot.sendMessage(cm.getChannel(), usage);
                return;
            }
            
            if (hifumiBot.getDynamicCommandLoader().updateCommand(name, cm.getArgs()[2], cm.getArgs()[3])) {
                hifumiBot.sendMessage(cm.getChannel(), "Successfully updated command `" + name + "`.");
            } else {
                hifumiBot.sendMessage(cm.getChannel(), "Failed to update command `" + name + "`.");
            }
            
            break;
        case "remove":
            if (hifumiBot.getDynamicCommandLoader().removeCommand(name)) {
                hifumiBot.sendMessage(cm.getChannel(), "Successfully removed command `" + name + "`.");
            } else {
                hifumiBot.sendMessage(cm.getChannel(), "Failed to remove command `" + name + "`.");
            }
            
            break;
        }
    }

    @Override
    protected String getHelpText() {
        return "Add a dynamic command to HifumiBot";
    }

}
