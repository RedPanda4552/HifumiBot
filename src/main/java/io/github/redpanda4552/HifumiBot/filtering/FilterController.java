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
package io.github.redpanda4552.HifumiBot.filtering;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.SQLite;
import io.github.redpanda4552.HifumiBot.util.EmbedUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

/**
 * Really could have just pumped out regexes but felt like making this 
 * unnecessarily complex.
 */
public class FilterController {

    private final String FILTER_TABLE = "filters", ACTIVATION_WORDS_TABLE = "activation_words";
    
    private ExecutorService executor;
    private HashMap<String, Filter> filters = new HashMap<String, Filter>();
    
    public FilterController() {
        executor = Executors.newFixedThreadPool(2);
        
        try {
            PreparedStatement ps;
            ps = SQLite.prepareStatement("CREATE TABLE IF NOT EXISTS " + FILTER_TABLE + " (name TEXT PRIMARY KEY, wholeWord BOOLEAN, deleteSource BOOLEAN, requireAll BOOLEAN, responseTitle TEXT, responseBody TEXT);");
            ps.executeUpdate();
            
            ps = SQLite.prepareStatement("CREATE TABLE IF NOT EXISTS " + ACTIVATION_WORDS_TABLE + " (activationWord TEXT PRIMARY KEY, name TEXT, FOREIGN KEY(name) REFERENCES " + FILTER_TABLE + "(name));");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        this.refreshFilters();
    }
    
    public void shutdown() {
        try {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private ResultSet getActivationWordsFromDB(String name) {
        try {
            PreparedStatement ps = SQLite.prepareStatement("SELECT * FROM " + ACTIVATION_WORDS_TABLE + " WHERE name = ?;");
            ps.setString(1, name);
            return ps.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public boolean refreshFilter(String name) {
        try {
            PreparedStatement ps = SQLite.prepareStatement("SELECT * FROM " + FILTER_TABLE + " WHERE name = ?;");
            ps.setString(1, name);
            ResultSet res = ps.executeQuery();
            
            if (res.next()) {
                ResultSet activationRes = getActivationWordsFromDB(res.getString("name"));
                ArrayList<String> activationWords = new ArrayList<String>();
                
                while (activationRes.next()) {
                    activationWords.add(activationRes.getString("activationWord"));
                }
                
                Filter filter = new Filter(
                        res.getString("name"), 
                        res.getBoolean("wholeWord"),
                        res.getBoolean("deleteSource"),
                        res.getBoolean("requireAll"),
                        res.getString("responseTitle"),
                        res.getString("responseBody"),
                        activationWords
                );
                filters.put(res.getString("name"), filter);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    public boolean refreshFilters() {
        filters = new HashMap<String, Filter>();
        
        try {
            ResultSet filterRes = SQLite.prepareStatement("SELECT * FROM " + FILTER_TABLE + ";").executeQuery();
            
            while (filterRes.next()) {
                ResultSet activationRes = getActivationWordsFromDB(filterRes.getString("name"));
                ArrayList<String> activationWords = new ArrayList<String>();
                
                while (activationRes.next()) {
                    activationWords.add(activationRes.getString("activationWord"));
                }
                
                Filter filter = new Filter(
                        filterRes.getString("name"), 
                        filterRes.getBoolean("wholeWord"), 
                        filterRes.getBoolean("deleteSource"), 
                        filterRes.getBoolean("requireAll"), 
                        filterRes.getString("responseTitle"), 
                        filterRes.getString("responseBody"), 
                        activationWords
                );
                filters.put(filterRes.getString("name"), filter);
            }
            
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    public Set<String> getFilterList() {
        return filters.keySet();
    }
    
    public Filter getFilter(String name) {
        return filters.get(name);
    }
    
    public boolean insertFilter(String name, String responseTitle, String responseBody) {
        try {
            PreparedStatement ps = SQLite.prepareStatement("INSERT INTO " + FILTER_TABLE + " (name, wholeWord, deleteSource, requireAll, responseTitle, responseBody) VALUES (?, 0, 0, 0, ?, ?);");
            ps.setString(1, name);
            ps.setString(2, responseTitle);
            ps.setString(3, responseBody);
            ps.executeUpdate();
            
            if (refreshFilter(name)) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    public boolean updateFilter(String name, String attribute, String value) {
        try {
            PreparedStatement ps;
            
            switch (attribute.toLowerCase()) {
            case "wholeword":
            case "deletesource":
            case "requireall":
                ps = SQLite.prepareStatement("UPDATE " + FILTER_TABLE + " SET " + attribute + " = ? WHERE name = ?;");
                ps.setBoolean(1, Boolean.parseBoolean(value));
                break;
            case "responsetitle":
            case "responsebody":
                ps = SQLite.prepareStatement("UPDATE " + FILTER_TABLE + " SET " + attribute + " = ? WHERE name = ?;");
                ps.setString(1, value);
                break;
            default:
                return false;
            }
            
            ps.setString(2, name);
            ps.executeUpdate();

            if (refreshFilter(name)) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    public boolean removeFilter(String name) {
        try {
            PreparedStatement ps = SQLite.prepareStatement("DELETE FROM " + ACTIVATION_WORDS_TABLE + " WHERE name = ?;");
            ps.setString(1, name);
            ps.executeUpdate();
            ps = SQLite.prepareStatement("DELETE FROM " + FILTER_TABLE + " WHERE name = ?;");
            ps.setString(1, name);
            ps.executeUpdate();

            if (refreshFilters()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    public boolean addActivationWord(String name, String word) {
        try {
            PreparedStatement ps = SQLite.prepareStatement("INSERT INTO " + ACTIVATION_WORDS_TABLE + " (activationWord, name) VALUES (?, ?);");
            ps.setString(1, word);
            ps.setString(2, name);
            ps.executeUpdate();
            
            if (refreshFilter(name)) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    public boolean removeActivationWord(String name, String word) {
        try {
            PreparedStatement ps = SQLite.prepareStatement("DELETE FROM " + ACTIVATION_WORDS_TABLE + " WHERE name = ? AND activationWord = ?;");
            ps.setString(1, name);
            ps.setString(2, word);
            ps.executeUpdate();
            
            if (refreshFilter(name)) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    public void filter(Member member, MessageChannel channel, Message message) {
        executor.execute(new FilterRunnable(member, channel, message));
    }
    
    private class FilterRunnable implements Runnable {
        
        private Member member;
        private MessageChannel channel;
        private Message message;
        
        public FilterRunnable(Member member, MessageChannel channel, Message message) {
            this.member = member;
            this.channel = channel;
            this.message = message;
        }
        
        @Override
        public void run() {
            for (Filter filter : filters.values()) {
                int totalWordCount = filter.getActivationWords().size();
                int wordCount = 0;
                
                for (String word : filter.getActivationWords()) {
                    Matcher matcher = filter.getPatternForWord(word).matcher(message.getContentDisplay());

                    if (matcher.matches()) {
                        wordCount++;
                    }
                }
                
                if ((wordCount == totalWordCount) || (wordCount > 0 && !filter.requireAll())) {
                    if (filter.deleteSource()) {
                        try {
                            message.delete().complete();
                        } catch (InsufficientPermissionException e) { }
                    }
                    
                    EmbedBuilder eb = EmbedUtil.newFootedEmbedBuilder(member);
                    eb.setTitle(filter.getResponseTitle());
                    eb.setDescription(filter.getResponseBody());
                    HifumiBot.getSelf().sendMessage(channel, eb.build());
                }
            }
        }
    }
}
