package io.github.redpanda4552.HifumiBot.database;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
            var jbdcString = String.format("jdbc:sqlite:%s/hifumibot.db", dataDirectory);
            Log.info("Opening database with JBDC string: " + jbdcString);
            this.connection = DriverManager.getConnection(jbdcString);
            ensureDatabaseIsInitialized();
        } catch (Exception e) {
            Messaging.logException("SQlite", "(constructor)", e);
        }
    }

    // NOTE: order is potentially important here
    // each file should contain a single valid SQL statement
    private String[] schemaMigrations = {
        "000-create-table.sql"
    };

    private void ensureDatabaseIsInitialized() {
        var conn = this.connection;
        try {
            conn.setAutoCommit(false); // begin transaction
            for (var migrationFile : schemaMigrations) {
                var resourcePath = String.format("db/migrations/%s", migrationFile);
                var is = SQLite.class.getClassLoader().getResourceAsStream(resourcePath);
                if (is == null) {
                    throw new RuntimeException("Resource not found: " + resourcePath);
                }
                String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                try (var statement = conn.createStatement()) {
                    statement.execute(sql.trim());
                }
            }

            conn.commit(); // commit all statements
        } catch (Exception e) {
            try {
                conn.rollback(); // rollback if anything fails
            } catch (SQLException rollbackEx) {
                e.addSuppressed(rollbackEx); // don't lose original exception
            }
            throw new RuntimeException("Unable to ensure database is initialized properly", e);
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
