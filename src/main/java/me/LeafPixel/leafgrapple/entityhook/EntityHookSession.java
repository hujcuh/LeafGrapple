package me.LeafPixel.leafgrapple.entityhook;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.LeafPixel.leafgrapple.hook.HookTier;
import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;

import java.util.UUID;

public final class EntityHookSession {

    private final UUID ownerId;
    private final UUID targetId;
    private final HookTier tier;
    private final Location anchorLocation;

    private ScheduledTask task;
    private ItemDisplay hookDisplay;
    private int ticksLived;

    public EntityHookSession(
            UUID ownerId,
            UUID targetId,
            HookTier tier,
            Location anchorLocation
    ) {
        this.ownerId = ownerId;
        this.targetId = targetId;
        this.tier = tier;
        this.anchorLocation = anchorLocation;
    }

    public UUID ownerId() {
        return ownerId;
    }

    public UUID targetId() {
        return targetId;
    }

    public HookTier tier() {
        return tier;
    }

    public Location anchorLocation() {
        return anchorLocation;
    }

    public ScheduledTask task() {
        return task;
    }

    public void setTask(ScheduledTask task) {
        this.task = task;
    }

    public ItemDisplay hookDisplay() {
        return hookDisplay;
    }

    public void setHookDisplay(ItemDisplay hookDisplay) {
        this.hookDisplay = hookDisplay;
    }

    public int ticksLived() {
        return ticksLived;
    }

    public void incrementTicksLived() {
        this.ticksLived++;
    }
}
