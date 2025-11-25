package io.github.redpanda4552.HifumiBot.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.github.redpanda4552.HifumiBot.util.Log;
import io.github.redpanda4552.HifumiBot.util.Messaging;

public class SQLite {

    private Connection connection;

    public SQLite(String dataDirectory) {
        try {
            // NOTE: this shouldn't be needed for modern versions of java, it should just dynamically look
            // at the classpath for you, but leaving it here incase im wrong
            // Class.forName("org.sqlite.JDBC");
            var jbdcString = String.format("jdbc:sqlite:{}/hifumibot.db", dataDirectory);
            Log.info("Opening database with JBDC string: " + jbdcString);
            this.connection = DriverManager.getConnection(jbdcString);
        } catch (Exception e) {
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
