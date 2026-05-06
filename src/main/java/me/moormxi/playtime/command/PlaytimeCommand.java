package me.moormxi.playtime.command;

import me.moormxi.playtime.OceanPlaytimePlugin;
import me.moormxi.playtime.database.DatabaseManager;
import me.moormxi.playtime.manager.PlaytimeManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class PlaytimeCommand implements CommandExecutor, TabCompleter {

    private final OceanPlaytimePlugin plugin;
    private final PlaytimeManager playtimeManager;

    public PlaytimeCommand(OceanPlaytimePlugin plugin, PlaytimeManager playtimeManager) {
        this.plugin = plugin;
        this.playtimeManager = playtimeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getSettings().consoleUsage("/" + label + " <player>"));
                return true;
            }

            sendPlaytime(sender, player.getUniqueId(), player.getName(), true);
            return true;
        }

        if (args.length == 1) {
            Player onlinePlayer = Bukkit.getPlayerExact(args[0]);
            if (onlinePlayer != null) {
                sendPlaytime(sender, onlinePlayer.getUniqueId(), onlinePlayer.getName(), false);
                return true;
            }

            findAndSendOfflinePlaytime(sender, args[0]);
            return true;
        }

        sender.sendMessage(plugin.getSettings().commandUsage("/" + label + " [player]"));
        return true;
    }

    private void sendPlaytime(CommandSender sender, UUID uuid, String name, boolean self) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            long playtime = playtimeManager.getPlaytime(uuid);
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (self) {
                    sender.sendMessage(plugin.getSettings().selfPlaytime(playtimeManager.formatTime(playtime)));
                    return;
                }

                sender.sendMessage(plugin.getSettings().otherPlaytime(name, playtimeManager.formatTime(playtime)));
            });
        });
    }

    private void findAndSendOfflinePlaytime(CommandSender sender, String query) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            DatabaseManager.PlaytimeRecord record = plugin.getDatabaseManager().getByName(query);
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (record == null) {
                    sender.sendMessage(plugin.getSettings().playerNotFound(query));
                    return;
                }

                sender.sendMessage(plugin.getSettings().otherPlaytime(record.name(), playtimeManager.formatTime(record.totalMs())));
            });
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }

        String prefix = args[0].toLowerCase(Locale.ROOT);
        List<String> completions = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            String name = player.getName();
            if (name.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                completions.add(name);
            }
        }
        Collections.sort(completions);
        return completions;
    }
}
