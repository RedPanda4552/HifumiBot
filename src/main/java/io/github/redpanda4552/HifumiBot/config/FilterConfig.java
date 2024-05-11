package io.github.redpanda4552.HifumiBot.config;

import java.util.concurrent.ConcurrentHashMap;

import io.github.redpanda4552.HifumiBot.filter.FilterObject;

public class FilterConfig implements IConfig {

    @Override
    public ConfigType getConfigType() {
        return ConfigType.FILTERS;
    }

    @Override
    public boolean usePrettyPrint() {
        return true;
    }

    public ConcurrentHashMap<String, FilterObject> filters;

    public FilterConfig() {
        filters = new ConcurrentHashMap<String, FilterObject>();
    }
}
