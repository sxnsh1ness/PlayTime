package com.github.sxnsh1ness.playtime.database;

import com.github.sxnsh1ness.playtime.PlaytimePlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;

public final class DatabaseManager implements AutoCloseable {

    private final PlaytimePlugin plugin;
    private final String url;
    private Connection connection;

    public DatabaseManager(PlaytimePlugin plugin) {
        this.plugin = plugin;
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("Could not create plugin data folder: " + plugin.getDataFolder());
        }

        File dbFile = new File(plugin.getDataFolder(), "playtime.db");
        this.url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
    }

    public void connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException exception) {
            throw new SQLException("SQLite JDBC driver was not found.", exception);
        }

        this.connection = DriverManager.getConnection(url);
        init();
    }

    private synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url);
        }
        return connection;
    }

    private void init() throws SQLException {
        synchronized (this) {
            try (Statement statement = getConnection().createStatement()) {
                statement.execute("PRAGMA journal_mode=WAL");
                statement.execute("PRAGMA foreign_keys=ON");
                statement.execute("""
                        CREATE TABLE IF NOT EXISTS playtime (
                            uuid TEXT PRIMARY KEY NOT NULL,
                            name TEXT NOT NULL DEFAULT '',
                            first_join INTEGER NOT NULL DEFAULT 0,
                            last_seen INTEGER NOT NULL DEFAULT 0,
                            total_ms INTEGER NOT NULL DEFAULT 0
                        )
                        """);
            }

            ensureColumn("name", "TEXT NOT NULL DEFAULT ''");
            ensureColumn("first_join", "INTEGER NOT NULL DEFAULT 0");
            ensureColumn("last_seen", "INTEGER NOT NULL DEFAULT 0");
        }
    }

    private void ensureColumn(String columnName, String definition) throws SQLException {
        if (hasColumn(columnName)) {
            return;
        }

        try (Statement statement = getConnection().createStatement()) {
            statement.execute("ALTER TABLE playtime ADD COLUMN " + columnName + " " + definition);
        }
    }

    private boolean hasColumn(String columnName) throws SQLException {
        try (ResultSet columns = getConnection().getMetaData().getColumns(null, null, "playtime", columnName)) {
            return columns.next();
        }
    }

    public synchronized void touchPlayer(UUID uuid, String name, long joinedAt) {
        String sql = """
                INSERT INTO playtime (uuid, name, first_join, last_seen, total_ms)
                VALUES (?, ?, ?, ?, 0)
                ON CONFLICT(uuid) DO UPDATE SET
                    name = excluded.name,
                    last_seen = excluded.last_seen,
                    first_join = CASE
                        WHEN playtime.first_join = 0 THEN excluded.first_join
                        ELSE playtime.first_join
                    END
                """;

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, name);
            statement.setLong(3, joinedAt);
            statement.setLong(4, joinedAt);
            statement.executeUpdate();
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.SEVERE, "Could not touch playtime row for " + uuid, exception);
        }
    }

    public synchronized void addPlaytime(UUID uuid, String name, long firstJoin, long lastSeen, long deltaMs) {
        long safeDelta = Math.max(0L, deltaMs);
        String sql = """
                INSERT INTO playtime (uuid, name, first_join, last_seen, total_ms)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT(uuid) DO UPDATE SET
                    name = excluded.name,
                    last_seen = excluded.last_seen,
                    total_ms = playtime.total_ms + excluded.total_ms,
                    first_join = CASE
                        WHEN playtime.first_join = 0 THEN excluded.first_join
                        ELSE playtime.first_join
                    END
                """;

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, name);
            statement.setLong(3, firstJoin);
            statement.setLong(4, lastSeen);
            statement.setLong(5, safeDelta);
            statement.executeUpdate();
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.SEVERE, "Could not save playtime for " + uuid, exception);
        }
    }

    public synchronized PlaytimeRecord getByUuid(UUID uuid) {
        String sql = "SELECT uuid, name, first_join, last_seen, total_ms FROM playtime WHERE uuid = ?";

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, uuid.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return readRecord(resultSet);
                }
            }
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.SEVERE, "Could not load playtime for " + uuid, exception);
        }

        return null;
    }

    public synchronized PlaytimeRecord getByName(String name) {
        String sql = """
                SELECT uuid, name, first_join, last_seen, total_ms
                FROM playtime
                WHERE lower(name) = ?
                ORDER BY last_seen DESC
                LIMIT 1
                """;

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, name.toLowerCase(Locale.ROOT));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return readRecord(resultSet);
                }
            }
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.SEVERE, "Could not load playtime for name " + name, exception);
        }

        return null;
    }

    public long getStoredPlaytime(UUID uuid) {
        PlaytimeRecord record = getByUuid(uuid);
        return record == null ? 0L : record.totalMs();
    }

    public List<TopRecord> getTopPlaytime(int limit) {
        List<TopRecord> top = new ArrayList<>();

        synchronized (this) {
            String sql = "SELECT uuid, name, total_ms FROM playtime ORDER BY total_ms DESC LIMIT ?";
            try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
                statement.setInt(1, Math.max(1, limit));

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        top.add(new TopRecord(
                                UUID.fromString(resultSet.getString("uuid")),
                                resultSet.getString("name"),
                                resultSet.getLong("total_ms")
                        ));
                    }
                }
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.SEVERE, "Could not load top playtime", exception);
            }
        }

        return top;
    }

    private PlaytimeRecord readRecord(ResultSet resultSet) throws SQLException {
        return new PlaytimeRecord(
                UUID.fromString(resultSet.getString("uuid")),
                resultSet.getString("name"),
                resultSet.getLong("first_join"),
                resultSet.getLong("last_seen"),
                resultSet.getLong("total_ms")
        );
    }

    @Override
    public synchronized void close() {
        if (connection == null) {
            return;
        }

        try {
            connection.close();
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.WARNING, "Could not close SQLite connection", exception);
        } finally {
            connection = null;
        }
    }

    public record PlaytimeRecord(UUID uuid, String name, long firstJoin, long lastSeen, long totalMs) {
    }

    public record TopRecord(UUID uuid, String name, long value) {
    }
}
