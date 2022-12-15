// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.util;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import org.xbill.DNS.Address;

public class Internet {
  public static void init() {
    if (HifumiBot.getSelf().getVersion() != null
        && HifumiBot.getSelf().getConfig().useLocalDNSFiltering) {
      System.setProperty("dns.server", "127.0.0.1");
    }
  }

  public static DNSQueryResult nslookup(String urlStr) {
    try {
      URL url = new URL(urlStr);
      String host = url.getHost();
      InetAddress addr = Address.getByName(host);
      String ret = addr.getHostAddress();

      if (ret.equals("0.0.0.0")) {
        return DNSQueryResult.BLOCKED;
      } else {
        return DNSQueryResult.SUCCESS;
      }
    } catch (UnknownHostException | MalformedURLException e) {
      return DNSQueryResult.FAIL;
    }
  }
}
