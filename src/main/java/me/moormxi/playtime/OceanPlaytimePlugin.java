package me.moormxi.playtime;

import me.moormxi.playtime.api.OceanPlaytimeApi;
import me.moormxi.playtime.api.SimpleOceanPlaytimeApi;
import me.moormxi.playtime.command.PlaytimeCommand;
import me.moormxi.playtime.config.PluginSettings;
import me.moormxi.playtime.database.DatabaseManager;
import me.moormxi.playtime.manager.PlaytimeManager;
import me.moormxi.playtime.placeholder.OceanPlaytimeExpansion;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.logging.Level;

public final class OceanPlaytimePlugin extends JavaPlugin implements Listener {

    private DatabaseManager databaseManager;
    private PlaytimeManager playtimeManager;
    private OceanPlaytimeApi playtimeApi;
    private OceanPlaytimeExpansion placeholderExpansion;
    private PluginSettings settings;

    @Override
    public void onEnable() {
        this.settings = new PluginSettings(this);
        this.settings.load();

        this.databaseManager = new DatabaseManager(this);
        try {
            this.databaseManager.connect();
        } catch (SQLException exception) {
            getLogger().log(Level.SEVERE, "Could not initialize SQLite database.", exception);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.playtimeManager = new PlaytimeManager(this);
        this.playtimeApi = new SimpleOceanPlaytimeApi(playtimeManager, databaseManager);

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getServicesManager().register(OceanPlaytimeApi.class, playtimeApi, this, ServicePriority.Normal);
        registerPlaceholders();

        PlaytimeCommand command = new PlaytimeCommand(this, playtimeManager);
        PluginCommand pluginCommand = getCommand("playtime");
        if (pluginCommand == null) {
            getLogger().severe("Command /playtime is missing from plugin.yml.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        pluginCommand.setExecutor(command);
        pluginCommand.setTabCompleter(command);

        Bukkit.getScheduler().runTaskTimer(this, playtimeManager::saveAll, settings.autosaveIntervalTicks(), settings.autosaveIntervalTicks());
        for (var player : Bukkit.getOnlinePlayers()) {
            playtimeManager.handleJoin(player);
        }

        getLogger().info("OceanPlaytime enabled.");
    }

    @Override
    public void onDisable() {
        if (playtimeManager != null) {
            playtimeManager.saveAllSync();
        }
        if (placeholderExpansion != null) {
            placeholderExpansion.unregister();
        }
        getServer().getServicesManager().unregisterAll(this);
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    private void registerPlaceholders() {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getLogger().info("PlaceholderAPI not found. Placeholders are disabled.");
            return;
        }

        this.placeholderExpansion = new OceanPlaytimeExpansion(this);
        if (placeholderExpansion.register()) {
            getLogger().info("Registered PlaceholderAPI placeholders.");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        playtimeManager.handleJoin(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        playtimeManager.handleQuit(event.getPlayer());
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public PlaytimeManager getPlaytimeManager() {
        return playtimeManager;
    }

    public OceanPlaytimeApi getPlaytimeApi() {
        return playtimeApi;
    }

    public PluginSettings getSettings() {
        return settings;
    }
}
