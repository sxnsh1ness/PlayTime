package com.github.sxnsh1ness.playtime.manager.model;

import java.util.UUID;

public record PlayTimeEntry(UUID uuid, String name, long time) {
}
