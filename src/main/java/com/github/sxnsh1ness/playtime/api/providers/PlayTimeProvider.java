package com.github.sxnsh1ness.playtime.api.providers;

import com.github.sxnsh1ness.playtime.api.PlayTimeAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Optional;

public final class PlayTimeProvider {

    private PlayTimeProvider() {
    }

    public static Optional<PlayTimeAPI> get() {
        RegisteredServiceProvider<PlayTimeAPI> provider = Bukkit.getServicesManager().getRegistration(PlayTimeAPI.class);
        if (provider == null) {
            return Optional.empty();
        }
        return Optional.of(provider.getProvider());
    }
}
