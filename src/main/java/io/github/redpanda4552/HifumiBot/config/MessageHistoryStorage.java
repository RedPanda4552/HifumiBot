package io.github.redpanda4552.HifumiBot.config;

import java.util.concurrent.ConcurrentHashMap;

import io.github.redpanda4552.HifumiBot.filter.MessageHistoryEntry;

public class MessageHistoryStorage implements IConfig {
    
    @Override
    public ConfigType getConfigType() {
        return ConfigType.MESSAGE_HISTORY;
    }
    
    @Override
    public boolean usePrettyPrint() {
        return false;
    }
    
    public ConcurrentHashMap<String, MessageHistoryEntry> messageHistory;

    public MessageHistoryStorage() {
        messageHistory = new ConcurrentHashMap<String, MessageHistoryEntry>();
    }
}
