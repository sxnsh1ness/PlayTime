package com.github.sxnsh1ness.playtime.api;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayTimeAPI {

    long getPlayTimeMillis(UUID uuid);

    Duration getPlayTime(UUID uuid);

    String formatPlayTime(UUID uuid);

    Optional<PlayerPlayTime> getPlayer(UUID uuid);

    Optional<PlayerPlayTime> getPlayer(String name);

    List<PlayerPlayTime> getTop(int limit);
}
