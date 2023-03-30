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

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class CommandPanic extends AbstractSlashCommand {

    @Override
    protected void onExecute(SlashCommandInteractionEvent event) {
        OptionMapping enableOpt = event.getOption("enable");
        
        if (enableOpt == null) {
            event.reply("Missing required argument 'enable'").setEphemeral(true).queue();
            return;
        }
        
        event.deferReply().queue();
        
        if (enableOpt.getAsBoolean()) {
            enable(event);
        } else {
            disable(event);
        }
    }

    @Override
    protected CommandData defineSlashCommand() {
        return Commands.slash("panic", "Panic mode to restrict messaging and server joins")
                .addOption(OptionType.BOOLEAN, "enable", "Enable or disable panic mode", true)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    private void enable(SlashCommandInteractionEvent event) {
        if (HifumiBot.getSelf().getMemberEventListener().getLockdown()) {
            event.getHook().editOriginal("Panic mode is already enabled").queue();
            return;
        }
        
        event.getGuild().getTextChannels().forEach((channel) -> {
            if (channel.getGuild().getPublicRole().hasAccess(channel)) {
                channel.getManager().setSlowmode(1).queue();
            }
        });
        
        HifumiBot.getSelf().getMemberEventListener().setLockdown(true);
        event.getHook().editOriginal("Panic mode activated.\n- 1 second slow mode is applied to all channels visible to role-less viewers\n- New users are being instantly kicked but will receive a DM explaining why").queue();
    }
    
    private void disable(SlashCommandInteractionEvent event) {
        event.getGuild().getTextChannels().forEach((channel) -> {
            if (channel.getGuild().getPublicRole().hasAccess(channel)) {
                channel.getManager().setSlowmode(0).queue();
            }
        });
        
        HifumiBot.getSelf().getMemberEventListener().setLockdown(false);
        event.getHook().editOriginal("Panic mode deactivated. All previous changes have been reverted.").queue();
    }
}
