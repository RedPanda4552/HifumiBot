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
import io.github.redpanda4552.HifumiBot.config.ConfigManager;
import io.github.redpanda4552.HifumiBot.permissions.PermissionLevel;
import io.github.redpanda4552.HifumiBot.util.EmbedUtil;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;

public class CommandPerms extends AbstractCommand {
    public CommandPerms() {
        super("perms", CATEGORY_BUILTIN, PermissionLevel.SUPER_ADMIN, false);
    }

    @Override
    public void execute(CommandMeta cm) {
        if (cm.getArgs().length == 0) {
            showHelpDialog(cm);
        } else {
            String permissionLevelName = null;
            PermissionLevel permissionLevel = null;
            ArrayList<String> roleIds = null;
            String roleId = null;
            Guild guild = null;
            Role role = null;

            switch (cm.getArgs()[0].toLowerCase()) {
            case "add":
                if (cm.getArgs().length == 3) {
                    permissionLevelName = cm.getArgs()[1].toUpperCase();

                    try {
                        permissionLevel = PermissionLevel.valueOf(permissionLevelName);
                    } catch (IllegalArgumentException e) {
                        Messaging.sendMessage(cm.getChannel(), "Invalid permission level specified.");
                        sendPermissionLevelList(cm.getChannel());
                        return;
                    }

                    roleId = cm.getArgs()[2];
                    guild = HifumiBot.getSelf().getJDA().getGuildById(cm.getGuild().getId());

                    try {
                        role = guild.getRoleById(roleId);
                    } catch (NumberFormatException e) {
                        Messaging.sendMessage(cm.getChannel(),
                                "Failed to parse role id; is it valid? Hint: Enable Developer Mode, then right click a role to copy id.");
                        return;
                    }

                    if (role != null) {
                        switch (permissionLevel) {
                        case MOD:
                            roleIds = HifumiBot.getSelf().getConfig().permissions.modRoleIds;
                            break;
                        case ADMIN:
                            roleIds = HifumiBot.getSelf().getConfig().permissions.adminRoleIds;
                            break;
                        case SUPER_ADMIN:
                            roleIds = HifumiBot.getSelf().getConfig().permissions.superAdminRoleIds;
                            break;
                        default:
                            Messaging.sendMessage(cm.getChannel(), "Unrecognized permission level");
                            return;
                        }

                        if (!roleIds.contains(roleId)) {
                            roleIds.add(roleId);
                            Messaging.sendMessage(cm.getChannel(), ":white_check_mark: Role `" + role.getName()
                                    + "` added to permission level " + permissionLevelName + ".");
                            ConfigManager.write(HifumiBot.getSelf().getConfig());
                        } else {
                            Messaging.sendMessage(cm.getChannel(),
                                    ":x: Role `" + role.getName() + "` is already in this permission level.");
                        }
                    } else {
                        Messaging.sendMessage(cm.getChannel(),
                                ":x: Role id `" + roleId + "` does not match any roles in the server.");
                    }

                    break;
                } else {
                    showHelpDialog(cm);
                    return;
                }
            case "del":
                if (cm.getArgs().length == 3) {
                    permissionLevelName = cm.getArgs()[1].toUpperCase();

                    try {
                        permissionLevel = PermissionLevel.valueOf(permissionLevelName);
                    } catch (IllegalArgumentException e) {
                        Messaging.sendMessage(cm.getChannel(), "Invalid permission level specified.");
                        sendPermissionLevelList(cm.getChannel());
                        return;
                    }

                    roleId = cm.getArgs()[2];
                    guild = HifumiBot.getSelf().getJDA().getGuildById(cm.getGuild().getId());

                    try {
                        role = guild.getRoleById(roleId);
                    } catch (NumberFormatException e) {
                        Messaging.sendMessage(cm.getChannel(),
                                "Failed to parse role id; is it valid? Hint: Enable Developer Mode, then right click a role to copy id.");
                        return;
                    }

                    if (role != null) {
                        switch (permissionLevel) {
                        case MOD:
                            roleIds = HifumiBot.getSelf().getConfig().permissions.modRoleIds;
                            break;
                        case ADMIN:
                            roleIds = HifumiBot.getSelf().getConfig().permissions.adminRoleIds;
                            break;
                        case SUPER_ADMIN:
                            roleIds = HifumiBot.getSelf().getConfig().permissions.superAdminRoleIds;
                            break;
                        default:
                            Messaging.sendMessage(cm.getChannel(), "Unrecognized permission level");
                            return;
                        }

                        if (roleIds.contains(roleId)) {
                            roleIds.remove(roleId);
                            Messaging.sendMessage(cm.getChannel(), ":white_check_mark: Role `" + role.getName()
                                    + "` removed from permission level " + permissionLevelName + ".");
                            ConfigManager.write(HifumiBot.getSelf().getConfig());
                        } else {
                            Messaging.sendMessage(cm.getChannel(),
                                    ":x: Role `" + role.getName() + "` is not in this permission level.");
                        }
                    } else {
                        Messaging.sendMessage(cm.getChannel(),
                                ":x: Role id `" + roleId + "` does not match any roles in the server.");
                    }

                    break;
                } else {
                    showHelpDialog(cm);
                    return;
                }
            case "list":
                EmbedBuilder eb = EmbedUtil.newFootedEmbedBuilder(cm);
                eb.setTitle(HifumiBot.getSelf().getJDA().getSelfUser().getName()
                        + " - Roles Assigned to Permission Levels");
                StringBuilder sb = new StringBuilder();

                for (String modRoleId : HifumiBot.getSelf().getConfig().permissions.modRoleIds) {
                    sb.append("`" + cm.getGuild().getRoleById(modRoleId).getName() + "`\t");
                }

                eb.addField("MOD", sb.toString(), false);

                sb = new StringBuilder();

                for (String adminRoleId : HifumiBot.getSelf().getConfig().permissions.adminRoleIds) {
                    sb.append("`" + cm.getGuild().getRoleById(adminRoleId).getName() + "`\t");
                }

                eb.addField("ADMIN", sb.toString(), false);

                sb = new StringBuilder();

                for (String superAdminRoleId : HifumiBot.getSelf().getConfig().permissions.superAdminRoleIds) {
                    sb.append("`" + cm.getGuild().getRoleById(superAdminRoleId).getName() + "`\t");
                }

                eb.addField("SUPER_ADMIN", sb.toString(), false);

                Messaging.sendMessageEmbed(cm.getChannel(), eb.build());
                break;
            case "levels":
                sendPermissionLevelList(cm.getChannel());
                break;
            default:
                showHelpDialog(cm);
                break;
            }
        }
    }

    private void showHelpDialog(CommandMeta cm) {
        EmbedBuilder eb = EmbedUtil.newFootedEmbedBuilder(cm);
        eb.setTitle("Add or remove admin roles");
        eb.addField("Add Role", CommandInterpreter.PREFIX + "perms add <permissionLevel> <roleId>", false);
        eb.addField("Remove Role", CommandInterpreter.PREFIX + "perms del <permissionLevel> <roleId>", false);
        eb.addField("List Roles", CommandInterpreter.PREFIX + "perms list", false);
        eb.addField("Show Permission Levels", CommandInterpreter.PREFIX + "perms levels", false);
        Messaging.sendMessageEmbed(cm.getChannel(), eb.build());
    }

    private void sendPermissionLevelList(MessageChannel channel) {
        MessageBuilder mb = new MessageBuilder("Permission Levels: ");
        mb.append("`MOD`, `ADMIN`, `SUPER_ADMIN`");
        Messaging.sendMessage(channel, mb.build());
    }

    @Override
    public String getHelpText() {
        return "Assign or revoke permissions for a role";
    }

}
