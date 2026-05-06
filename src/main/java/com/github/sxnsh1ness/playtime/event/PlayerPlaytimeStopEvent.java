package com.github.sxnsh1ness.playtime.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class PlayerPlayTimeStopEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID uuid;
    private final String playerName;
    private final long sessionMillis;
    private final long totalMillis;
    private final long stoppedAt;

    public PlayerPlayTimeStopEvent(UUID uuid, String playerName, long sessionMillis, long totalMillis, long stoppedAt) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.sessionMillis = sessionMillis;
        this.totalMillis = totalMillis;
        this.stoppedAt = stoppedAt;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public long getSessionMillis() {
        return sessionMillis;
    }

    public long getTotalMillis() {
        return totalMillis;
    }

    public long getStoppedAt() {
        return stoppedAt;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
