// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.util;

import java.util.List;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class RoleUtils {

  /**
   * Check if a member has a Role contained in the specified list
   *
   * @param member - The Member to check
   * @param roles - The List of Roles to compare against
   * @return True if the Member has a Role contained in the List, false otherwise.
   */
  public static boolean memberHasRole(Member member, List<String> roles) {
    for (Role role : member.getRoles()) {
      if (roles.contains(role.getName())) {
        return true;
      }
    }

    return false;
  }
}
