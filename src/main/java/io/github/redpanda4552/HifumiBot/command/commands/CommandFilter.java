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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.CommandInterpreter;
import io.github.redpanda4552.HifumiBot.command.CommandMeta;
import io.github.redpanda4552.HifumiBot.config.ConfigManager;
import io.github.redpanda4552.HifumiBot.filter.Filter;
import io.github.redpanda4552.HifumiBot.permissions.PermissionLevel;
import io.github.redpanda4552.HifumiBot.util.EmbedUtil;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.EmbedBuilder;

public class CommandFilter extends AbstractCommand {
    private static final String NO_SUCH_FILTER = ":x: No such filter '%s' exists.";

    public CommandFilter() {
        super("filter", CATEGORY_BUILTIN, PermissionLevel.SUPER_ADMIN, false);
    }

    @Override
    public void execute(CommandMeta cm) {
        if (cm.getArgs().length == 0) {
            showHelpDialog(cm);
        } else {
            switch (cm.getArgs()[0].toLowerCase()) {
            case "new":
                if (cm.getArgs().length >= 2) {
                    if (HifumiBot.getSelf().getConfig().filters.containsKey(cm.getArgs()[1])) {
                        Messaging.sendMessage(cm.getChannel(), ":x: A filter already exists with this name.");
                        break;
                    }

                    Filter filter = new Filter();
                    filter.name = cm.getArgs()[1];
                    HifumiBot.getSelf().getConfig().filters.put(filter.name, filter);
                    ConfigManager.write(HifumiBot.getSelf().getConfig());
                    Messaging.sendMessage(cm.getChannel(),
                            ":white_check_mark: Created empty filter '" + filter.name + "'.");
                } else {
                    showHelpDialog(cm);
                }

                break;
            case "add":
                if (cm.getArgs().length >= 4) {
                    Filter filter = HifumiBot.getSelf().getConfig().filters.get(cm.getArgs()[1]);

                    if (filter == null) {
                        Messaging.sendMessage(cm.getChannel(), String.format(NO_SUCH_FILTER, cm.getArgs()[1]));
                        break;
                    }

                    try {
                        Pattern.compile(cm.getArgs()[3]);
                    } catch (PatternSyntaxException e) {
                        Messaging.sendMessage(cm.getChannel(),
                                ":x: Regular expression did not compile! \n\nException message: " + e.getMessage());
                        break;
                    }

                    filter.regexes.put(cm.getArgs()[2], cm.getArgs()[3]);
                    HifumiBot.getSelf().getConfig().filters.put(cm.getArgs()[1], filter);
                    ConfigManager.write(HifumiBot.getSelf().getConfig());
                    HifumiBot.getSelf().getChatFilter().compile();
                    Messaging.sendMessage(cm.getChannel(), ":white_check_mark: Created regex '" + cm.getArgs()[2]
                            + "' on filter '" + filter.name + "'.");
                } else {
                    showHelpDialog(cm);
                }

                break;
            case "rm":
                if (cm.getArgs().length >= 3) {
                    Filter filter = HifumiBot.getSelf().getConfig().filters.get(cm.getArgs()[1]);

                    if (filter == null) {
                        Messaging.sendMessage(cm.getChannel(), String.format(NO_SUCH_FILTER, cm.getArgs()[1]));
                        break;
                    }

                    String regexName = cm.getArgs()[2];

                    if (filter.regexes.containsKey(regexName)) {
                        filter.regexes.remove(regexName);
                    } else {
                        Messaging.sendMessage(cm.getChannel(),
                                ":x: No regex with name '" + regexName + "' found on filter '" + filter.name + "'.");
                        break;
                    }

                    HifumiBot.getSelf().getConfig().filters.put(filter.name, filter);
                    ConfigManager.write(HifumiBot.getSelf().getConfig());
                    HifumiBot.getSelf().getChatFilter().compile();
                    Messaging.sendMessage(cm.getChannel(), ":white_check_mark: Removed regex '" + cm.getArgs()[2]
                            + "' from filter '" + filter.name + "'.");
                } else {
                    showHelpDialog(cm);
                }

                break;
            case "reply":
                if (cm.getArgs().length >= 3) {
                    Filter filter = HifumiBot.getSelf().getConfig().filters.get(cm.getArgs()[1]);

                    if (filter == null) {
                        Messaging.sendMessage(cm.getChannel(), String.format(NO_SUCH_FILTER, cm.getArgs()[1]));
                        break;
                    }

                    filter.replyMessage = cm.getArgs()[2];
                    HifumiBot.getSelf().getConfig().filters.put(filter.name, filter);
                    ConfigManager.write(HifumiBot.getSelf().getConfig());
                    Messaging.sendMessage(cm.getChannel(),
                            ":white_check_mark: Set reply message on filter '" + filter.name + "'.");
                } else {
                    showHelpDialog(cm);
                }

                break;
            case "get":
                if (cm.getArgs().length >= 2) {
                    Filter filter = HifumiBot.getSelf().getConfig().filters.get(cm.getArgs()[1]);

                    if (filter == null) {
                        Messaging.sendMessage(cm.getChannel(), String.format(NO_SUCH_FILTER, cm.getArgs()[1]));
                        break;
                    }

                    EmbedBuilder eb = EmbedUtil.newFootedEmbedBuilder(cm);
                    eb.setTitle(filter.name);
                    eb.setDescription(filter.replyMessage.isBlank() ? "This filter has no reply message."
                            : "Replies with:\n```\n" + filter.replyMessage + "\n```");

                    for (String regexName : filter.regexes.keySet()) {
                        eb.addField(regexName, "`" + filter.regexes.get(regexName) + "`", false);
                    }

                    Messaging.sendMessageEmbed(cm.getChannel(), eb.build());
                } else {
                    showHelpDialog(cm);
                }

                break;
            case "del":
                if (cm.getArgs().length >= 2) {
                    Filter filter = HifumiBot.getSelf().getConfig().filters.get(cm.getArgs()[1]);

                    if (filter == null) {
                        Messaging.sendMessage(cm.getChannel(), String.format(NO_SUCH_FILTER, cm.getArgs()[1]));
                        break;
                    }

                    HifumiBot.getSelf().getConfig().filters.remove(filter.name);
                    ConfigManager.write(HifumiBot.getSelf().getConfig());
                    HifumiBot.getSelf().getChatFilter().compile();
                    Messaging.sendMessage(cm.getChannel(), ":white_check_mark: Deleted filter '" + filter.name + "'.");
                } else {
                    showHelpDialog(cm);

                }

                break;
            case "list":
                EmbedBuilder eb = EmbedUtil.newFootedEmbedBuilder(cm);
                eb.setTitle("Filter List");

                for (Filter filter : HifumiBot.getSelf().getConfig().filters.values()) {
                    eb.addField(filter.name, filter.regexes.size() + " regular expressions // Reply Message = "
                            + !filter.replyMessage.isBlank(), false);
                }

                if (eb.getFields().size() == 0) {
                    eb.setDescription("oh yeah... THERE IS NONE.");
                }

                Messaging.sendMessageEmbed(cm.getChannel(), eb.build());
                break;
            case "compile":
                HifumiBot.getSelf().getChatFilter().compile();
                Messaging.sendMessage(cm.getChannel(), ":white_check_mark: Compiled all filter regular expressions.");
                break;
            default:
                showHelpDialog(cm);
                break;
            }
        }
    }

    @Override
    public String getHelpText() {
        return "Manage chat filters";
    }

    private void showHelpDialog(CommandMeta cm) {
        EmbedBuilder eb = EmbedUtil.newFootedEmbedBuilder(cm);
        eb.setTitle("Manage Chat Filters");
        eb.addField("Create Empty Filter", CommandInterpreter.PREFIX + "filter new <filterName>", false);
        eb.addField("Add Regex", CommandInterpreter.PREFIX + "filter add <filterName> <regexName> <regex>", false);
        eb.addField("Remove Regex", CommandInterpreter.PREFIX + "filter rm <filterName> <regexName>", false);
        eb.addField("Set Reply Message", CommandInterpreter.PREFIX + "filter reply <filterName> <message>", false);
        eb.addField("Get Filter Details", CommandInterpreter.PREFIX + "filter get <filterName>", false);
        eb.addField("Delete Filter", CommandInterpreter.PREFIX + "filter del <filterName>", false);
        eb.addField("List All Filters", CommandInterpreter.PREFIX + "filter list", false);
        eb.addField("Compile All Filters", CommandInterpreter.PREFIX + "filter compile", false);
        Messaging.sendMessageEmbed(cm.getChannel(), eb.build());
    }
}
