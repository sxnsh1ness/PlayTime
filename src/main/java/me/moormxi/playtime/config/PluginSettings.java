package me.moormxi.playtime.config;

import me.moormxi.playtime.OceanPlaytimePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class PluginSettings {

    private static final long MIN_AUTOSAVE_SECONDS = 30L;

    private final OceanPlaytimePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private long autosaveIntervalSeconds;
    private String consoleUsageMessage;
    private String commandUsageMessage;
    private String selfPlaytimeMessage;
    private String otherPlaytimeMessage;
    private String playerNotFoundMessage;
    private String daysFormat;
    private String hoursFormat;
    private String minutesFormat;
    private String secondsFormat;
    private String separator;

    public PluginSettings(OceanPlaytimePlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.saveResource("messages.yml", false);
        plugin.reloadConfig();

        FileConfiguration config = plugin.getConfig();
        FileConfiguration messages = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "messages.yml"));

        autosaveIntervalSeconds = Math.max(
                MIN_AUTOSAVE_SECONDS,
                config.getLong("autosave-interval-seconds", 300L)
        );
        consoleUsageMessage = messages.getString("console-usage", "<red>Usage: %usage%");
        commandUsageMessage = messages.getString("command-usage", "<red>Usage: %usage%");
        selfPlaytimeMessage = messages.getString("self-playtime", "<aqua>Your playtime: <white>%time%");
        otherPlaytimeMessage = messages.getString("other-playtime", "<aqua>Playtime of %player%: <white>%time%");
        playerNotFoundMessage = messages.getString("player-not-found", "<red>No playtime data found for %player%.");
        daysFormat = messages.getString("time.days", "%value%d");
        hoursFormat = messages.getString("time.hours", "%value%h");
        minutesFormat = messages.getString("time.minutes", "%value%m");
        secondsFormat = messages.getString("time.seconds", "%value%s");
        separator = messages.getString("time.separator", " ");
    }

    public long autosaveIntervalTicks() {
        return autosaveIntervalSeconds * 20L;
    }

    public Component consoleUsage(String usage) {
        return parse(consoleUsageMessage, Map.of("usage", usage));
    }

    public Component commandUsage(String usage) {
        return parse(commandUsageMessage, Map.of("usage", usage));
    }

    public Component selfPlaytime(String time) {
        return parse(selfPlaytimeMessage, Map.of("time", time));
    }

    public Component otherPlaytime(String player, String time) {
        return parse(otherPlaytimeMessage, Map.of("player", player, "time", time));
    }

    public Component playerNotFound(String player) {
        return parse(playerNotFoundMessage, Map.of("player", player));
    }

    public String formatTime(long days, long hours, long minutes, long seconds) {
        if (days > 0) {
            return join(formatUnit(daysFormat, days), formatUnit(hoursFormat, hours), formatUnit(minutesFormat, minutes));
        }
        if (hours > 0) {
            return join(formatUnit(hoursFormat, hours), formatUnit(minutesFormat, minutes));
        }
        if (minutes > 0) {
            return join(formatUnit(minutesFormat, minutes), formatUnit(secondsFormat, seconds));
        }
        return formatUnit(secondsFormat, seconds);
    }

    private String formatUnit(String format, long value) {
        return format.replace("%value%", Long.toString(value));
    }

    private String join(String... parts) {
        return String.join(separator, parts);
    }

    private Component parse(String raw, Map<String, String> placeholders) {
        Map<String, String> escapedPlaceholders = new HashMap<>();
        for (var entry : placeholders.entrySet()) {
            escapedPlaceholders.put(entry.getKey(), MiniMessage.miniMessage().escapeTags(entry.getValue()));
        }

        String message = raw;
        for (var entry : escapedPlaceholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return miniMessage.deserialize(message);
    }
}
