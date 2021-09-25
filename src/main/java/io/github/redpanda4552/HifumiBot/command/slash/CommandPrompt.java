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
import io.github.redpanda4552.HifumiBot.config.ConfigManager;
import io.github.redpanda4552.HifumiBot.permissions.PermissionLevel;
import io.github.redpanda4552.HifumiBot.util.Strings;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class CommandPrompt extends AbstractSlashCommand {

    public CommandPrompt() {
        super(PermissionLevel.SUPER_ADMIN);
    }

    @Override
    protected void onExecute(SlashCommandEvent event) {
        event.deferReply(true).queue();
        
        OptionMapping os = event.getOption("os");
        OptionMapping instructions = event.getOption("instructions");
        
        switch (event.getSubcommandName()) {
        case "get":
            switch (os.getAsString()) {
            case "windows":
                event.getHook().sendMessage("Currently displaying (markdown suppressed):\n```\n" + HifumiBot.getSelf().getConfig().dev.windows + "\n```").queue();
                return;
            case "ubuntu":
                event.getHook().sendMessage("Currently displaying (markdown suppressed):\n```\n" + HifumiBot.getSelf().getConfig().dev.ubuntu + "\n```").queue();
                return;
            case "linux":
                event.getHook().sendMessage("Currently displaying (markdown suppressed):\n```\n" + HifumiBot.getSelf().getConfig().dev.linux + "\n```").queue();
                return;
            }
        case "set":
            switch (os.getAsString()) {
            case "windows":
                HifumiBot.getSelf().getConfig().dev.windows = Strings.unescapeNewlines(instructions.getAsString());
                ConfigManager.write(HifumiBot.getSelf().getConfig());
                event.getHook().sendMessage("Updated to (markdown enabled):\n" + HifumiBot.getSelf().getConfig().dev.windows).queue();
                return;
            case "ubuntu":
                HifumiBot.getSelf().getConfig().dev.ubuntu = Strings.unescapeNewlines(instructions.getAsString());
                ConfigManager.write(HifumiBot.getSelf().getConfig());
                event.getHook().sendMessage("Updated to (markdown enabled):\n" + HifumiBot.getSelf().getConfig().dev.ubuntu).queue();
                return;
            case "linux":
                HifumiBot.getSelf().getConfig().dev.linux = Strings.unescapeNewlines(instructions.getAsString());
                ConfigManager.write(HifumiBot.getSelf().getConfig());
                event.getHook().sendMessage("Updated to (markdown enabled):\n" + HifumiBot.getSelf().getConfig().dev.linux).queue();
                return;
            }
        }
    }

    @Override
    protected CommandData defineSlashCommand() {
        OptionData os = new OptionData(OptionType.STRING, "os", "Operating System", true)
                .addChoice("Windows", "windows")
                .addChoice("Ubuntu", "ubuntu")
                .addChoice("Linux", "linux");
        
        SubcommandData get = new SubcommandData("get", "Get the current instructions")
                .addOptions(os);
        
        SubcommandData set = new SubcommandData("set", "Set the instructions")
                .addOptions(os)
                .addOption(OptionType.STRING, "instructions", "New instructions to set", true);
        
        return new CommandData("prompt", "Get or set various information prompts")
                .addSubcommands(get, set);
    }

}
