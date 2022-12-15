// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.wiki;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import io.github.redpanda4552.HifumiBot.util.Refreshable;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WikiIndex implements Refreshable {

  private static final String FULL_GAMES_URL = "https://wiki.pcsx2.net/Complete_List_of_Games";

  private final ConcurrentHashMap<String, String> fullGamesMap = new ConcurrentHashMap<>();

  public WikiIndex() {
    this.refresh();
  }

  @Override
  public synchronized void refresh() {
    try {
      // Attempt to retrieve the games list, if successful, wipe the
      // current map and repopulate it.
      Document doc =
          Jsoup.connect(FULL_GAMES_URL)
              .header("user-agent", "hifumibot/" + HifumiBot.getSelf().getVersion())
              .maxBodySize(0)
              .get();
      Elements anchors = doc.getElementsByClass("wikitable").first().getElementsByTag("a");

      this.clear();

      for (Element anchor : anchors) {
        this.addGame(anchor.attr("title"), WikiPage.BASE_URL + anchor.attr("href"));
      }
    } catch (IOException e) {
      Messaging.logException("WikiIndex", "refresh", e);
    }
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
