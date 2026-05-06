package com.github.sxnsh1ness.playtime;

import com.github.sxnsh1ness.playtime.api.PlayTimeAPI;
import com.github.sxnsh1ness.playtime.api.SimplePlayTimeAPI;
import com.github.sxnsh1ness.playtime.command.PlayTimeCommand;
import com.github.sxnsh1ness.playtime.config.PluginSettings;
import com.github.sxnsh1ness.playtime.database.DatabaseManager;
import com.github.sxnsh1ness.playtime.manager.PlayTimeManager;
import com.github.sxnsh1ness.playtime.placeholder.PlayTimeExpansion;
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

public final class PlayTimePlugin extends JavaPlugin implements Listener {

    private DatabaseManager databaseManager;
    private PlayTimeManager playtimeManager;
    private PlayTimeAPI playtimeApi;
    private PlayTimeExpansion placeholderExpansion;
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

        this.playtimeManager = new PlayTimeManager(this);
        this.playtimeApi = new SimplePlayTimeAPI(playtimeManager, databaseManager);

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getServicesManager().register(PlayTimeAPI.class, playtimeApi, this, ServicePriority.Normal);
        registerPlaceholders();

        PlayTimeCommand command = new PlayTimeCommand(this, playtimeManager);
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

        getLogger().info("PlayTime enabled.");
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

        this.placeholderExpansion = new PlayTimeExpansion(this);
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

    public PlayTimeManager getPlayTimeManager() {
        return playtimeManager;
    }

    public PlayTimeAPI getPlayTimeAPI() {
        return playtimeApi;
    }

    public PluginSettings getSettings() {
        return settings;
    }
}
