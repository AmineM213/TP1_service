package com.hautlesbas.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static DatabaseManager db;
    private final HikariDataSource dataSource;

    private DatabaseManager() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:./hautlesbas_db;DB_CLOSE_DELAY=-1;MODE=Legacy");
        config.setUsername("sa");
        config.setPassword("");

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.dataSource = new HikariDataSource(config);
        initDb();
    }

    public static synchronized DatabaseManager getDb() {
        if (db == null) {
            db = new DatabaseManager();
        }
        return db;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private void initDb() {
        String createChaussetteSql = """
            CREATE TABLE IF NOT EXISTS chaussette (
                id INT PRIMARY KEY AUTO_INCREMENT,
                couleur VARCHAR(50),
                taille VARCHAR(20),
                type_tissu VARCHAR(50),
                prix DOUBLE
            )
        """;

        String createVenteSql = """
            CREATE TABLE IF NOT EXISTS vente (
                id INT PRIMARY KEY AUTO_INCREMENT,
                date_vente TIMESTAMP,
                total DOUBLE
            )
        """;

        String createVenteDetailSql = """
            CREATE TABLE IF NOT EXISTS vente_detail (
                vente_id INT,
                chaussette_id_org INT,
                couleur VARCHAR(50),
                taille VARCHAR(20),
                type_tissu VARCHAR(50),
                prix DOUBLE,
                FOREIGN KEY (vente_id) REFERENCES vente(id) ON DELETE CASCADE
            )
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createChaussetteSql);
            stmt.execute(createVenteSql);
            stmt.execute(createVenteDetailSql);
            System.out.println("Base de données initialisée avec succès.");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'initialisation de la base de données", e);
        }
    }
}