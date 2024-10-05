package io.github.redpanda4552.HifumiBot.database;

import java.util.Optional;

public class WarezEventObject {

    private long timestamp;
    private long userId;
    private Action action;
    private Long messageId;

    public WarezEventObject(long timestamp, long userId, Action action, Long messageId) {
        this.timestamp = timestamp;
        this.userId = userId;
        this.action = action;
        this.messageId = messageId;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public long getUserId() {
        return this.userId;
    }

    public Action getAction() {
        return this.action;
    }

    public Optional<Long> getMessageId() {
        return Optional.ofNullable(messageId);
    }

    public enum Action {
        ADD,
        REMOVE;
    }
}
