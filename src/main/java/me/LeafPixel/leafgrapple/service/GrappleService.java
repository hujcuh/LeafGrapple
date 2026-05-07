package me.LeafPixel.leafgrapple.service;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.LeafPixel.leafgrapple.hook.GrappleSession;
import me.LeafPixel.leafgrapple.hook.HookItemService;
import me.LeafPixel.leafgrapple.hook.HookTier;
import net.kyori.adventure.text.Component;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class GrappleService {

    private final Plugin plugin;
    private final CooldownService cooldownService;
    private final HookItemService hookItemService;
    private final Map<UUID, GrappleSession> sessions = new ConcurrentHashMap<>();

    public GrappleService(Plugin plugin, CooldownService cooldownService, HookItemService hookItemService) {
        this.plugin = plugin;
        this.cooldownService = cooldownService;
        this.hookItemService = hookItemService;
    }

    public GrappleSession getSession(Player player) {
        return sessions.get(player.getUniqueId());
    }

    public boolean hasSession(Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    public void fire(Player player, HookTier tier) {
        clearSession(player.getUniqueId());

        Location start = player.getEyeLocation().clone();
        Vector direction = start.getDirection().normalize();

        GrappleSession session = new GrappleSession(
                player.getUniqueId(),
                tier,
                start,
                direction
        );

        ItemDisplay display = spawnHookDisplay(player, start, tier);
        session.setHookDisplay(display);

        sessions.put(player.getUniqueId(), session);

        ScheduledTask task = player.getScheduler().runAtFixedRate(
                plugin,
                scheduledTask -> tickFlight(player, scheduledTask),
                () -> clearSession(player.getUniqueId()),
                1L,
                1L
        );

        session.setFlightTask(task);

        playConfiguredSound(player, "sounds.launch");
        debug(player, "[IDLE -> FLYING] 发射钩爪");
    }

    private void tickFlight(Player player, ScheduledTask task) {
        GrappleSession session = getSession(player);

        if (session == null) {
            task.cancel();
            return;
        }

        if (session.hooked() || session.pulling()) {
            task.cancel();
            return;
        }

        if (!player.isOnline() || player.isDead() || !player.isValid()) {
            clearSession(player.getUniqueId());
            task.cancel();
            return;
        }

        Location current = session.tipLocation();
        Vector direction = session.direction();

        if (current == null || direction == null || current.getWorld() == null) {
            debug(player, "[FLYING -> IDLE] 飞行数据无效");
            clearSession(player.getUniqueId());
            task.cancel();
            return;
        }

        session.incrementFlyTicks();

        int maxLifetime = plugin.getConfig().getInt("settings.max-session-lifetime-ticks", 120);
        if (session.flyTicks() > maxLifetime) {
            debug(player, "[FLYING -> IDLE] 飞行超时");
            clearSession(player.getUniqueId());
            task.cancel();
            return;
        }

        HookTier tier = session.tier();

        double step = Math.max(0.2, tier.launchSpeed());
        World world = current.getWorld();

        RayTraceResult result = world.rayTraceBlocks(
                current,
                direction,
                step,
                FluidCollisionMode.NEVER,
                true
        );

        if (result != null && result.getHitBlock() != null && result.getHitPosition() != null) {
            Location anchor = result.getHitPosition().toLocation(world);
            markHooked(player, anchor);
            task.cancel();
            return;
        }

        Location next = current.clone().add(direction.clone().multiply(step));

        session.setTipLocation(next);
        session.addTravelled(step);

        updateHookDisplay(session, next);
        spawnFlightParticles(player, next);

        if (session.travelled() > tier.maxDistance()) {
            debug(player, "[FLYING -> IDLE] 超出距离");
            clearSession(player.getUniqueId());
            task.cancel();
        }
    }

    private void markHooked(Player player, Location anchor) {
        GrappleSession session = getSession(player);
        if (session == null || session.hooked() || session.pulling()) {
            return;
        }

        session.setHooked(true);
        session.setAnchor(anchor.clone());

        ScheduledTask flightTask = session.flightTask();
        if (flightTask != null) {
            flightTask.cancel();
            session.setFlightTask(null);
        }

        updateHookDisplay(session, anchor);

        playConfiguredSound(player, "sounds.hook-block");
        debug(player, "[FLYING -> HOOKED] 钩爪已抓牢，右键拉回，左键取消");
    }

    public void startPull(Player player, Location anchor) {
        GrappleSession session = getSession(player);
        if (session == null || session.pulling()) {
            return;
        }

        session.setPulling(true);
        session.setHooked(false);
        session.setAnchor(anchor.clone());
        session.setCurrentPullSpeed(0.0);

        ScheduledTask flightTask = session.flightTask();
        if (flightTask != null) {
            flightTask.cancel();
            session.setFlightTask(null);
        }

        removeHookDisplay(session);

        playConfiguredSound(player, "sounds.pulling");
        debug(player, "[HOOKED -> PULLING] 开始拉回");

        ScheduledTask task = player.getScheduler().runAtFixedRate(
                plugin,
                scheduledTask -> tickPull(player, scheduledTask),
                () -> clearSession(player.getUniqueId()),
                1L,
                1L
        );

        session.setPullTask(task);
    }

    private void tickPull(Player player, ScheduledTask task) {
        GrappleSession session = getSession(player);

        if (session == null) {
            task.cancel();
            return;
        }

        if (!player.isOnline() || player.isDead() || !player.isValid()) {
            clearSession(player.getUniqueId());
            task.cancel();
            return;
        }

        Location anchor = session.anchor();
        if (anchor == null || anchor.getWorld() == null || !anchor.getWorld().equals(player.getWorld())) {
            debug(player, "[PULLING -> IDLE] 钩点无效");
            clearSession(player.getUniqueId());
            task.cancel();
            return;
        }

        int maxPullTicks = plugin.getConfig().getInt("settings.max-pull-ticks", 100);
        double arriveDistance = plugin.getConfig().getDouble("settings.arrive-distance", 0.9);

        session.incrementPullTicks();

        if (session.pullTicks() > maxPullTicks) {
            debug(player, "[PULLING -> IDLE] 拉扯超时");
            stopPlayerAfterPull(player);
            finishWithCooldown(player);
            task.cancel();
            return;
        }

        Location playerLocation = player.getLocation().add(0.0, 0.35, 0.0);
        Vector toAnchor = anchor.toVector().subtract(playerLocation.toVector());
        double distance = toAnchor.length();

        if (distance <= arriveDistance) {
            playConfiguredSound(player, "sounds.finish");
            debug(player, "[PULLING -> IDLE] 到达钩点");
            stopPlayerAfterPull(player);
            finishWithCooldown(player);
            task.cancel();
            return;
        }

        HookTier tier = session.tier();

        double nextSpeed = Math.min(
                tier.maxPullSpeed(),
                session.currentPullSpeed() + tier.pullAcceleration()
        );

        nextSpeed = Math.min(nextSpeed, Math.max(0.12, distance * 0.35));
        session.setCurrentPullSpeed(nextSpeed);

        Vector velocity = toAnchor.normalize().multiply(nextSpeed);

        if (velocity.getY() > 1.2) {
            velocity.setY(1.2);
        }

        if (velocity.getY() < -0.8) {
            velocity.setY(-0.8);
        }

        if (distance < 3.0 && velocity.getY() > 0.25) {
            velocity.setY(0.25);
        }

        velocity.setX(clamp(velocity.getX(), -3.0, 3.0));
        velocity.setY(clamp(velocity.getY(), -0.8, 1.2));
        velocity.setZ(clamp(velocity.getZ(), -3.0, 3.0));

        player.setVelocity(velocity);

        if (plugin.getConfig().getBoolean("settings.reduce-fall-damage", true)) {
            player.setFallDistance(0.0F);
        }

        int soundInterval = plugin.getConfig().getInt("sounds.pulling.interval-ticks", 3);
        if (soundInterval > 0 && session.pullTicks() % soundInterval == 0) {
            playConfiguredSound(player, "sounds.pulling");
        }
    }

    public void cancelByPlayer(Player player) {
        debug(player, "[ANY -> IDLE] 玩家取消");
        clearSession(player.getUniqueId());
    }

    public void finishWithCooldown(Player player) {
        GrappleSession session = getSession(player);
        if (session == null) {
            return;
        }

        HookTier tier = session.tier();

        clearSession(player.getUniqueId());

        hookItemService.damageHookItem(
                player,
                tier,
                tier.durabilityCostOnFinish()
        );

        int cooldownTicks = tier.cooldownTicks();
        if (cooldownTicks > 0) {
            cooldownService.start(player.getUniqueId(), "launch", cooldownTicks);
            hookItemService.applyHookCooldown(player, tier, cooldownTicks);
        }
    }

    public void clearSession(UUID playerId) {
        GrappleSession session = sessions.remove(playerId);
        if (session == null) {
            return;
        }

        ScheduledTask flightTask = session.flightTask();
        if (flightTask != null) {
            flightTask.cancel();
        }

        ScheduledTask pullTask = session.pullTask();
        if (pullTask != null) {
            pullTask.cancel();
        }

        removeHookDisplay(session);
    }

    public void clearAll() {
        for (UUID playerId : sessions.keySet()) {
            clearSession(playerId);
        }
        sessions.clear();
    }

    private ItemDisplay spawnHookDisplay(Player player, Location location, HookTier tier) {
        ItemStack displayItem = createDisplayItem(tier);
        Location spawnLocation = location.clone();

        return player.getWorld().spawn(spawnLocation, ItemDisplay.class, display -> {
            display.setItemStack(displayItem);
            display.setPersistent(false);
            display.setGlowing(tier.displayGlowing());
            display.setBillboard(Display.Billboard.CENTER);
            display.setTransformation(createDisplayTransformation(tier));
        });
    }

    private ItemStack createDisplayItem(HookTier tier) {
        ItemStack item = new ItemStack(tier.displayMaterial());

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (tier.displayItemModel() != null) {
                meta.setItemModel(tier.displayItemModel());
            }

            if (tier.displayCustomModelData() > 0) {
                meta.setCustomModelData(tier.displayCustomModelData());
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    private Transformation createDisplayTransformation(HookTier tier) {
        Vector3f translation = new Vector3f(
                tier.displayOffsetX(),
                tier.displayOffsetY(),
                tier.displayOffsetZ()
        );

        Vector3f scale = new Vector3f(
                tier.displayScaleX(),
                tier.displayScaleY(),
                tier.displayScaleZ()
        );

        float angleRadians = (float) Math.toRadians(tier.displayRotationAngleDegrees());

        AxisAngle4f leftRotation = new AxisAngle4f(
                angleRadians,
                tier.displayRotationAxisX(),
                tier.displayRotationAxisY(),
                tier.displayRotationAxisZ()
        );

        AxisAngle4f rightRotation = new AxisAngle4f(
                0.0F,
                0.0F,
                1.0F,
                0.0F
        );

        return new Transformation(
                translation,
                leftRotation,
                scale,
                rightRotation
        );
    }

    private void updateHookDisplay(GrappleSession session, Location location) {
        ItemDisplay display = session.hookDisplay();
        if (display == null || !display.isValid()) {
            return;
        }

        Location target = location.clone();

        display.getScheduler().execute(
                plugin,
                () -> {
                    if (display.isValid()) {
                        display.teleport(target);
                    }
                },
                null,
                1L
        );
    }

    private void removeHookDisplay(GrappleSession session) {
        ItemDisplay display = session.hookDisplay();
        if (display == null || !display.isValid()) {
            session.setHookDisplay(null);
            return;
        }

        display.getScheduler().execute(
                plugin,
                () -> {
                    if (display.isValid()) {
                        display.remove();
                    }
                },
                null,
                1L
        );

        session.setHookDisplay(null);
    }

    private void spawnFlightParticles(Player player, Location location) {
        if (location.getWorld() == null) {
            return;
        }

        location.getWorld().spawnParticle(
                Particle.CRIT,
                location,
                2,
                0.02,
                0.02,
                0.02,
                0.0
        );

        if (plugin.getConfig().getBoolean("settings.debug", false) && player.getTicksLived() % 2 == 0) {
            Location eye = player.getEyeLocation();
            Vector toTip = location.toVector().subtract(eye.toVector());
            double length = toTip.length();

            if (length > 0.5) {
                Vector step = toTip.normalize().multiply(0.75);
                Location point = eye.clone();

                for (double d = 0.0; d < length; d += 0.75) {
                    point.add(step);
                    if (point.getWorld() != null) {
                        point.getWorld().spawnParticle(
                                Particle.CRIT,
                                point,
                                1,
                                0.0,
                                0.0,
                                0.0,
                                0.0
                        );
                    }
                }
            }
        }
    }

    private void stopPlayerAfterPull(Player player) {
        Vector velocity = player.getVelocity();

        double x = velocity.getX() * 0.25;
        double z = velocity.getZ() * 0.25;

        player.setVelocity(new Vector(x, 0.0, z));

        if (plugin.getConfig().getBoolean("settings.reduce-fall-damage", true)) {
            player.setFallDistance(0.0F);
        }
    }

    private void playConfiguredSound(Player player, String path) {
        if (!plugin.getConfig().getBoolean(path + ".enabled", true)) {
            return;
        }

        String soundName = plugin.getConfig().getString(path + ".sound", "minecraft:block.chain.hit");
        float volume = (float) plugin.getConfig().getDouble(path + ".volume", 1.0);
        float pitch = (float) plugin.getConfig().getDouble(path + ".pitch", 1.0);

        Sound sound = parseSound(soundName);

        if (sound == null) {
            plugin.getLogger().warning("Invalid sound in config: " + soundName);
            return;
        }

        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    private Sound parseSound(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        String normalized = input.trim().toLowerCase(Locale.ROOT);

        NamespacedKey key = NamespacedKey.fromString(normalized);

        if (key == null && !normalized.contains(":")) {
            String legacyPath = normalized.replace('_', '.');
            key = NamespacedKey.minecraft(legacyPath);
        }

        if (key == null) {
            return null;
        }

        return Registry.SOUNDS.get(key);
    }

    private void debug(Player player, String message) {
        if (plugin.getConfig().getBoolean("settings.debug", false)) {
            player.sendActionBar(Component.text(message));
        }
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
