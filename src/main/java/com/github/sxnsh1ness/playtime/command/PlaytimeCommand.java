package com.github.sxnsh1ness.playtime.command;

import com.github.sxnsh1ness.playtime.PlayTimePlugin;
import com.github.sxnsh1ness.playtime.database.model.PlayTimeRecord;
import com.github.sxnsh1ness.playtime.manager.PlayTimeManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class PlayTimeCommand implements CommandExecutor, TabCompleter {

    private final PlayTimePlugin plugin;
    private final PlayTimeManager playtimeManager;

    public PlayTimeCommand(PlayTimePlugin plugin, PlayTimeManager playtimeManager) {
        this.plugin = plugin;
        this.playtimeManager = playtimeManager;
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getSettings().consoleUsage("/" + label + " <player>"));
                return true;
            }

            sendPlayTime(sender, player.getUniqueId(), player.getName(), true);
            return true;
        }

        if (args.length == 1) {
            Player onlinePlayer = Bukkit.getPlayerExact(args[0]);
            if (onlinePlayer != null) {
                sendPlayTime(sender, onlinePlayer.getUniqueId(), onlinePlayer.getName(), false);
                return true;
            }

            findAndSendOfflinePlaytime(sender, args[0]);
            return true;
        }

        sender.sendMessage(plugin.getSettings().commandUsage("/" + label + " [player]"));
        return true;
    }

    private void sendPlayTime(CommandSender sender, UUID uuid, String name, boolean self) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            long playtime = playtimeManager.getPlayTime(uuid);
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (self) {
                    sender.sendMessage(plugin.getSettings().selfPlayTime(playtimeManager.formatTime(playtime)));
                    return;
                }

                sender.sendMessage(plugin.getSettings().otherPlayTime(name, playtimeManager.formatTime(playtime)));
            });
        });
    }

    private void findAndSendOfflinePlaytime(CommandSender sender, String query) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayTimeRecord record = plugin.getDatabaseManager().getByName(query);
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (record == null) {
                    sender.sendMessage(plugin.getSettings().playerNotFound(query));
                    return;
                }

                sender.sendMessage(plugin.getSettings().otherPlayTime(record.name(), playtimeManager.formatTime(record.totalMs())));
            });
        });
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String alias, String[] args) {
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
