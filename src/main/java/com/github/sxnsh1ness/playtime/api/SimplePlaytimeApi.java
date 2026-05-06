package com.github.sxnsh1ness.playtime.api;

import com.github.sxnsh1ness.playtime.database.DatabaseManager;
import com.github.sxnsh1ness.playtime.database.model.PlayTimeRecord;
import com.github.sxnsh1ness.playtime.manager.PlayTimeManager;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class SimplePlayTimeAPI implements PlayTimeAPI {

    private final PlayTimeManager playtimeManager;
    private final DatabaseManager databaseManager;

    public SimplePlayTimeAPI(PlayTimeManager playtimeManager, DatabaseManager databaseManager) {
        this.playtimeManager = playtimeManager;
        this.databaseManager = databaseManager;
    }

    @Override
    public long getPlayTimeMillis(UUID uuid) {
        return playtimeManager.getPlayTime(uuid);
    }

    @Override
    public Duration getPlayTime(UUID uuid) {
        return Duration.ofMillis(getPlayTimeMillis(uuid));
    }

    @Override
    public String formatPlayTime(UUID uuid) {
        return playtimeManager.formatTime(getPlayTimeMillis(uuid));
    }

    @Override
    public Optional<PlayerPlayTime> getPlayer(UUID uuid) {
        return Optional.ofNullable(databaseManager.getByUuid(uuid))
                .map(record -> toPlayer(record, getPlayTimeMillis(uuid)));
    }

    @Override
    public Optional<PlayerPlayTime> getPlayer(String name) {
        return Optional.ofNullable(databaseManager.getByName(name))
                .map(record -> toPlayer(record, getPlayTimeMillis(record.uuid())));
    }

    @Override
    public List<PlayerPlayTime> getTop(int limit) {
        return databaseManager.getTopPlayTime(limit).stream()
                .map(record -> new PlayerPlayTime(
                        record.uuid(),
                        record.name(),
                        Instant.EPOCH,
                        Instant.EPOCH,
                        record.value()
                ))
                .toList();
    }

    private PlayerPlayTime toPlayer(PlayTimeRecord record, long currentPlayTimeMillis) {
        return new PlayerPlayTime(
                record.uuid(),
                record.name(),
                Instant.ofEpochMilli(record.firstJoin()),
                Instant.ofEpochMilli(record.lastSeen()),
                currentPlayTimeMillis
        );
    }
}
