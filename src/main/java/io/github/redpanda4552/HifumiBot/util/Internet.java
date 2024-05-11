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
package io.github.redpanda4552.HifumiBot.util;

import java.net.InetAddress;
import java.net.URI;
import java.net.URL;

import org.xbill.DNS.Address;

import io.github.redpanda4552.HifumiBot.HifumiBot;

public class Internet {
    public static void init() {
        if (HifumiBot.getSelf().getVersion() != null && HifumiBot.getSelf().getConfig().filterOptions.useLocalDNSFiltering) {
            System.setProperty("dns.server", HifumiBot.getSelf().getConfig().filterOptions.localDNSFilterAddress);
        }
    }

    public static DNSQueryResult nslookup(String urlStr) {
        try {
            URL url = new URI(urlStr).toURL();
            String host = url.getHost();
            InetAddress addr = Address.getByName(host);
            String ret = addr.getHostAddress();

            if (ret.equals("0.0.0.0")) {
                return DNSQueryResult.BLOCKED;
            } else {
                return DNSQueryResult.SUCCESS;
            }
        } catch (Exception e) {
            return DNSQueryResult.FAIL;
        }
    }
}
