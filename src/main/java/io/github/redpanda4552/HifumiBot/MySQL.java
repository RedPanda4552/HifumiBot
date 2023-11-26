package io.github.redpanda4552.HifumiBot;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.github.redpanda4552.HifumiBot.config.Config.MySQLOptions;

public class MySQL {
    
    public static void closeConnection(Connection conn) {
        if (conn != null) { try { conn.close(); } catch (SQLException e) { } };
    }

    private HikariDataSource ds;

    public MySQL() {
        MySQLOptions opt = HifumiBot.getSelf().getConfig().mysql;

        HikariConfig conf = new HikariConfig();
        conf.setJdbcUrl("jdbc:mysql://" + opt.url);
        conf.setUsername(opt.username);
        conf.setPassword(opt.password);
        conf.addDataSourceProperty("cachePrepStmts", "true");
        conf.addDataSourceProperty("prepStmtCacheSize", "250");
        conf.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.ds = new HikariDataSource(conf);
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public void shutdown() {
        ds.close();
    }
}
