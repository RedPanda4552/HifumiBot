package io.github.redpanda4552.HifumiBot.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.github.redpanda4552.HifumiBot.util.Messaging;

public class SQLite {

    private Connection connection;

    public SQLite() {
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:hifumibot.db");
        } catch (SQLException e) {
            Messaging.logException("SQlite", "(constructor)", e);
        }
    }

    public Connection getConnection() {
        return this.connection;
    }

    public void shutdown() {
        try {
            this.connection.close();
        } catch (SQLException e) {
            Messaging.logException("SQLite", "shutdown", e);
        }
    }
}
