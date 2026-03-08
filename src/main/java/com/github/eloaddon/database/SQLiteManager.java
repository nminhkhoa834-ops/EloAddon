package com.github.eloaddon.database;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the SQLite database connection and all database operations.
 * Uses WAL mode, connection resilience, and proper resource handling.
 */
public class SQLiteManager {

    private static final DateTimeFormatter BACKUP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final Logger logger;
    private final File dbFile;
    private final String jdbcUrl;
    private Connection connection;

    public SQLiteManager(File dataFolder, Logger logger) {
        this.logger = logger;
        this.dbFile = new File(dataFolder, "data.db");
        this.jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        try {
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            openConnection();
            configurePragmas();
            createTables();
            logHealthCheck();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to initialize SQLite database!", e);
        }
    }


    private void openConnection() throws SQLException {
        connection = DriverManager.getConnection(jdbcUrl);
    }


    private Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            logger.warning("Database connection was lost, reconnecting...");
            openConnection();
            configurePragmas();
        }
        return connection;
    }


    private void configurePragmas() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL;");
            stmt.execute("PRAGMA synchronous=NORMAL;");
            stmt.execute("PRAGMA temp_store=MEMORY;");
            stmt.execute("PRAGMA foreign_keys=ON;");
        }
    }



    private void createTables() throws SQLException {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS claims (" +
                            "player_uuid TEXT NOT NULL, " +
                            "milestone_id TEXT NOT NULL, " +
                            "PRIMARY KEY (player_uuid, milestone_id))");
            stmt.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_claims_uuid ON claims(player_uuid)");
        }
    }

    private void logHealthCheck() {
        logger.info("=== SQLite Database Health ===");
        logger.info("Database file: " + dbFile.getAbsolutePath());

        try (Statement stmt = getConnection().createStatement()) {
            // Verify WAL mode
            try (ResultSet rs = stmt.executeQuery("PRAGMA journal_mode;")) {
                if (rs.next()) {
                    logger.info("Journal mode: " + rs.getString(1).toUpperCase());
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to run health check", e);
        }

        logger.info("Database connected successfully.");
    }


    public static void backupDatabase(File dataFolder, Logger logger) {
        File dbFile = new File(dataFolder, "data.db");
        if (!dbFile.exists()) {
            return;
        }

        String timestamp = LocalDateTime.now().format(BACKUP_FORMAT);
        File backupFile = new File(dataFolder, "data-backup-" + timestamp + ".db");
        try {
            Files.copy(dbFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Database backup created: " + backupFile.getName());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to create database backup", e);
        }
    }



    public void addClaim(String playerUuid, String milestoneId) {
        String sql = "INSERT OR IGNORE INTO claims (player_uuid, milestone_id) VALUES (?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, playerUuid);
            ps.setString(2, milestoneId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to add claim for " + playerUuid + " milestone " + milestoneId, e);
        }
    }

    public boolean hasClaimed(String playerUuid, String milestoneId) {
        String sql = "SELECT 1 FROM claims WHERE player_uuid = ? AND milestone_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, playerUuid);
            ps.setString(2, milestoneId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to check claim for " + playerUuid, e);
        }
        return false;
    }

    public Set<String> getClaimsForPlayer(String playerUuid) {
        Set<String> claims = new HashSet<>();
        String sql = "SELECT milestone_id FROM claims WHERE player_uuid = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, playerUuid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    claims.add(rs.getString("milestone_id"));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get claims for " + playerUuid, e);
        }
        return claims;
    }


    public void resetClaim(String playerUuid, String milestoneId) {
        String sql = "DELETE FROM claims WHERE player_uuid = ? AND milestone_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, playerUuid);
            ps.setString(2, milestoneId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to reset claim for " + playerUuid + " milestone " + milestoneId, e);
        }
    }


    public void resetAllClaims(String milestoneId) {
        String sql = "DELETE FROM claims WHERE milestone_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, milestoneId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to reset all claims for milestone " + milestoneId, e);
        }
    }


    public int resetPlayerClaims(String playerUuid) {
        String sql = "DELETE FROM claims WHERE player_uuid = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, playerUuid);
            return ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to reset claims for " + playerUuid, e);
        }
        return 0;
    }


    public boolean hasAnyClaims(String playerUuid) {
        String sql = "SELECT 1 FROM claims WHERE player_uuid = ? LIMIT 1";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, playerUuid);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to check claims for " + playerUuid, e);
        }
        return false;
    }



    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("SQLite database connection closed.");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to close database connection", e);
        }
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}

