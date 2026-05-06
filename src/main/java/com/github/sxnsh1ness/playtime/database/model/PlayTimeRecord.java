package com.github.sxnsh1ness.playtime.database.model;

import java.util.UUID;

public record PlayTimeRecord(UUID uuid, String name, long firstJoin, long lastSeen, long totalMs) {
}
