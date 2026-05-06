package me.moormxi.playtime.api;

import me.moormxi.playtime.database.DatabaseManager;
import me.moormxi.playtime.manager.PlaytimeManager;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class SimpleOceanPlaytimeApi implements OceanPlaytimeApi {

    private final PlaytimeManager playtimeManager;
    private final DatabaseManager databaseManager;

    public SimpleOceanPlaytimeApi(PlaytimeManager playtimeManager, DatabaseManager databaseManager) {
        this.playtimeManager = playtimeManager;
        this.databaseManager = databaseManager;
    }

    @Override
    public long getPlaytimeMillis(UUID uuid) {
        return playtimeManager.getPlaytime(uuid);
    }

    @Override
    public Duration getPlaytime(UUID uuid) {
        return Duration.ofMillis(getPlaytimeMillis(uuid));
    }

    @Override
    public String formatPlaytime(UUID uuid) {
        return playtimeManager.formatTime(getPlaytimeMillis(uuid));
    }

    @Override
    public Optional<PlayerPlaytime> getPlayer(UUID uuid) {
        return Optional.ofNullable(databaseManager.getByUuid(uuid))
                .map(record -> toPlayer(record, getPlaytimeMillis(uuid)));
    }

    @Override
    public Optional<PlayerPlaytime> getPlayer(String name) {
        return Optional.ofNullable(databaseManager.getByName(name))
                .map(record -> toPlayer(record, getPlaytimeMillis(record.uuid())));
    }

    @Override
    public List<PlayerPlaytime> getTop(int limit) {
        return databaseManager.getTopPlaytime(limit).stream()
                .map(record -> new PlayerPlaytime(
                        record.uuid(),
                        record.name(),
                        Instant.EPOCH,
                        Instant.EPOCH,
                        record.value()
                ))
                .toList();
    }

    private PlayerPlaytime toPlayer(DatabaseManager.PlaytimeRecord record, long currentPlaytimeMillis) {
        return new PlayerPlaytime(
                record.uuid(),
                record.name(),
                Instant.ofEpochMilli(record.firstJoin()),
                Instant.ofEpochMilli(record.lastSeen()),
                currentPlaytimeMillis
        );
    }
}
