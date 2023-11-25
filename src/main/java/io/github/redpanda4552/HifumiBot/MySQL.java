package io.github.redpanda4552.HifumiBot;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.github.redpanda4552.HifumiBot.config.Config.MySQLOptions;

public class MySQL {
    
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

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return ds.getConnection().prepareStatement(sql);
    }

    public void shutdown() {
        ds.close();
    }
}
