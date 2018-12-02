/**
 * This file is part of HifumiBot, licensed under the MIT License (MIT)
 * 
 * Copyright (c) 2017 Brian Wood
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
package io.github.redpanda4552.HifumiBot;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DynamicCommandLoader {

    private final String DB_LOCATION = "hifumi-commands.db";
    
    private Connection connection;
    
    public DynamicCommandLoader() {
        try {
            File file = new File(DB_LOCATION);
            file.createNewFile();
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_LOCATION);
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS commands (name TEXT PRIMARY KEY, helpText TEXT, admin BOOLEAN, title TEXT, body TEXT, imageUrl TEXT)");
            ps.executeUpdate();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public ResultSet getDynamicCommands() {
        try {
            return connection.prepareStatement("SELECT * FROM commands;").executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public boolean insertCommand(String name, String helpText) {
        // Check that the attempted command doesn't already exist in the interpreter. Prevents overrides of things like reload.
        if (HifumiBot.getSelf().getCommandInterpreter().getCommandMap().containsKey(name))
            return false;
        
        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO commands (name, helpText, admin, title, body, imageUrl) VALUES (?, ?, ?, ?, ?, ?);");
            ps.setString(1, name);
            ps.setString(2, helpText);
            ps.setBoolean(3, false);
            ps.setString(4, null);
            ps.setString(5, null);
            ps.setString(6, null);
            ps.executeUpdate();
            HifumiBot.getSelf().getCommandInterpreter().refreshCommandMap();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    public boolean updateCommand(String name, String attribute, String value) {
        try {
            PreparedStatement ps = connection.prepareStatement("UPDATE commands SET " + attribute + " = ? WHERE name = ?;");
            
            switch (attribute) {
            case "admin":
                ps.setBoolean(1, Boolean.parseBoolean(value));
                break;
            default:
                ps.setString(1, value);
                break;
            }
            
            ps.setString(2, name);
            ps.executeUpdate();
            HifumiBot.getSelf().getCommandInterpreter().refreshCommandMap();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    public boolean removeCommand(String name) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM commands WHERE name = ?;");
            ps.setString(1, name);
            ps.executeUpdate();
            HifumiBot.getSelf().getCommandInterpreter().refreshCommandMap();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
}
