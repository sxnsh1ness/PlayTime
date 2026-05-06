package me.moormxi.playtime.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class PlayerPlaytimeSaveEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID uuid;
    private final String playerName;
    private final long previousTotalMillis;
    private final long savedDeltaMillis;
    private final long newTotalMillis;
    private final long savedAt;

    public PlayerPlaytimeSaveEvent(
            boolean async,
            UUID uuid,
            String playerName,
            long previousTotalMillis,
            long savedDeltaMillis,
            long newTotalMillis,
            long savedAt
    ) {
        super(async);
        this.uuid = uuid;
        this.playerName = playerName;
        this.previousTotalMillis = previousTotalMillis;
        this.savedDeltaMillis = savedDeltaMillis;
        this.newTotalMillis = newTotalMillis;
        this.savedAt = savedAt;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public long getPreviousTotalMillis() {
        return previousTotalMillis;
    }

    public long getSavedDeltaMillis() {
        return savedDeltaMillis;
    }

    public long getNewTotalMillis() {
        return newTotalMillis;
    }

    public long getSavedAt() {
        return savedAt;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
