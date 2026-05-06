package me.moormxi.playtime.api;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Optional;

public final class OceanPlaytimeProvider {

    private OceanPlaytimeProvider() {
    }

    public static Optional<OceanPlaytimeApi> get() {
        RegisteredServiceProvider<OceanPlaytimeApi> provider = Bukkit.getServicesManager().getRegistration(OceanPlaytimeApi.class);
        if (provider == null) {
            return Optional.empty();
        }
        return Optional.of(provider.getProvider());
    }
}
