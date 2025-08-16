package com.karta.petsplus.manager;

import com.karta.petsplus.KartaPetsPlus;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private final KartaPetsPlus plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(KartaPetsPlus plugin) {
        this.plugin = plugin;
        setup();
    }

    private void setup() {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        if (!config.getString("storage-type", "yaml").equalsIgnoreCase("mysql")) {
            return;
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + config.getString("database.host") + ":" + config.getInt("database.port") + "/" + config.getString("database.database"));
        hikariConfig.setUsername(config.getString("database.username"));
        hikariConfig.setPassword(config.getString("database.password"));
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(hikariConfig);

        createTables();
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    private void createTables() {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS pets (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "owner_uuid VARCHAR(36) NOT NULL," +
                    "pet_type VARCHAR(255) NOT NULL," +
                    "pet_name VARCHAR(255) NOT NULL," +
                    "pet_status VARCHAR(255) NOT NULL" +
                    ")");
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not create database tables: " + e.getMessage());
        }
    }
}
