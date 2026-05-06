package com.github.sxnsh1ness.playtime.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class PlayerPlayTimeStartEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID uuid;
    private final String playerName;
    private final long startedAt;

    public PlayerPlayTimeStartEvent(UUID uuid, String playerName, long startedAt) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.startedAt = startedAt;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public long getStartedAt() {
        return startedAt;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
