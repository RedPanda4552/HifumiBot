// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.permissions;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import java.util.ArrayList;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class PermissionManager {
  private final String superuserId;

  public PermissionManager(String superuserId) {
    this.superuserId = superuserId;

    if (HifumiBot.getSelf().getConfig().permissions.blockedRoleIds == null) {
      HifumiBot.getSelf().getConfig().permissions.blockedRoleIds = new ArrayList<>();
    }

    if (HifumiBot.getSelf().getConfig().permissions.modRoleIds == null) {
      HifumiBot.getSelf().getConfig().permissions.modRoleIds = new ArrayList<>();
    }

    if (HifumiBot.getSelf().getConfig().permissions.adminRoleIds == null) {
      HifumiBot.getSelf().getConfig().permissions.adminRoleIds = new ArrayList<>();
    }

    if (HifumiBot.getSelf().getConfig().permissions.superAdminRoleIds == null) {
      HifumiBot.getSelf().getConfig().permissions.superAdminRoleIds = new ArrayList<>();
    }
  }

  public boolean hasPermission(PermissionLevel permissionLevel, Member member) {
    if (member == null) {
      return false;
    }

    switch (permissionLevel) {
      case GUEST:
        for (Role role : member.getRoles()) {
          if (HifumiBot.getSelf().getConfig().permissions.blockedRoleIds.contains(role.getId())) {
            return false;
          }
        }

        return true;
      case MOD:
        for (Role role : member.getRoles()) {
          if (HifumiBot.getSelf().getConfig().permissions.modRoleIds.contains(role.getId())) {
            return true;
          }
        }
      case ADMIN:
        for (Role role : member.getRoles()) {
          if (HifumiBot.getSelf().getConfig().permissions.adminRoleIds.contains(role.getId())) {
            return true;
          }
        }
      case SUPER_ADMIN:
        for (Role role : member.getRoles()) {
          if (HifumiBot.getSelf()
              .getConfig()
              .permissions
              .superAdminRoleIds
              .contains(role.getId())) {
            return true;
          }
        }
      case SUPERUSER:
        if (superuserId != null && superuserId.equals(member.getUser().getId())) {
          return true;
        }
      default:
        return false;
    }
  }
}
