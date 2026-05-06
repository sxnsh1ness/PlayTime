package com.github.sxnsh1ness.playtime.api;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Optional;

public final class PlaytimeProvider {

    private PlaytimeProvider() {
    }

    public static Optional<PlaytimeApi> get() {
        RegisteredServiceProvider<PlaytimeApi> provider = Bukkit.getServicesManager().getRegistration(PlaytimeApi.class);
        if (provider == null) {
            return Optional.empty();
        }
        return Optional.of(provider.getProvider());
    }
}
