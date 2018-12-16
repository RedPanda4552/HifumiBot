package io.github.redpanda4552.HifumiBot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

public class PermissionManager {

    private String superuserId;
    private ArrayList<String> adminRoles = new ArrayList<String>();
    
    public PermissionManager(String superuserId) {
        this.superuserId = superuserId;
        populateAdminRoles();
    }
    
    private boolean populateAdminRoles() {
        File adminsFile = new File("./admins.txt");
        
        try {
            if (!adminsFile.exists() && !adminsFile.createNewFile()) {
                System.out.println("Failed to create admins.txt! Only superuser will have command permissions!");
                return false;
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(adminsFile));
            String line = "";
            
            while ((line = reader.readLine()) != null)
                if (!line.startsWith("//"))
                    adminRoles.add(line);
            
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return true;
    }
    
    public boolean hasPermission(Member member, User user) {
        return isSuperuser(user) || isAdmin(member);
    }
    
    private boolean isSuperuser(User user) {
        if (user == null)
            return false;
        
        return user.getId().equals(superuserId);
    }
    
    private boolean isAdmin(Member member) {
        // Only commands requiring admin permissions will ever need to test with this method.
        // Thus, if we are in a private channel (where member is always null), where we don't
        // want admin commands to ever be an option, always return false.
        if (member == null)
            return false;
        
        for (Role role : member.getRoles()) {
            if (adminRoles.contains(role.getName())) {
                return true;
            }
        }
        
        return false;
    }
}
