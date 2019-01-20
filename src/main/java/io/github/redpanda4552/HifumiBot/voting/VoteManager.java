/**
 * This file is part of HifumiBot, licensed under the MIT License (MIT)
 * 
 * Copyright (c) 2018 Brian Wood
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
package io.github.redpanda4552.HifumiBot.voting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import io.github.redpanda4552.HifumiBot.HifumiBot;

public class VoteManager {

    private final String OPT_SPLIT = "::";
    
    private Connection connection;
    
    public VoteManager(HifumiBot hifumiBot) {
        this.connection = hifumiBot.getDatabaseConnection();
        
        try {
            PreparedStatement ps;
            ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS votes (name TEXT PRIMARY KEY, question TEXT, userId TEXT, options TEXT);");
            ps.executeUpdate();
            ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS voteres (name TEXT, userId TEXT, option TEXT, PRIMARY KEY (name, userId), FOREIGN KEY (name) REFERENCES votes (name));");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void createVote(String name, String question, String userId, String... options) {
        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO votes (name, question, userId, options) VALUES (?, ?, ?, ?);");
            ps.setString(1, name);
            ps.setString(2, question);
            ps.setString(3, userId);
            StringBuilder optionsBuilder = new StringBuilder();
            
            for (String opt : options)
                optionsBuilder.append(opt).append(OPT_SPLIT);
            
            ps.setString(4, optionsBuilder.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void deleteVote(String name) {
        try {
            PreparedStatement ps;
            ps = connection.prepareStatement("DELETE FROM voteres WHERE name = ?;");
            ps.setString(1, name);
            ps = connection.prepareStatement("DELETE FROM votes WHERE name = ?;");
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public Vote getVote(String name) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM votes WHERE name = ?;");
            ps.setString(1, name);
            ResultSet res = ps.executeQuery();
            
            if (res.next()) {
                String[] options = res.getString("options").split(OPT_SPLIT);
                return new Vote(res.getString("name"), res.getString("userId"), res.getString("question"), options);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public ArrayList<Vote> getAllVotes() {
        try {
            ArrayList<Vote> ret = new ArrayList<Vote>();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM votes;");
            ResultSet res = ps.executeQuery();
            
            while (res.next()) {
                String[] options = res.getString("options").split(OPT_SPLIT);
                ret.add(new Vote(res.getString("name"), res.getString("userId"), res.getString("question"), options));
            }
            
            return ret;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public VoteResult getVoteResult(String name) {
        try {
            ArrayList<String> results = new ArrayList<String>();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM voteres WHERE name = ?;");
            ps.setString(1, name);
            ResultSet res = ps.executeQuery();
            
            while (res.next())
                results.add(res.getString("option"));
            
            return new VoteResult(name, results);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public void castVote(String name, String userId, String option) {
        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO voteres (name, userId, option) VALUES (?, ?, ?);");
            ps.setString(1, name);
            ps.setString(2, userId);
            ps.setString(3, option);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
