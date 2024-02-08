package io.github.redpanda4552.HifumiBot.database;

public class WarezEventObject {

    private long timestamp;
    private long userId;
    private Action action;

    public WarezEventObject(long timestamp, long userId, Action action) {
        this.timestamp = timestamp;
        this.userId = userId;
        this.action = action;
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

    public enum Action {
        ADD("add"),
        REMOVE("remove");

        public final String stringValue;

        private Action(String action) {
            this.stringValue = action;
        }
    }
}
