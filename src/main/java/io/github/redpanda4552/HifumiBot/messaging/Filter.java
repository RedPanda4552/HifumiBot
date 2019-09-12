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
package io.github.redpanda4552.HifumiBot.messaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Pattern;

public class Filter {

    private String name;
    private boolean wholeWord, deleteSource, requireAll;
    private String responseTitle, responseBody;
    private HashMap<String, Pattern> activationWords;
    
    public Filter(String name, boolean wholeWord, boolean deleteSource, boolean requireAll, String responseTitle, String responseBody, ArrayList<String> activationWords) {
        this.name = name;
        this.wholeWord = wholeWord;
        this.responseTitle = responseTitle;
        this.responseBody = responseBody;
        this.activationWords = new HashMap<String, Pattern>();
        
        for (String activationWord : activationWords) {
            StringBuilder regexBuilder = new StringBuilder(".*");
            
            if (wholeWord) {
                regexBuilder.append("\\b");
            }
            
            regexBuilder.append(activationWord);
            
            if (wholeWord) {
                regexBuilder.append("\\b");
            }
            
            regexBuilder.append(".*");
            this.activationWords.put(activationWord, Pattern.compile(regexBuilder.toString(), Pattern.CASE_INSENSITIVE));
        }
    }
    
    /**
     * Get the name of this filter.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get whether or not this filter uses whole word matching.
     * @return True if whole word matching, false otherwise.
     */
    public boolean wholeWord() {
        return wholeWord;
    }
    
    /**
     * Get whether or not this filter will attempt to delete the source message.
     * @return True if it will attempt, false otherwise.
     */
    public boolean deleteSource() {
        return deleteSource;
    }
    
    /**
     * Get whether or not this filter requires all activation words to be
     * present to fire its response.
     * @return True if all activation words are required, false if any one word
     * will activate the filter.
     */
    public boolean requireAll() {
        return requireAll;
    }
    
    /**
     * Get the title this filter will respond with.
     * @return The response title
     */
    public String getResponseTitle() {
        return responseTitle;
    }
    
    /**
     * Get the body this filter will respond with.
     * @return The response body
     */
    public String getResponseBody() {
        return responseBody;
    }
    
    /**
     * Get the words that will activate this filter. 
     * @return Set of words
     */
    public Set<String> getActivationWords() {
        return activationWords.keySet();
    }
    
    /**
     * Get the Pattern for an activation word.
     * @return Pattern object
     */
    public Pattern getPatternForWord(String word) {
        return activationWords.get(word);
    }
}
