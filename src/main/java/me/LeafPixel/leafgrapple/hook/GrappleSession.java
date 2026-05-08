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

    /**
     * 钩爪真实命中点，一般是 rayTraceBlocks 返回的 hitPosition。
     */
    private Location anchor;

    /**
     * 钩爪命中的方块位置。
     *
     * 这个字段用于解决玩家被拉向方块侧面导致卡边的问题。
     * 如果只用 anchor.getBlock()，在命中方块边界时可能取到相邻空气方块。
     */
    private Location anchorBlockLocation;

    /**
     * 实际拉扯目标点。
     *
     * 它不一定等于 anchor。
     * 例如钩中方块侧面时，pullTarget 会被修正到方块上表面附近。
     */
    private Location pullTarget;

    /**
     * 开始拉扯时，玩家到 pullTarget 的初始距离。
     * 用于计算拉扯进度和抛物线强度。
     */
    private double initialPullDistance;

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

    public Location anchorBlockLocation() {
        return anchorBlockLocation;
    }

    public void setAnchorBlockLocation(Location anchorBlockLocation) {
        this.anchorBlockLocation = anchorBlockLocation;
    }

    public Location pullTarget() {
        return pullTarget;
    }

    public void setPullTarget(Location pullTarget) {
        this.pullTarget = pullTarget;
    }

    public double initialPullDistance() {
        return initialPullDistance;
    }

    public void setInitialPullDistance(double initialPullDistance) {
        this.initialPullDistance = initialPullDistance;
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
