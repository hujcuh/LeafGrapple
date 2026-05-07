package me.LeafPixel.leafgrapple.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CooldownService {

    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();

    public boolean isCoolingDown(UUID playerId, String channel) {
        return getRemainingMillis(playerId, channel) > 0L;
    }

    public void start(UUID playerId, String channel, int cooldownTicks) {
        if (cooldownTicks <= 0) {
            clear(playerId, channel);
            return;
        }

        long now = System.currentTimeMillis();
        long durationMillis = cooldownTicks * 50L;
        cooldowns.put(key(playerId, channel), now + durationMillis);
    }

    public long getRemainingMillis(UUID playerId, String channel) {
        long now = System.currentTimeMillis();
        long until = cooldowns.getOrDefault(key(playerId, channel), 0L);

        long remaining = until - now;
        if (remaining <= 0L) {
            cooldowns.remove(key(playerId, channel));
            return 0L;
        }

        return remaining;
    }

    public int getRemainingTicks(UUID playerId, String channel) {
        long remainingMillis = getRemainingMillis(playerId, channel);
        return (int) Math.ceil(remainingMillis / 50.0);
    }

    public void clear(UUID playerId, String channel) {
        cooldowns.remove(key(playerId, channel));
    }

    public void clearAll(UUID playerId) {
        cooldowns.remove(key(playerId, "launch"));
        cooldowns.remove(key(playerId, "recast"));
    }

    public void clearAll() {
        cooldowns.clear();
    }

    private String key(UUID playerId, String channel) {
        return playerId + ":" + channel;
    }
}
