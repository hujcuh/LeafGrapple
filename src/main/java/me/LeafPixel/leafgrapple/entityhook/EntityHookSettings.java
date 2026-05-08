package me.LeafPixel.leafgrapple.entityhook;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Locale;

public final class EntityHookSettings {

    private final boolean enabled;

    private final boolean targetPlayers;
    private final boolean targetMobs;
    private final boolean targetAnimals;
    private final boolean targetMonsters;
    private final boolean requireWorldPvp;
    private final String bypassPermission;

    private final int durationTicks;
    private final double maxHitDistance;
    private final double breakDistance;

    private final double bindRadius;
    private final double pullStrength;
    private final double maxPullSpeed;
    private final double velocityDamping;

    private final boolean damageEnabled;
    private final double damageAmount;
    private final int damageIntervalTicks;

    private final boolean particlesEnabled;
    private final int particleIntervalTicks;
    private final Particle particle;
    private final int particleCount;
    private final double particleStep;
    private final int particleMaxPoints;

    private final boolean hookDisplayEnabled;
    private final Material hookDisplayMaterial;
    private final boolean hookDisplayGlowing;
    private final float hookDisplayScale;

    private EntityHookSettings(
            boolean enabled,
            boolean targetPlayers,
            boolean targetMobs,
            boolean targetAnimals,
            boolean targetMonsters,
            boolean requireWorldPvp,
            String bypassPermission,
            int durationTicks,
            double maxHitDistance,
            double breakDistance,
            double bindRadius,
            double pullStrength,
            double maxPullSpeed,
            double velocityDamping,
            boolean damageEnabled,
            double damageAmount,
            int damageIntervalTicks,
            boolean particlesEnabled,
            int particleIntervalTicks,
            Particle particle,
            int particleCount,
            double particleStep,
            int particleMaxPoints,
            boolean hookDisplayEnabled,
            Material hookDisplayMaterial,
            boolean hookDisplayGlowing,
            float hookDisplayScale
    ) {
        this.enabled = enabled;
        this.targetPlayers = targetPlayers;
        this.targetMobs = targetMobs;
        this.targetAnimals = targetAnimals;
        this.targetMonsters = targetMonsters;
        this.requireWorldPvp = requireWorldPvp;
        this.bypassPermission = bypassPermission;
        this.durationTicks = durationTicks;
        this.maxHitDistance = maxHitDistance;
        this.breakDistance = breakDistance;
        this.bindRadius = bindRadius;
        this.pullStrength = pullStrength;
        this.maxPullSpeed = maxPullSpeed;
        this.velocityDamping = velocityDamping;
        this.damageEnabled = damageEnabled;
        this.damageAmount = damageAmount;
        this.damageIntervalTicks = damageIntervalTicks;
        this.particlesEnabled = particlesEnabled;
        this.particleIntervalTicks = particleIntervalTicks;
        this.particle = particle;
        this.particleCount = particleCount;
        this.particleStep = particleStep;
        this.particleMaxPoints = particleMaxPoints;
        this.hookDisplayEnabled = hookDisplayEnabled;
        this.hookDisplayMaterial = hookDisplayMaterial;
        this.hookDisplayGlowing = hookDisplayGlowing;
        this.hookDisplayScale = hookDisplayScale;
    }

    public static EntityHookSettings disabled() {
        return new EntityHookSettings(
                false,
                false,
                false,
                false,
                false,
                true,
                "leafgrapple.entityhook.bypass",
                0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                1.0,
                false,
                0.0,
                20,
                false,
                2,
                Particle.CRIT,
                1,
                0.35,
                48,
                false,
                Material.CHAIN,
                true,
                0.45F
        );
    }

