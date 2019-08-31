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

import org.apache.commons.lang3.StringUtils;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.filtering.Filter;
import io.github.redpanda4552.HifumiBot.util.CommandMeta;
import io.github.redpanda4552.HifumiBot.util.EmbedUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;

public class CommandFilter extends AbstractCommand {

    private final MessageEmbed usage;
    
    public CommandFilter(HifumiBot hifumiBot) {
        super(hifumiBot, true, CATEGORY_BUILTIN);
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Filter Usage");
        eb.addField("List all filters", "`filter list`", false);
        eb.addField("View a filter", "`filter view <name>`", false);
        eb.addField("Reload the filter table", "`filter reload`", false);
        eb.addField("Add a filter", "`filter add <name> <responseTitle> <responseBody>`", false);
        eb.addField("Remove a filter", "`filter remove <name>`", false);
        eb.addField("Add an activation word", "`filter addword <name> <word>`", false);
        eb.addField("Remove an activation word", "`filter removeword <name> <word>`", false);
        eb.addField("Modify an attribute of a filter", "`filter set <name> <attribute> <value>`", false);
        eb.addField("Filter attributes (description) [accepted values]", "wholeWord (require whole word matching) [true/false]\ndeleteSource (delete the message that triggered this filter) [true/false]\nrequireAll (require all activation words to be present to activate the filter [true/false]\nresponseTitle (the title text of the filter's response) [any text]\nresponseBody (the body text of the filter's response) [any text]", false);
        usage = eb.build();
    }

    @Override
    protected void onExecute(CommandMeta cm) {
        if (cm.getArgs().length < 1) {
            hifumiBot.sendMessage(cm.getChannel(), usage);
            return;
        }
        
        String subCommand = cm.getArgs()[0];
        EmbedBuilder eb = null;
        
        switch (subCommand.toLowerCase()) {
        case "list":
            if (cm.getMember() != null)
                eb = EmbedUtil.newFootedEmbedBuilder(cm.getMember());
            else
                eb = EmbedUtil.newFootedEmbedBuilder(cm.getUser());
            
            eb.setTitle("Filter List");
            
            for (String name : hifumiBot.getFilterController().getFilterList()) {
                eb.addField(name, "", true);
            }
            
            hifumiBot.sendMessage(cm.getChannel(), eb.build());
            break;
        case "view":
            if (cm.getArgs().length < 2) {
                hifumiBot.sendMessage(cm.getChannel(), usage);
                return;
            }
            
            Filter filter = hifumiBot.getFilterController().getFilter(cm.getArgs()[1]);
            
            if (filter != null) {
                if (cm.getMember() != null)
                    eb = EmbedUtil.newFootedEmbedBuilder(cm.getMember());
                else 
                    eb = EmbedUtil.newFootedEmbedBuilder(cm.getUser());
                
                eb.setTitle(filter.getName());
                eb.setDescription("Reacts to words:\n" + StringUtils.join(filter.getActivationWords(), ", "));
                eb.addField("Response Title", filter.getResponseTitle(), false);
                eb.addField("Response Body", filter.getResponseBody(), false);
                eb.addField("Whole Word", String.valueOf(filter.wholeWord()), true);
                eb.addField("Delete Source", String.valueOf(filter.deleteSource()), true);
                eb.addField("Require All", String.valueOf(filter.requireAll()), true);
                hifumiBot.sendMessage(cm.getChannel(), eb.build());
            } else {
                hifumiBot.sendMessage(cm.getChannel(), "Could not find filter named `" + cm.getArgs()[1] + "`.");
            }
            
            break;
        case "reload":
            if (hifumiBot.getFilterController().refreshFilters()) {
                hifumiBot.sendMessage(cm.getChannel(), "Filters reloaded.");
            } else {
                hifumiBot.sendMessage(cm.getChannel(), "Cound not reload filters.");
            }
            
            break;
        case "add":
            if (cm.getArgs().length < 4) {
                hifumiBot.sendMessage(cm.getChannel(), usage);
                return;
            }
            
            if (hifumiBot.getFilterController().insertFilter(cm.getArgs()[1], cm.getArgs()[2], cm.getArgs()[3])) {
                hifumiBot.sendMessage(cm.getChannel(), "Successfully created empty filter `" + cm.getArgs()[1] + "`.");
            } else {
                hifumiBot.sendMessage(cm.getChannel(), "Failed to create filter `" + cm.getArgs()[1] + "`.");
            }
            
            break;
        case "remove":
            if (cm.getArgs().length < 2) {
                hifumiBot.sendMessage(cm.getChannel(), usage);
                return;
            }
            
            if (hifumiBot.getFilterController().removeFilter(cm.getArgs()[1])) {
                hifumiBot.sendMessage(cm.getChannel(), "Successfully removed filter `" + cm.getArgs()[1] + "`.");
            } else {
                hifumiBot.sendMessage(cm.getChannel(), "Failed to remove filter `" + cm.getArgs()[1] + "`.");
            }
            
            break;
        case "addword":
            if (cm.getArgs().length < 3) {
                hifumiBot.sendMessage(cm.getChannel(), usage);
                return;
            }
            
            if (hifumiBot.getFilterController().addActivationWord(cm.getArgs()[1], cm.getArgs()[2])) {
                hifumiBot.sendMessage(cm.getChannel(), "Successfully added word to filter `" + cm.getArgs()[1] + "`.");
            } else {
                hifumiBot.sendMessage(cm.getChannel(), "Failed to add word to filter `" + cm.getArgs()[1] + "`.");
            }
            
            break;
        case "removeword":
            if (cm.getArgs().length < 3) {
                hifumiBot.sendMessage(cm.getChannel(), usage);
                return;
            }
            
            if (hifumiBot.getFilterController().removeActivationWord(cm.getArgs()[1], cm.getArgs()[2])) {
                hifumiBot.sendMessage(cm.getChannel(), "Successfully removed word from filter `" + cm.getArgs()[1] + "`.");
            } else {
                hifumiBot.sendMessage(cm.getChannel(), "Failed to remove word from filter `" + cm.getArgs()[1] + "`.");
            }
            
            break;
        case "set":
            if (cm.getArgs().length < 4) {
                hifumiBot.sendMessage(cm.getChannel(), usage);
                return;
            }
            
            if (hifumiBot.getFilterController().updateFilter(cm.getArgs()[1], cm.getArgs()[2], cm.getArgs()[3])) {
                hifumiBot.sendMessage(cm.getChannel(), "Successfully updated filter `" + cm.getArgs()[1] + "`.");
            } else {
                hifumiBot.sendMessage(cm.getChannel(), "Failed to update filter `" + cm.getArgs()[1] + "`.");
            }
            
            break;
        }
    }

    @Override
    protected String getHelpText() {
        return "Manage text filters";
    }

}
