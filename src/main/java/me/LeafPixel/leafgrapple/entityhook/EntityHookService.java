package me.LeafPixel.leafgrapple.entityhook;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.LeafPixel.leafgrapple.hook.HookItemService;
import me.LeafPixel.leafgrapple.hook.HookTier;
import me.LeafPixel.leafgrapple.service.CooldownService;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class EntityHookService {

    private final Plugin plugin;
    private final HookItemService hookItemService;
    private final CooldownService cooldownService;

    private final Map<UUID, EntityHookSession> sessionsByTarget = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> targetByOwner = new ConcurrentHashMap<>();

    public EntityHookService(
            Plugin plugin,
            HookItemService hookItemService,
            CooldownService cooldownService
    ) {
        this.plugin = plugin;
        this.hookItemService = hookItemService;
        this.cooldownService = cooldownService;
    }

    public boolean hasSessionByOwner(UUID ownerId) {
        return targetByOwner.containsKey(ownerId);
    }

    public boolean canHookTarget(Player owner, LivingEntity target, HookTier tier) {
        if (owner == null || target == null || tier == null) {
            return false;
        }

        EntityHookSettings settings = tier.entityHookSettings();
        if (settings == null || !settings.enabled()) {
            return false;
        }

        if (!target.isValid() || target.isDead()) {
            return false;
        }

        if (target instanceof ArmorStand) {
            return false;
        }

        if (target.getUniqueId().equals(owner.getUniqueId())) {
            return false;
        }

        if (target.getWorld() == null || !target.getWorld().equals(owner.getWorld())) {
            return false;
        }

        double maxHitDistance = settings.maxHitDistance();
        if (maxHitDistance > 0.0 && owner.getEyeLocation().distance(target.getLocation()) > maxHitDistance) {
            return false;
        }

        if (target instanceof Player targetPlayer) {
            return canHookPlayer(owner, targetPlayer, settings);
        }

        return canHookMob(target, settings);
    }

    private boolean canHookPlayer(Player owner, Player target, EntityHookSettings settings) {
        if (!settings.targetPlayers()) {
            return false;
        }

        if (target.getGameMode() == GameMode.CREATIVE || target.getGameMode() == GameMode.SPECTATOR) {
            return false;
        }

        if (target.hasPermission(settings.bypassPermission())) {
            return false;
        }

        World world = target.getWorld();
        if (settings.requireWorldPvp() && world != null && !world.getPVP()) {
            return false;
        }

        return true;
    }

    private boolean canHookMob(LivingEntity target, EntityHookSettings settings) {
        if (settings.targetMobs()) {
            return true;
        }

        if (target instanceof Animals) {
            return settings.targetAnimals();
        }

        if (target instanceof Monster) {
            return settings.targetMonsters();
        }

        return false;
    }

    public boolean startHook(Player owner, LivingEntity target, HookTier tier, Location hitLocation) {
        if (!canHookTarget(owner, target, tier)) {
            return false;
        }

        clearByOwner(owner.getUniqueId());
        clearByTarget(target.getUniqueId());

        /*
         * 视觉锚点：
         * 如果没有精确命中点，就使用目标身体上半部。
         * 如果有命中点，也稍微修正，避免链条从脚底或地面发出。
         */
        Location anchor = createVisualAnchor(target, hitLocation);

        EntityHookSession session = new EntityHookSession(
                owner.getUniqueId(),
                target.getUniqueId(),
                tier,
                anchor
        );

        sessionsByTarget.put(target.getUniqueId(), session);
        targetByOwner.put(owner.getUniqueId(), target.getUniqueId());

        /*
         * 成功开始实体束缚时立即消耗耐久。
         * 与“开始拉回就消耗耐久”的规则保持一致。
         */
        hookItemService.damageHookItem(
                owner,
                tier,
                tier.durabilityCostOnFinish()
        );

        int cooldownTicks = tier.cooldownTicks();
        if (cooldownTicks > 0) {
            cooldownService.start(owner.getUniqueId(), "launch", cooldownTicks);
            hookItemService.applyHookCooldown(owner, tier, cooldownTicks);
        }

        ScheduledTask task = target.getScheduler().runAtFixedRate(
                plugin,
                scheduledTask -> tickHook(session, target, scheduledTask),
                () -> clearByTarget(target.getUniqueId()),
                1L,
                1L
        );

        session.setTask(task);

        owner.sendActionBar(Component.text("钩爪已束缚目标"));
        return true;
    }

    private Location createVisualAnchor(LivingEntity target, Location hitLocation) {
        Location targetLocation = target.getLocation();

        if (hitLocation == null || hitLocation.getWorld() == null) {
            return targetLocation.clone().add(0.0, target.getHeight() * 0.75, 0.0);
        }

        Location anchor = hitLocation.clone();

        /*
         * 如果命中点太低，则抬到目标身体中上部附近。
         * 这样视觉上更像钩住了身体，而不是钩在地面。
         */
        double minY = targetLocation.getY() + target.getHeight() * 0.45;
        double maxY = targetLocation.getY() + target.getHeight() * 0.9;

        if (anchor.getY() < minY) {
            anchor.setY(minY);
        }

        if (anchor.getY() > maxY) {
            anchor.setY(maxY);
        }

        return anchor;
    }

    private void tickHook(EntityHookSession session, LivingEntity target, ScheduledTask task) {
        if (session == null || target == null) {
            task.cancel();
            return;
        }

        EntityHookSettings settings = session.tier().entityHookSettings();
        if (settings == null || !settings.enabled()) {
            stopSession(session, task);
            return;
        }

        if (!target.isValid() || target.isDead()) {
            stopSession(session, task);
            return;
        }

        Player owner = Bukkit.getPlayer(session.ownerId());
        if (owner == null || !owner.isOnline() || owner.isDead() || !owner.isValid()) {
            stopSession(session, task);
            return;
        }

        Location anchor = session.anchorLocation();
        Location targetLocation = target.getLocation();

        if (
                anchor == null
                        || anchor.getWorld() == null
                        || targetLocation.getWorld() == null
                        || !anchor.getWorld().equals(targetLocation.getWorld())
        ) {
            stopSession(session, task);
            return;
        }

        session.incrementTicksLived();

        ensureHookDisplay(target, session, settings);

        if (session.ticksLived() > settings.durationTicks()) {
            stopSession(session, task);
            return;
        }

        double distanceToAnchor = targetLocation.distance(anchor);

        if (distanceToAnchor > settings.breakDistance()) {
            stopSession(session, task);
            return;
        }

        applySoftBind(target, anchor, settings, distanceToAnchor);

        if (
                settings.damageEnabled()
                        && settings.damageAmount() > 0.0
                        && session.ticksLived() % settings.damageIntervalTicks() == 0
        ) {
            /*
             * 标准实体伤害：
             * 走护甲、抗性、EntityDamageByEntityEvent 和其他插件监听。
             */
            target.damage(settings.damageAmount(), owner);
        }

        if (
                settings.particlesEnabled()
                        && session.ticksLived() % settings.particleIntervalTicks() == 0
        ) {
            spawnBindParticles(target, anchor, settings);
        }
    }

    private void ensureHookDisplay(
            LivingEntity target,
            EntityHookSession session,
            EntityHookSettings settings
    ) {
        if (!settings.hookDisplayEnabled()) {
            return;
        }

        ItemDisplay existing = session.hookDisplay();
        if (existing != null && existing.isValid()) {
            return;
        }

        Location anchor = session.anchorLocation();
        if (anchor == null || anchor.getWorld() == null) {
            return;
        }

        if (target.getWorld() == null || !target.getWorld().equals(anchor.getWorld())) {
            return;
        }

        ItemStack item = new ItemStack(settings.hookDisplayMaterial());

        ItemDisplay display = anchor.getWorld().spawn(anchor, ItemDisplay.class, spawned -> {
            spawned.setItemStack(item);
            spawned.setPersistent(false);
            spawned.setGlowing(settings.hookDisplayGlowing());
            spawned.setBillboard(Display.Billboard.CENTER);
            spawned.setTransformation(createHookDisplayTransformation(settings));
        });

        session.setHookDisplay(display);
    }

    private Transformation createHookDisplayTransformation(EntityHookSettings settings) {
        float scaleValue = settings.hookDisplayScale();

        Vector3f translation = new Vector3f(
                0.0F,
                0.0F,
                0.0F
        );

        AxisAngle4f leftRotation = new AxisAngle4f(
                0.0F,
                0.0F,
                1.0F,
                0.0F
        );

        Vector3f scale = new Vector3f(
                scaleValue,
                scaleValue,
                scaleValue
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

    private void applySoftBind(
            LivingEntity target,
            Location anchor,
            EntityHookSettings settings,
            double distanceToAnchor
    ) {
        double radius = settings.bindRadius();

        if (distanceToAnchor <= radius) {
            return;
        }

        Vector toAnchor = anchor.toVector().subtract(target.getLocation().toVector());

        if (toAnchor.lengthSquared() <= 0.0001) {
            return;
        }

        double excess = distanceToAnchor - radius;
        double pullSpeed = Math.min(settings.maxPullSpeed(), excess * settings.pullStrength());

        Vector pullVelocity = toAnchor.normalize().multiply(pullSpeed);
        Vector currentVelocity = target.getVelocity().multiply(settings.velocityDamping());

        Vector finalVelocity = currentVelocity.add(pullVelocity);

        finalVelocity.setX(clamp(finalVelocity.getX(), -settings.maxPullSpeed(), settings.maxPullSpeed()));
        finalVelocity.setY(clamp(finalVelocity.getY(), -settings.maxPullSpeed(), settings.maxPullSpeed()));
        finalVelocity.setZ(clamp(finalVelocity.getZ(), -settings.maxPullSpeed(), settings.maxPullSpeed()));

        target.setVelocity(finalVelocity);
    }

    private void spawnBindParticles(
            LivingEntity target,
            Location anchor,
            EntityHookSettings settings
    ) {
        Location targetLocation = target.getLocation().add(0.0, target.getHeight() * 0.55, 0.0);

        if (targetLocation.getWorld() == null || anchor.getWorld() == null) {
            return;
        }

        if (!targetLocation.getWorld().equals(anchor.getWorld())) {
            return;
        }

        /*
         * 目标身上粒子。
         */
        targetLocation.getWorld().spawnParticle(
                settings.particle(),
                targetLocation,
                settings.particleCount(),
                0.25,
                0.35,
                0.25,
                0.0
        );

        /*
         * 锚点到目标之间的粒子链。
         */
        Vector line = targetLocation.toVector().subtract(anchor.toVector());
        double length = line.length();

        if (length <= 0.1) {
            return;
        }

        double stepSize = settings.particleStep();
        Vector step = line.normalize().multiply(stepSize);
        Location point = anchor.clone();

        int maxPoints = Math.min(
                settings.particleMaxPoints(),
                (int) Math.ceil(length / stepSize)
        );

        for (int i = 0; i < maxPoints; i++) {
            point.add(step);

            if (point.getWorld() != null) {
                point.getWorld().spawnParticle(
                        settings.particle(),
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

    private void stopSession(EntityHookSession session, ScheduledTask task) {
        if (task != null) {
            task.cancel();
        }

        clearByTarget(session.targetId());
    }

    public void clearByOwner(UUID ownerId) {
        if (ownerId == null) {
            return;
        }

        UUID targetId = targetByOwner.remove(ownerId);
        if (targetId == null) {
            return;
        }

        clearByTarget(targetId);
    }

    public void clearByTarget(UUID targetId) {
        if (targetId == null) {
            return;
        }

        EntityHookSession session = sessionsByTarget.remove(targetId);
        if (session == null) {
            return;
        }

        targetByOwner.remove(session.ownerId());

        ScheduledTask task = session.task();
        if (task != null) {
            task.cancel();
        }

        removeHookDisplay(session);
    }

    public void clearAll() {
        for (UUID targetId : sessionsByTarget.keySet()) {
            clearByTarget(targetId);
        }

        sessionsByTarget.clear();
        targetByOwner.clear();
    }

    private void removeHookDisplay(EntityHookSession session) {
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

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
