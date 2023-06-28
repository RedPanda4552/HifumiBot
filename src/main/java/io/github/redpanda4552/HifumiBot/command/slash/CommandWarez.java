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

import java.time.OffsetDateTime;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import io.github.redpanda4552.HifumiBot.config.ConfigManager;
import io.github.redpanda4552.HifumiBot.permissions.PermissionLevel;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public class CommandWarez extends AbstractSlashCommand {
    
    @Override
    protected void onExecute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        
        try {
            MessageCreateBuilder mb = new MessageCreateBuilder();
            EmbedBuilder eb = new EmbedBuilder();
            OptionMapping userOpt = event.getOption("user");
            
            if (userOpt != null) {
                Member member = event.getOption("user").getAsMember();
                User user = event.getOption("user").getAsUser();

                if (member == null && user != null) {
                    HifumiBot.getSelf().getWarezTracking().warezUsers.put(user.getId(), OffsetDateTime.now());
                    ConfigManager.write(HifumiBot.getSelf().getWarezTracking());
                    event.getHook().sendMessage("Warez record logged (User has already left the server)").queue();
                    return;
                } else if (user == null) {
                    event.getHook().sendMessage("Could not record warez; supplied user parameter did not match any actual users").queue();
                    return;
                }

                mb.setContent(member.getAsMention());
                
                if (member != null && !HifumiBot.getSelf().getPermissionManager().hasPermission(PermissionLevel.MOD, member)) {
                    try {
                        Role warezRole = event.getGuild().getRoleById(HifumiBot.getSelf().getConfig().roles.warezRoleId);
                        event.getGuild().addRoleToMember(member, warezRole).queue();
                    } catch (InsufficientPermissionException e) {
                        Messaging.logInfo("CommandWarez", "execute", "Failed to assign role to " + member.getAsMention() + " (insufficient permissions)");                        
                    }
                }
            }
            
            TextChannel welcomeRules = event.getGuild().getTextChannelById(HifumiBot.getSelf().getConfig().channels.rulesChannelId);
            TextChannel appeals = event.getGuild().getTextChannelById(HifumiBot.getSelf().getConfig().channels.appealsChannelId);
            eb.setTitle("PCSX2 Anti-Warez Rules");
            eb.setDescription("As per ")
                    .appendDescription(welcomeRules.getAsMention())
                    .appendDescription(", our server **does not support** piracy.\n")
                    .appendDescription("- No help or support will be given to anyone who uses pirated games, BIOS files, or other materials\n")
                    .appendDescription("- Do not discuss how to pirate games, BIOS files, or other materials\n")
                    .appendDescription("- Do not discuss why you think piracy should be allowed\n");
            eb.addField("Enforcement", "Enforcement is at the discretion of server staff, as they see fit.", false);
            eb.addField(
                    "Permissions",
                    "Your warez role makes you ineligible for support; you no longer have access to chat in our general or help channels. However, you are free to remain in the server and may chat in any off topic channels about non-support topics.",
                    false);
            eb.addField(
                    "Your Stance On Piracy",
                    "We are not here to argue. Please forward all complaints about copyright law to Sony, or your local government's copyright enforcement agency.",
                    false);
            eb.addField(
                    "\"But a friend gave it to me!\" or \"I own a copy, and just downloaded it instead!\"",
                    "Games, BIOS files and other materials must be from discs, a console or other device that you own.",
                    false);
            eb.addField(
                    "Appeal",
                    "Should you wish to appeal and have your warez role removed, use the " + appeals.getAsMention() + " channel, and **follow the instructions in the pinned messages**. Staff will want to see some visual proof that you own the items you were warez'd for - a picture of the item(s) with your Discord username written on a piece of paper next to it.",
                    false);

            mb.setEmbeds(eb.build());
            event.getHook().sendMessage(mb.build()).queue();
        } catch (Exception e) {
            event.getHook().sendMessage("Command failed; check bot output channel for error log.").queue();
            Messaging.logException("CommandWarez", "execute", e);
        }
    }

    @Override
    protected CommandData defineSlashCommand() {
        return Commands.slash("warez", "Show a prompt about anti-piracy rules, with optional warez role assignment")
                .addOption(OptionType.USER, "user", "Optional user to assign warez role to")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }
}
