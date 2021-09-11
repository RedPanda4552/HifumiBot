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

import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import io.github.redpanda4552.HifumiBot.permissions.PermissionLevel;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class CommandBan extends AbstractSlashCommand {

    public CommandBan() {
        super(PermissionLevel.SUPER_ADMIN);
    }

    @Override
    protected void onExecute(SlashCommandEvent event) {
        event.deferReply(true).queue();
        
        if (!event.getMember().hasPermission(Permission.BAN_MEMBERS))
        {
            event.reply("You do not have the required permissions to ban users from this server.").setEphemeral(true).queue();
            return;
        }

        Member selfMember = event.getGuild().getSelfMember();
        
        if (!selfMember.hasPermission(Permission.BAN_MEMBERS))
        {
            event.reply("I don't have the required permissions to ban users from this server.").setEphemeral(true).queue();
            return;
        }
        
        OptionMapping user = event.getOption("user");
        Member member = user.getAsMember();
        User userObj = user.getAsUser();
        
        if (member != null && !selfMember.canInteract(member))
        {
            event.reply("This user is too powerful for me to ban.").setEphemeral(true).queue();
            return;
        }
        
        int days = 0;
        OptionMapping option = event.getOption("days");
        
        if (option != null) {
            days = (int) Math.max(0, Math.min(7, option.getAsLong()));
        }

        // Ban the user and send a success response
        event.getGuild().ban(userObj, days)
                .flatMap(v -> event.getHook().sendMessage("Banned user " + userObj.getAsTag()))
                .queue();
    }

    @Override
    protected CommandData defineSlashCommand() {
        return new CommandData("ban", "Ban a user")
                .addOption(OptionType.USER, "user", "User to ban", true)
                .addOption(OptionType.INTEGER, "days", "Number of days of chat history to delete. Min 0, max 7, default 0.");
    }

}
