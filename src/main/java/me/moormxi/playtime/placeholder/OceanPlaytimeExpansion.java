package me.moormxi.playtime.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.moormxi.playtime.OceanPlaytimePlugin;
import me.moormxi.playtime.database.DatabaseManager;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class OceanPlaytimeExpansion extends PlaceholderExpansion {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    private final OceanPlaytimePlugin plugin;

    public OceanPlaytimeExpansion(OceanPlaytimePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "oceanplaytime";
    }

    @Override
    public @NotNull String getAuthor() {
        return "moormxi";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        } else {
            player.getUniqueId();
        }

        long millis = plugin.getPlaytimeManager().getPlaytime(player.getUniqueId());
        Duration duration = Duration.ofMillis(millis);

        return switch (params.toLowerCase()) {
            case "time", "formatted" -> plugin.getPlaytimeManager().formatTime(millis);
            case "millis", "milliseconds" -> Long.toString(millis);
            case "seconds" -> Long.toString(duration.toSeconds());
            case "minutes" -> Long.toString(duration.toMinutes());
            case "hours" -> Long.toString(duration.toHours());
            case "days" -> Long.toString(duration.toDays());
            case "first_join" -> formatFirstJoin(player);
            case "last_seen" -> formatLastSeen(player);
            default -> null;
        };
    }

    private String formatFirstJoin(OfflinePlayer player) {
        DatabaseManager.PlaytimeRecord record = plugin.getDatabaseManager().getByUuid(player.getUniqueId());
        if (record == null || record.firstJoin() <= 0L) {
            return "";
        }
        return DATE_FORMAT.format(Instant.ofEpochMilli(record.firstJoin()));
    }

    private String formatLastSeen(OfflinePlayer player) {
        DatabaseManager.PlaytimeRecord record = plugin.getDatabaseManager().getByUuid(player.getUniqueId());
        if (record == null || record.lastSeen() <= 0L) {
            return "";
        }
        return DATE_FORMAT.format(Instant.ofEpochMilli(record.lastSeen()));
    }
}
