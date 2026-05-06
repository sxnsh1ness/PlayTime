package com.github.sxnsh1ness.playtime.manager;

import com.github.sxnsh1ness.playtime.PlayTimePlugin;
import com.github.sxnsh1ness.playtime.event.*;
import com.github.sxnsh1ness.playtime.manager.model.PlayTimeEntry;
import com.github.sxnsh1ness.playtime.manager.model.Session;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayTimeManager {

    private final PlayTimePlugin plugin;
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

    public PlayTimeManager(PlayTimePlugin plugin) {
        this.plugin = plugin;
    }

    public void handleJoin(Player player) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        sessions.put(uuid, new Session(now));
        Bukkit.getPluginManager().callEvent(new PlayerPlayTimeStartEvent(uuid, player.getName(), now));

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                plugin.getDatabaseManager().touchPlayer(uuid, player.getName(), now)
        );
    }

    public void handleQuit(Player player) {
        savePlayer(player, true, true);
    }

    public void saveAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            savePlayer(player, false, false);
        }
    }

    public void saveAllSync() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            savePlayer(player, false, true);
        }
    }

    public long getPlayTime(UUID uuid) {
        long total = plugin.getDatabaseManager().getStoredPlayTime(uuid);
        Session session = sessions.get(uuid);
        if (session == null) {
            return total;
        }

        return total + Math.max(0L, System.currentTimeMillis() - session.savedUntil());
    }

    public List<PlayTimeEntry> getTopPlayTime(int limit) {
        List<PlayTimeEntry> result = new ArrayList<>();
        var top = plugin.getDatabaseManager().getTopPlayTime(limit);
        for (var record : top) {
            result.add(new PlayTimeEntry(record.uuid(), record.name(), record.value()));
        }
        return result;
    }

    public String formatTime(long ms) {
        Duration duration = Duration.ofMillis(Math.max(0L, ms));
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        return plugin.getSettings().formatTime(days, hours, minutes, seconds);
    }

    private void savePlayer(Player player, boolean removeSession, boolean sync) {
        UUID uuid = player.getUniqueId();
        String playerName = player.getName();
        Session session = removeSession ? sessions.remove(uuid) : sessions.get(uuid);
        if (session == null) {
            return;
        }

        long now = System.currentTimeMillis();
        long previousSave = session.getAndSetSavedUntil(now);
        long delta = Math.max(0L, now - previousSave);
        if (delta == 0L) {
            return;
        }

        if (sync) {
            saveAndCallEvents(uuid, playerName, session, now, delta, removeSession);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveAndCallEvents(uuid, playerName, session, now, delta, removeSession));
    }

    private void saveAndCallEvents(UUID uuid, String playerName, Session session, long now, long delta, boolean stopped) {
        long previousTotal = plugin.getDatabaseManager().getStoredPlayTime(uuid);
        long newTotal = previousTotal + delta;

        plugin.getDatabaseManager().addPlayTime(uuid, playerName, session.joinedAt(), now, delta);
        Bukkit.getPluginManager().callEvent(new PlayerPlayTimeSaveEvent(
                !Bukkit.isPrimaryThread(),
                uuid,
                playerName,
                previousTotal,
                delta,
                newTotal,
                now
        ));

        if (stopped) {
            Bukkit.getPluginManager().callEvent(new PlayerPlayTimeStopEvent(
                    uuid,
                    playerName,
                    now - session.joinedAt(),
                    newTotal,
                    now
            ));
        }
    }

}
