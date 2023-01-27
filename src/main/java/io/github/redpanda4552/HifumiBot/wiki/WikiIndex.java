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
package io.github.redpanda4552.HifumiBot.wiki;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import io.github.redpanda4552.HifumiBot.util.Refreshable;

public class WikiIndex implements Refreshable {

    private static final String FULL_GAMES_URL = "https://wiki.pcsx2.net/Complete_List_of_Games";

    private boolean isInitialized = false;
    private ConcurrentHashMap<String, String> fullGamesMap = new ConcurrentHashMap<String, String>();

    public WikiIndex() {
        
    }

    @Override
    public synchronized void refresh() {
        try {
            // Attempt to retrieve the games list, if successful, wipe the
            // current map and repopulate it.
            Document doc = Jsoup.connect(FULL_GAMES_URL).header("user-agent", "hifumibot/" + HifumiBot.getSelf().getVersion()).maxBodySize(0).get();
            Elements anchors = doc.getElementsByClass("wikitable").first().getElementsByTag("a");

            this.clear();

            for (Element anchor : anchors) {
                this.addGame(anchor.attr("title"), WikiPage.BASE_URL + anchor.attr("href"));
            }

            this.isInitialized = true;
        } catch (IOException e) {
            Messaging.logException("WikiIndex", "refresh", e);
        }
    }

    public synchronized boolean isInitialized() {
        return this.isInitialized;
    }

    public synchronized void clear() {
        fullGamesMap.clear();
    }

    public synchronized void addGame(String title, String wikiPageUrl) {
        fullGamesMap.put(title, wikiPageUrl);
    }

    public synchronized Set<String> getAllTitles() {
        return fullGamesMap.keySet();
    }

    public synchronized String getWikiPageUrl(String title) {
        return fullGamesMap.get(title);
    }
}
