package com.github.sxnsh1ness.playtime.api;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public record PlayerPlayTime(
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
