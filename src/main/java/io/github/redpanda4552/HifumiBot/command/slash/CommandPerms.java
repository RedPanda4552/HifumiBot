// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.command.slash;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import io.github.redpanda4552.HifumiBot.config.ConfigManager;
import io.github.redpanda4552.HifumiBot.permissions.PermissionLevel;
import java.util.ArrayList;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class CommandPerms extends AbstractSlashCommand {

  @Override
  protected void onExecute(SlashCommandInteractionEvent event) {
    event.deferReply().queue();
    OptionMapping optPermissionLevel = event.getOption("permission-level");
    PermissionLevel permissionLevel = null;
    ArrayList<String> roleIds = null;

    if (optPermissionLevel != null) {
      permissionLevel = PermissionLevel.valueOf(optPermissionLevel.getAsString());

      switch (permissionLevel) {
        case BLOCKED -> roleIds = HifumiBot.getSelf().getConfig().permissions.blockedRoleIds;
        case MOD -> roleIds = HifumiBot.getSelf().getConfig().permissions.modRoleIds;
        case ADMIN -> roleIds = HifumiBot.getSelf().getConfig().permissions.adminRoleIds;
        case SUPER_ADMIN -> roleIds = HifumiBot.getSelf().getConfig().permissions.superAdminRoleIds;
        default -> {
          event.getHook().sendMessage("Sanity check.").queue();
          return;
        }
      }

      if (roleIds == null) {
        roleIds = new ArrayList<String>();
      }
    }

    Role role = null;
    OptionMapping optRole = event.getOption("role");

    if (optRole != null) {
      role = optRole.getAsRole();
    }

    switch (event.getSubcommandName()) {
      case "add":
        if (!roleIds.contains(role.getId())) {
          roleIds.add(role.getId());
          ConfigManager.write(HifumiBot.getSelf().getConfig());
          event
              .getHook()
              .sendMessage(
                  ":white_check_mark: Role "
                      + role.getName()
                      + " added to permission level "
                      + permissionLevel.toString()
                      + ".")
              .queue();
        } else {
          event
              .getHook()
              .sendMessage(
                  ":x: Role "
                      + role.getName()
                      + " is already a member of permission level "
                      + permissionLevel.toString()
                      + ".")
              .queue();
        }
        break;
      case "del":
        if (roleIds.contains(role.getId())) {
          roleIds.remove(role.getId());
          ConfigManager.write(HifumiBot.getSelf().getConfig());
          event
              .getHook()
              .sendMessage(
                  ":white_check_mark: Role "
                      + role.getName()
                      + " removed from permission level "
                      + permissionLevel.toString()
                      + ".")
              .queue();
        } else {
          event
              .getHook()
              .sendMessage(
                  ":x: Role "
                      + role.getName()
                      + " is not a member of permission level "
                      + permissionLevel.toString()
                      + ".")
              .queue();
        }
        break;
      case "list":
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(
            HifumiBot.getSelf().getJda().getSelfUser().getName()
                + " - Roles Assigned to Permission Levels");
        StringBuilder sb = new StringBuilder();

        for (String blockedRoleId : HifumiBot.getSelf().getConfig().permissions.blockedRoleIds) {
          sb.append("`" + event.getGuild().getRoleById(blockedRoleId).getName() + "`\t");
        }

        eb.addField("BLOCKED", sb.toString(), false);
        sb = new StringBuilder();

        for (String modRoleId : HifumiBot.getSelf().getConfig().permissions.modRoleIds) {
          sb.append("`" + event.getGuild().getRoleById(modRoleId).getName() + "`\t");
        }

        eb.addField("MOD", sb.toString(), false);
        sb = new StringBuilder();

        for (String adminRoleId : HifumiBot.getSelf().getConfig().permissions.adminRoleIds) {
          sb.append("`" + event.getGuild().getRoleById(adminRoleId).getName() + "`\t");
        }

        eb.addField("ADMIN", sb.toString(), false);
        sb = new StringBuilder();

        for (String superAdminRoleId :
            HifumiBot.getSelf().getConfig().permissions.superAdminRoleIds) {
          sb.append("`" + event.getGuild().getRoleById(superAdminRoleId).getName() + "`\t");
        }

        eb.addField("SUPER_ADMIN", sb.toString(), false);
        event.getHook().sendMessageEmbeds(eb.build()).queue();
        break;
    }
  }

  @Override
  protected CommandData defineSlashCommand() {
    OptionData permissionLevel =
        new OptionData(OptionType.STRING, "permission-level", "Permission level to modify", true)
            .addChoice("Blocked", "BLOCKED")
            .addChoice("Mod", "MOD")
            .addChoice("Admin", "ADMIN")
            .addChoice("Super Admin", "SUPER_ADMIN");
    OptionData role = new OptionData(OptionType.ROLE, "role", "Target role", true);

    SubcommandData add =
        new SubcommandData("add", "Add a role to a permission level")
            .addOptions(permissionLevel, role);

    SubcommandData remove =
        new SubcommandData("remove", "Remove a role from a permission level")
            .addOptions(permissionLevel, role);

    SubcommandData list =
        new SubcommandData("list", "List current role assignments to permission levels");

    return Commands.slash("perms", "Manage permission levels")
        .addSubcommands(add, remove, list)
        .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
  }
}
