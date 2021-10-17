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
package io.github.redpanda4552.HifumiBot.command.slash;

import java.util.HashMap;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import io.github.redpanda4552.HifumiBot.command.DynamicCommand;
import io.github.redpanda4552.HifumiBot.permissions.PermissionLevel;
import io.github.redpanda4552.HifumiBot.util.EmbedUtil;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

public class CommandSupport extends AbstractSlashCommand {

    public CommandSupport() {
        super(PermissionLevel.GUEST);
    }

    @Override
    protected void onExecute(SlashCommandEvent event) {
        DynamicCommand dyncmd = HifumiBot.getSelf().getCommandIndex().getDynamicCommand(event.getSubcommandName());
        
        if (dyncmd != null) {
            EmbedBuilder eb;

            if (event.getMember() != null) {
                eb = EmbedUtil.newFootedEmbedBuilder(event.getMember());
            } else {
                eb = EmbedUtil.newFootedEmbedBuilder(event.getUser());
            }

            eb.setTitle(dyncmd.getTitle());
            eb.setDescription(dyncmd.getBody());
            eb.setImage(dyncmd.getImageURL());
            event.replyEmbeds(eb.build()).queue();
        } else {
            event.reply("Whoops! Looks like that dynamic command isn't linked correctly. Please let staff know about this!").setEphemeral(true).queue();
        }
    }

    @Override
    protected CommandData defineSlashCommand() {
        // Dirty hack: When CommandIndex constructor requests this definition,
        // just give back the name it needs.
        if (HifumiBot.getSelf().getCommandIndex() == null) {
            return new CommandData("support", "Main command for support");
        }
        
        HashMap<String, SubcommandGroupData> subcommandGroups = new HashMap<String, SubcommandGroupData>();
        
        for (String dyncmdName : HifumiBot.getSelf().getCommandIndex().getAllDynCmdNames()) {
            DynamicCommand dyncmd = HifumiBot.getSelf().getCommandIndex().getDynamicCommand(dyncmdName);
            
            if (!dyncmd.getCategory().equals("support")) {
                continue;
            }
            
            if (dyncmd.getSubGroup() != null && !dyncmd.getSubGroup().isBlank()) {
                SubcommandGroupData sgd = subcommandGroups.get(dyncmd.getSubGroup());
                
                if (sgd == null) {
                    if (subcommandGroups.size() >= 25) {
                        Messaging.logInfo("CommandSupport", "defineSlashCommand", "Hit limit of 25 subcommand groups, ignoring any others");
                        continue;
                    }
                    
                    sgd = new SubcommandGroupData(dyncmd.getSubGroup(), dyncmd.getSubGroup());
                }
                
                if (sgd.getSubcommands().size() >= 25) {
                    Messaging.logInfo("CommandSupport", "defineSlashCommand", "Hit limit of 25 subcommands in group " + dyncmd.getSubGroup() + ", ignoring any others");
                    continue;
                }
                
                SubcommandData subCommand = new SubcommandData(dyncmd.getName().toLowerCase(), dyncmd.getHelpText());
                sgd.addSubcommands(subCommand);
                subcommandGroups.put(dyncmd.getSubGroup(), sgd);
            }
        }
        
        return new CommandData("support", "Main command for support")
                .addSubcommandGroups(subcommandGroups.values());
    }

}