    public static EntityHookSettings fromConfig(ConfigurationSection section, double fallbackMaxDistance) {
        if (section == null || !section.getBoolean("enabled", false)) {
            return disabled();
        }

        ConfigurationSection targetSection = section.getConfigurationSection("target");
        boolean targetPlayers = targetSection != null && targetSection.getBoolean("players", false);
        boolean targetMobs = targetSection == null || targetSection.getBoolean("mobs", true);
        boolean targetAnimals = targetSection == null || targetSection.getBoolean("animals", true);
        boolean targetMonsters = targetSection == null || targetSection.getBoolean("monsters", true);

        ConfigurationSection pvpSection = section.getConfigurationSection("pvp");
        boolean requireWorldPvp = pvpSection == null || pvpSection.getBoolean("require-world-pvp", true);
        String bypassPermission = pvpSection == null
                ? "leafgrapple.entityhook.bypass"
                : pvpSection.getString("bypass-permission", "leafgrapple.entityhook.bypass");

        int durationTicks = clampInt(section.getInt("duration-ticks", 100), 1, 20 * 60);
        double maxHitDistance = clampDouble(section.getDouble("max-hit-distance", fallbackMaxDistance), 1.0, 128.0);
        double breakDistance = clampDouble(section.getDouble("break-distance", 12.0), 1.0, 128.0);

        ConfigurationSection bindSection = section.getConfigurationSection("bind");
        double bindRadius = bindSection == null
                ? 2.5
                : bindSection.getDouble("radius", 2.5);
        double pullStrength = bindSection == null
                ? 0.18
                : bindSection.getDouble("pull-strength", 0.18);
        double maxPullSpeed = bindSection == null
                ? 0.55
                : bindSection.getDouble("max-pull-speed", 0.55);
        double velocityDamping = bindSection == null
                ? 0.65
                : bindSection.getDouble("velocity-damping", 0.65);

        bindRadius = clampDouble(bindRadius, 0.25, 16.0);
        pullStrength = clampDouble(pullStrength, 0.01, 3.0);
        maxPullSpeed = clampDouble(maxPullSpeed, 0.05, 3.0);
        velocityDamping = clampDouble(velocityDamping, 0.0, 1.0);

        ConfigurationSection damageSection = section.getConfigurationSection("damage");
        boolean damageEnabled = damageSection != null && damageSection.getBoolean("enabled", false);
        double damageAmount = damageSection == null
                ? 0.0
                : damageSection.getDouble("amount", 2.0);
        int damageIntervalTicks = damageSection == null
                ? 20
                : damageSection.getInt("interval-ticks", 20);

        damageAmount = clampDouble(damageAmount, 0.0, 1000.0);
        damageIntervalTicks = clampInt(damageIntervalTicks, 1, 20 * 60);

        ConfigurationSection particlesSection = section.getConfigurationSection("particles");
        boolean particlesEnabled = particlesSection == null || particlesSection.getBoolean("enabled", true);
        int particleIntervalTicks = particlesSection == null
                ? 1
                : particlesSection.getInt("interval-ticks", 1);
        String particleName = particlesSection == null
                ? "ELECTRIC_SPARK"
                : particlesSection.getString("particle", "ELECTRIC_SPARK");
        int particleCount = particlesSection == null
                ? 1
                : particlesSection.getInt("count", 1);
        double particleStep = particlesSection == null
                ? 0.35
                : particlesSection.getDouble("step", 0.35);
        int particleMaxPoints = particlesSection == null
                ? 48
                : particlesSection.getInt("max-points", 48);

        Particle particle = parseParticle(particleName, Particle.ELECTRIC_SPARK);
        particleIntervalTicks = clampInt(particleIntervalTicks, 1, 20);
        particleCount = clampInt(particleCount, 1, 20);
        particleStep = clampDouble(particleStep, 0.15, 1.5);
        particleMaxPoints = clampInt(particleMaxPoints, 4, 128);

        ConfigurationSection visualSection = section.getConfigurationSection("visual");
        ConfigurationSection hookDisplaySection = visualSection == null
                ? null
                : visualSection.getConfigurationSection("hook-display");

        boolean hookDisplayEnabled = hookDisplaySection == null
                || hookDisplaySection.getBoolean("enabled", true);
        Material hookDisplayMaterial = hookDisplaySection == null
                ? Material.CHAIN
                : parseMaterial(hookDisplaySection.getString("material", "CHAIN"), Material.CHAIN);
        boolean hookDisplayGlowing = hookDisplaySection == null
                || hookDisplaySection.getBoolean("glowing", true);
        float hookDisplayScale = hookDisplaySection == null
                ? 0.45F
                : (float) hookDisplaySection.getDouble("scale", 0.45);

        hookDisplayScale = (float) clampDouble(hookDisplayScale, 0.05, 3.0);

        return new EntityHookSettings(
                true,
                targetPlayers,
                targetMobs,
                targetAnimals,
                targetMonsters,
                requireWorldPvp,
                bypassPermission,
                durationTicks,
                maxHitDistance,
                breakDistance,
                bindRadius,
                pullStrength,
                maxPullSpeed,
                velocityDamping,
                damageEnabled,
                damageAmount,
                damageIntervalTicks,
                particlesEnabled,
                particleIntervalTicks,
                particle,
                particleCount,
                particleStep,
                particleMaxPoints,
                hookDisplayEnabled,
                hookDisplayMaterial,
                hookDisplayGlowing,
                hookDisplayScale
        );
    }

    private static Particle parseParticle(String text, Particle fallback) {
        if (text == null || text.isBlank()) {
            return fallback;
        }

        try {
            return Particle.valueOf(text.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

    private static Material parseMaterial(String text, Material fallback) {
        if (text == null || text.isBlank()) {
            return fallback;
        }

        Material material = Material.matchMaterial(text.trim().toUpperCase(Locale.ROOT));
        return material == null ? fallback : material;
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clampDouble(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public boolean enabled() {
        return enabled;
    }

    public boolean targetPlayers() {
        return targetPlayers;
    }

    public boolean targetMobs() {
        return targetMobs;
    }

    public boolean targetAnimals() {
        return targetAnimals;
    }

    public boolean targetMonsters() {
        return targetMonsters;
    }

    public boolean requireWorldPvp() {
        return requireWorldPvp;
    }

    public String bypassPermission() {
        return bypassPermission;
    }

    public int durationTicks() {
        return durationTicks;
    }

    public double maxHitDistance() {
        return maxHitDistance;
    }

    public double breakDistance() {
        return breakDistance;
    }

    public double bindRadius() {
        return bindRadius;
    }

    public double pullStrength() {
        return pullStrength;
    }

    public double maxPullSpeed() {
        return maxPullSpeed;
    }

    public double velocityDamping() {
        return velocityDamping;
    }

    public boolean damageEnabled() {
        return damageEnabled;
    }

    public double damageAmount() {
        return damageAmount;
    }

    public int damageIntervalTicks() {
        return damageIntervalTicks;
    }

    public boolean particlesEnabled() {
        return particlesEnabled;
    }

    public int particleIntervalTicks() {
        return particleIntervalTicks;
    }

    public Particle particle() {
        return particle;
    }

    public int particleCount() {
        return particleCount;
    }

    public double particleStep() {
        return particleStep;
    }

    public int particleMaxPoints() {
        return particleMaxPoints;
    }

    public boolean hookDisplayEnabled() {
        return hookDisplayEnabled;
    }

    public Material hookDisplayMaterial() {
        return hookDisplayMaterial;
    }

    public boolean hookDisplayGlowing() {
        return hookDisplayGlowing;
    }

    public float hookDisplayScale() {
        return hookDisplayScale;
    }
}
