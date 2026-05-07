package me.LeafPixel.leafgrapple.hook;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.util.Vector;

import java.util.UUID;

public final class GrappleSession {

    private final UUID playerId;
    private final HookTier tier;

    private Location tipLocation;
    private Vector direction;
    private double travelled;

    private ItemDisplay hookDisplay;

    private boolean hooked;
    private boolean pulling;

    private Location anchor;

    private int flyTicks;
    private int pullTicks;
    private double currentPullSpeed;

    private ScheduledTask flightTask;
    private ScheduledTask pullTask;

    public GrappleSession(UUID playerId, HookTier tier, Location tipLocation, Vector direction) {
        this.playerId = playerId;
        this.tier = tier;
        this.tipLocation = tipLocation;
        this.direction = direction;
    }

    public UUID playerId() {
        return playerId;
    }

    public HookTier tier() {
        return tier;
    }

    public Location tipLocation() {
        return tipLocation;
    }

    public void setTipLocation(Location tipLocation) {
        this.tipLocation = tipLocation;
    }

    public Vector direction() {
        return direction;
    }

    public void setDirection(Vector direction) {
        this.direction = direction;
    }

    public double travelled() {
        return travelled;
    }

    public void addTravelled(double amount) {
        this.travelled += amount;
    }

    public ItemDisplay hookDisplay() {
        return hookDisplay;
    }

    public void setHookDisplay(ItemDisplay hookDisplay) {
        this.hookDisplay = hookDisplay;
    }

    public boolean hooked() {
        return hooked;
    }

    public void setHooked(boolean hooked) {
        this.hooked = hooked;
    }

    public boolean pulling() {
        return pulling;
    }

    public void setPulling(boolean pulling) {
        this.pulling = pulling;
    }

    public Location anchor() {
        return anchor;
    }

    public void setAnchor(Location anchor) {
        this.anchor = anchor;
    }

    public int flyTicks() {
        return flyTicks;
    }

    public void incrementFlyTicks() {
        this.flyTicks++;
    }

    public int pullTicks() {
        return pullTicks;
    }

    public void incrementPullTicks() {
        this.pullTicks++;
    }

    public double currentPullSpeed() {
        return currentPullSpeed;
    }

    public void setCurrentPullSpeed(double currentPullSpeed) {
        this.currentPullSpeed = currentPullSpeed;
    }

    public ScheduledTask flightTask() {
        return flightTask;
    }

    public void setFlightTask(ScheduledTask flightTask) {
        this.flightTask = flightTask;
    }

    public ScheduledTask pullTask() {
        return pullTask;
    }

    public void setPullTask(ScheduledTask pullTask) {
        this.pullTask = pullTask;
    }
}