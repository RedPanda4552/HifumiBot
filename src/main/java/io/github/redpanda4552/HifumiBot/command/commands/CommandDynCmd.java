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

import java.util.ArrayList;
import java.util.HashMap;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.CommandMeta;
import io.github.redpanda4552.HifumiBot.command.DynamicCommand;
import io.github.redpanda4552.HifumiBot.permissions.PermissionLevel;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class CommandDynCmd extends AbstractCommand
{
    private final MessageEmbed usage;

    public CommandDynCmd()
    {
        super("dyncmd", CATEGORY_BUILTIN, PermissionLevel.ADMIN, false);
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("DynCmd Usage");
        eb.addField("View", "`dyncmd get <name>`", false);
        eb.addField("Create/Modify", "`dyncmd set <name> [options]`", false);
        eb.addField("Delete", "`dyncmd del <name>`", false);

        StringBuilder sb = new StringBuilder();
        sb.append("-c, --category <category>\n");
        sb.append("-h, --helptext <help text>\n");
        sb.append("-t, --title <title>\n");
        sb.append("-b, --body <body>\n");
        sb.append("-i, --imageurl <image URL>\n");
        sb.append("-r, --restrict <true|false>`");

        eb.addField("Options", sb.toString(), false);
        usage = eb.build();
    }

    @Override
    public void execute(CommandMeta cm)
    {
        if (cm.getArgs().length < 2)
        {
            Messaging.sendMessageEmbed(cm.getChannel(), usage);
            return;
        }

        String subCommand = cm.getArgs()[0];
        String name = cm.getArgs()[1].toLowerCase();
        ArrayList<String> results = new ArrayList<String>();
        DynamicCommand dyncmd = null;

        switch (subCommand.toLowerCase())
        {
        case "get":
            if (!HifumiBot.getSelf().getCommandIndex().isDynamicCommand(name))
            {
                Messaging.sendMessage(cm.getChannel(), "Specified command is not a dynamic command");
                return;
            }

            dyncmd = HifumiBot.getSelf().getCommandIndex().getDynamicCommand(name);

            results.add("Restricted Channel: " + dyncmd.isRestricted());

            if (dyncmd.getCategory() != null && !dyncmd.getCategory().isBlank())
            {
                results.add("Category: " + dyncmd.getCategory());
            }

            if (dyncmd.getHelpText() != null && !dyncmd.getHelpText().isBlank())
            {
                results.add("Help Text: " + dyncmd.getHelpText());
            }

            if (dyncmd.getTitle() != null && !dyncmd.getTitle().isBlank())
            {
                results.add("Title: " + dyncmd.getTitle());
            }

            if (dyncmd.getBody() != null && !dyncmd.getBody().isBlank())
            {
                results.add("Body:\n```" + dyncmd.getBody() + "```");
            }

            if (dyncmd.getImageURL() != null && !dyncmd.getImageURL().isBlank())
            {
                results.add("Image URL: `" + dyncmd.getImageURL() + "`");
            }

            sendResults(cm.getChannel(), dyncmd.getName(), results);
            break;
        case "set":
            dyncmd = HifumiBot.getSelf().getCommandIndex().getDynamicCommand(name);

            if (HifumiBot.getSelf().getCommandIndex().isCommand(name)
                    && !HifumiBot.getSelf().getCommandIndex().isDynamicCommand(name))
            {
                Messaging.sendMessage(cm.getChannel(),
                        "You cannot create a dynamic command with the same name as a builtin command");
                return;
            }
            else if (dyncmd == null)
            {
                dyncmd = new DynamicCommand(name, CATEGORY_NONE, PermissionLevel.GUEST, false, "", null, null, null);
            }
            HashMap<String, String> switches = cm.getSwitches();

            for (String switchName : switches.keySet())
            {
                String switchValue = switches.get(switchName);

                switch (switchName)
                {
                case "category":
                case "c":
                    dyncmd.setCategory(switchValue);
                    results.add(":white_check_mark: New Category: " + switchValue);
                    break;
                case "helptext":
                case "h":
                    dyncmd.setHelpText(switchValue);
                    results.add(":white_check_mark: New Help Text: " + switchValue);
                    break;
                case "title":
                case "t":
                    dyncmd.setTitle(switchValue);
                    results.add(":white_check_mark: New Title: " + switchValue);
                    break;
                case "body":
                case "b":
                    dyncmd.setBody(switchValue);
                    results.add(":white_check_mark: New Body: " + switchValue);
                    break;
                case "imageurl":
                case "i":
                    dyncmd.setImageURL(switchValue);
                    results.add(":white_check_mark: New Image URL: " + switchValue);
                    break;
                case "restrict":
                case "r":
                    boolean restricted = Boolean.valueOf(switchValue);
                    dyncmd.setRestricted(restricted);
                    results.add(":white_check_mark: Restricted channels: " + restricted);
                    break;
                default:
                    results.add(":warning: Unrecognized switch " + switchName + " with value " + switchValue);
                    break;
                }
            }

            HifumiBot.getSelf().getCommandIndex().addCommand(dyncmd);
            sendResults(cm.getChannel(), dyncmd.getName(), results);
            break;
        case "del":
            if (HifumiBot.getSelf().getCommandIndex().isDynamicCommand(name))
            {
                HifumiBot.getSelf().getCommandIndex().deleteCommand(name);
                results.add(":white_check_mark: Deleted command '" + name + "'");
            }
            else
            {
                results.add(":warning: No command found with name '" + name + "'");
            }

            break;
        default:
            Messaging.sendMessageEmbed(cm.getChannel(), usage);
            break;
        }
    }

    @Override
    public String getHelpText()
    {
        return "Add a dynamic command to " + HifumiBot.getSelf().getJDA().getSelfUser().getName();
    }

    private void sendResults(MessageChannel channel, String name, ArrayList<String> results)
    {
        MessageBuilder mb = new MessageBuilder();

        for (String str : results)
        {
            mb.append(str).append("\n");
        }

        Messaging.sendMessage(channel, mb.build());
    }
}
