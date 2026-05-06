package me.moormxi.playtime.api;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OceanPlaytimeApi {

    long getPlaytimeMillis(UUID uuid);

    Duration getPlaytime(UUID uuid);

    String formatPlaytime(UUID uuid);

    Optional<PlayerPlaytime> getPlayer(UUID uuid);

    Optional<PlayerPlaytime> getPlayer(String name);

    List<PlayerPlaytime> getTop(int limit);
}
