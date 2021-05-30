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

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.CommandInterpreter;
import io.github.redpanda4552.HifumiBot.command.CommandMeta;
import io.github.redpanda4552.HifumiBot.util.EmbedUtil;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;

public class CommandAdmin extends AbstractCommand
{

    public CommandAdmin()
    {
        super("admin", CATEGORY_BUILTIN, true, false);
    }

    @Override
    protected void onExecute(CommandMeta cm)
    {
        if (HifumiBot.getSelf().getPermissionManager().isSuperuser(cm.getUser()))
        {
            if (cm.getArgs().length == 0)
            {
                showHelpDialog(cm);
            }
            else
            {
                switch (cm.getArgs()[0].toLowerCase())
                {
                case "add":
                    if (cm.getArgs().length == 2)
                    {
                        String newRole = cm.getArgs()[1];
                        Guild guild = HifumiBot.getSelf().getJDA().getGuildById(cm.getGuild().getId());

                        if (guild.getRolesByName(newRole, false).size() == 1)
                        {
                            ArrayList<String> adminRoles = HifumiBot.getSelf().getConfig().adminRoles;

                            if (!adminRoles.contains(newRole))
                            {
                                adminRoles.add(newRole);
                                Messaging.sendMessage(cm.getChannel(),
                                        ":white_check_mark: Role `" + newRole + "` added to admin role list.");
                            }
                            else
                            {
                                Messaging.sendMessage(cm.getChannel(),
                                        ":x: Role `" + newRole + "` is already an admin role.");
                            }
                        }
                        else
                        {
                            Messaging.sendMessage(cm.getChannel(), ":x: Role `" + newRole + "` does not exist.");
                        }

                        break;
                    }
                case "del":
                    if (cm.getArgs().length == 2)
                    {
                        String deleteRole = cm.getArgs()[1];
                        ArrayList<String> adminRoles = HifumiBot.getSelf().getConfig().adminRoles;

                        if (adminRoles.contains(deleteRole))
                        {
                            adminRoles.remove(deleteRole);
                            Messaging.sendMessage(cm.getChannel(),
                                    ":white_check_mark: Role `" + deleteRole + "` removed from admin role list.");
                        }
                        else
                        {
                            Messaging.sendMessage(cm.getChannel(),
                                    ":x: Role `" + deleteRole + "` is not an admin role.");
                        }

                        break;
                    }
                case "list":
                    EmbedBuilder eb = EmbedUtil.newFootedEmbedBuilder(cm);
                    eb.setTitle(HifumiBot.getSelf().getJDA().getSelfUser().getName() + " - Admin Role List");

                    for (String role : HifumiBot.getSelf().getConfig().adminRoles)
                    {
                        eb.addField(role, " ", true);
                    }

                    if (HifumiBot.getSelf().getConfig().adminRoles.isEmpty())
                    {
                        eb.setDescription("Nothing here! ¯\\_(ツ)_/¯");
                    }

                    Messaging.sendMessage(cm.getChannel(), eb.build());
                    break;
                default:
                    showHelpDialog(cm);
                    break;
                }
            }
        }
    }

    private void showHelpDialog(CommandMeta cm)
    {
        EmbedBuilder eb = EmbedUtil.newFootedEmbedBuilder(cm);
        eb.setTitle("Add or remove admin roles");
        eb.addField("Add Role", CommandInterpreter.PREFIX + "admin add <role>", false);
        eb.addField("Remove Role", CommandInterpreter.PREFIX + "admin del <role>", false);
        eb.addField("List Roles", CommandInterpreter.PREFIX + "admin list", false);
        Messaging.sendMessage(cm.getChannel(), eb.build());
    }

    @Override
    public String getHelpText()
    {
        return "Assign or revoke admin permissions for a role";
    }

}
