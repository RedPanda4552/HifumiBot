package io.github.redpanda4552.HifumiBot.database;

public class FilterEventObject {

    private long userId;
    private long messageId;
    private long timestamp;
    private String filterName;
    private String filterRegexName;
    private boolean informational;
    
    public FilterEventObject(long userId, long messageId, long timestamp, String filterName, String filterRegexName, boolean informational) {
        this.userId = userId;
        this.messageId = messageId;
        this.timestamp = timestamp;
        this.filterName = filterName;
        this.filterRegexName = filterRegexName;
        this.informational = informational;
    }

    public long getUserId() {
        return this.userId;
    }

    public long getMessageId() {
        return this.messageId;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public String getFilterName() {
        return this.filterName;
    }

    public String getFilterRegexName() {
        return this.filterRegexName;
    }

    public boolean isInformational() {
        return this.informational;
    }
}
