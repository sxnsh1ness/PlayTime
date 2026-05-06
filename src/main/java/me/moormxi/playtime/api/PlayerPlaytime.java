package me.moormxi.playtime.api;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public record PlayerPlaytime(
        UUID uuid,
        String name,
        Instant firstJoin,
        Instant lastSeen,
        long playtimeMillis
) {

    public Duration playtime() {
        return Duration.ofMillis(playtimeMillis);
    }
}
