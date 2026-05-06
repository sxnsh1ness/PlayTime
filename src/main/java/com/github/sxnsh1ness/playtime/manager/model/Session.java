package com.github.sxnsh1ness.playtime.manager.model;

import java.util.concurrent.atomic.AtomicLong;

public final class Session {

    private final long joinedAt;
    private final AtomicLong savedUntil;

    public Session(long joinedAt) {
        this.joinedAt = joinedAt;
        this.savedUntil = new AtomicLong(joinedAt);
    }

    public long joinedAt() {
        return joinedAt;
    }

    public long savedUntil() {
        return savedUntil.get();
    }

    public long getAndSetSavedUntil(long savedUntil) {
        return this.savedUntil.getAndSet(savedUntil);
    }
}
