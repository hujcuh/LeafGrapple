package me.LeafPixel.leafgrapple.hook;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import me.LeafPixel.leafgrapple.entityhook.EntityHookSettings;

import java.util.Locale;

public final class HookTier {

    private final String id;
    private final String displayName;

    private final Material material;
    private final int customModelData;
    private final NamespacedKey itemModel;

    private final Material displayMaterial;
    private final int displayCustomModelData;
    private final NamespacedKey displayItemModel;

    private final double maxDistance;
    private final double maxPullDistance;
    private final double launchSpeed;
    private final double pullAcceleration;
    private final double maxPullSpeed;
    private final int cooldownTicks;

    private final boolean durabilityEnabled;
    private final int maxDurability;
    private final int durabilityCostOnFinish;

    private final boolean showTechnicalLore;
    private final EntityHookSettings entityHookSettings;

    private final boolean displayGlowing;
    private final float displayScaleX;
    private final float displayScaleY;
    private final float displayScaleZ;
    private final float displayOffsetX;
    private final float displayOffsetY;
    private final float displayOffsetZ;
    private final float displayRotationAxisX;
    private final float displayRotationAxisY;
    private final float displayRotationAxisZ;
    private final float displayRotationAngleDegrees;
    

    public HookTier(
            String id,
            String displayName,
            Material material,
            int customModelData,
            NamespacedKey itemModel,
            Material displayMaterial,
            int displayCustomModelData,
            NamespacedKey displayItemModel,
            double maxDistance,
            double maxPullDistance,
            double launchSpeed,
            double pullAcceleration,
            double maxPullSpeed,
            int cooldownTicks,
            boolean durabilityEnabled,
            int maxDurability,
            int durabilityCostOnFinish,
            boolean showTechnicalLore,
            EntityHookSettings entityHookSettings,
            boolean displayGlowing,
            float displayScaleX,
            float displayScaleY,
            float displayScaleZ,
            float displayOffsetX,
            float displayOffsetY,
            float displayOffsetZ,
            float displayRotationAxisX,
            float displayRotationAxisY,
            float displayRotationAxisZ,
            float displayRotationAngleDegrees
    ) {
        this.id = id;
        this.displayName = displayName;
        this.material = material;
        this.customModelData = customModelData;
        this.itemModel = itemModel;
        this.displayMaterial = displayMaterial;
        this.displayCustomModelData = displayCustomModelData;
        this.displayItemModel = displayItemModel;
        this.maxDistance = maxDistance;
        this.maxPullDistance = maxPullDistance;
        this.launchSpeed = launchSpeed;
        this.pullAcceleration = pullAcceleration;
        this.maxPullSpeed = maxPullSpeed;
        this.cooldownTicks = cooldownTicks;
        this.durabilityEnabled = durabilityEnabled;
        this.maxDurability = maxDurability;
        this.durabilityCostOnFinish = durabilityCostOnFinish;
        this.showTechnicalLore = showTechnicalLore;
        this.entityHookSettings = entityHookSettings;
        this.displayGlowing = displayGlowing;
        this.displayScaleX = displayScaleX;
        this.displayScaleY = displayScaleY;
        this.displayScaleZ = displayScaleZ;
        this.displayOffsetX = displayOffsetX;
        this.displayOffsetY = displayOffsetY;
        this.displayOffsetZ = displayOffsetZ;
        this.displayRotationAxisX = displayRotationAxisX;
        this.displayRotationAxisY = displayRotationAxisY;
        this.displayRotationAxisZ = displayRotationAxisZ;
        this.displayRotationAngleDegrees = displayRotationAngleDegrees;
    }

    public static HookTier fromConfig(String id, ConfigurationSection section) {
        ItemSpec legacyItem = parseLegacyItemSpec(section.getString("item", null));

        Material material = parseMaterial(
                section.getString("item-material", null),
                legacyItem.material()
        );

        int customModelData = legacyItem.customModelData();

        /*
         * 是否启用 ItemsAdder Modern 自动兼容。
         *
         * true:
         * leafgrapple:wood_grapple
         * ->
         * leafgrapple:ia_auto/wood_grapple
         *
         * false:
         * 保持原样，适合纯资源包或其他插件。
         */
        boolean itemsAdderModernAutoModel = section.getBoolean("itemsadder-modern-auto-model", true);

        NamespacedKey itemModel = parseNamespacedKey(section.getString("item-model", null));
        itemModel = applyItemsAdderModernAutoModel(itemModel, itemsAdderModernAutoModel);

        ItemSpec legacyDisplayItem = parseLegacyItemSpec(section.getString("display-item", null));

        Material displayMaterial = parseMaterial(
                section.getString("display-item-material", null),
                legacyDisplayItem.hasValue() ? legacyDisplayItem.material() : material
        );

        int displayCustomModelData = legacyDisplayItem.hasValue()
                ? legacyDisplayItem.customModelData()
                : customModelData;

        NamespacedKey displayItemModel = parseNamespacedKey(section.getString("display-item-model", null));
        displayItemModel = applyItemsAdderModernAutoModel(displayItemModel, itemsAdderModernAutoModel);

        if (displayItemModel == null) {
            displayItemModel = itemModel;
        }

        double maxDistance = section.getDouble("max-distance", 16.0);

        /*
         * 新增：
         * 玩家右键拉回时，玩家与拉扯目标点之间允许的最大距离。
         * 默认等于 max-distance。
         * <= 0 表示不限制。
         */
        double maxPullDistance = section.getDouble("max-pull-distance", maxDistance);


        EntityHookSettings entityHookSettings = EntityHookSettings.fromConfig(
                section.getConfigurationSection("entity-hook"),
                maxDistance
        );

        ConfigurationSection durabilitySection = section.getConfigurationSection("durability");

        boolean durabilityEnabled = durabilitySection != null
                && durabilitySection.getBoolean("enabled", false);

        int maxDurability = durabilitySection == null
                ? 0
                : durabilitySection.getInt("max", 0);

        int durabilityCostOnFinish = durabilitySection == null
                ? 0
                : durabilitySection.getInt("cost-on-finish", 1);

        ConfigurationSection loreSection = section.getConfigurationSection("lore");

        /*
         * 新增：
         * 是否显示技术类 lore。
         *
         * false 时隐藏：
         * 类型、材质、物品模型、钩头材质、钩头模型。
         */
        boolean showTechnicalLore = loreSection != null
                && loreSection.getBoolean("show-technical-info", false);

        ConfigurationSection displaySection = section.getConfigurationSection("display");

        boolean glowing = displaySection == null
                || displaySection.getBoolean("glowing", true);

        float scaleX = getFloat(displaySection, "scale-x", 0.35F);
        float scaleY = getFloat(displaySection, "scale-y", 0.35F);
        float scaleZ = getFloat(displaySection, "scale-z", 0.35F);

        float offsetX = getFloat(displaySection, "offset-x", 0.0F);
        float offsetY = getFloat(displaySection, "offset-y", -0.1F);
        float offsetZ = getFloat(displaySection, "offset-z", 0.0F);

        float axisX = getFloat(displaySection, "rotation-axis-x", 0.0F);
        float axisY = getFloat(displaySection, "rotation-axis-y", 1.0F);
        float axisZ = getFloat(displaySection, "rotation-axis-z", 0.0F);
        float angleDegrees = getFloat(displaySection, "rotation-angle-degrees", 0.0F);

        return new HookTier(
                id.toLowerCase(Locale.ROOT),
                section.getString("display-name", id),
                material,
                customModelData,
                itemModel,
                displayMaterial,
                displayCustomModelData,
                displayItemModel,
                maxDistance,
                maxPullDistance,
                section.getDouble("launch-speed", 1.2),
                section.getDouble("pull-acceleration", 0.05),
                section.getDouble("max-pull-speed", 0.85),
                section.getInt("cooldown-ticks", 20),
                durabilityEnabled,
                maxDurability,
                durabilityCostOnFinish,
                showTechnicalLore,
                entityHookSettings,
                glowing,
                scaleX,
                scaleY,
                scaleZ,
                offsetX,
                offsetY,
                offsetZ,
                axisX,
                axisY,
                axisZ,
                angleDegrees
        );
    }

    private static NamespacedKey applyItemsAdderModernAutoModel(
            NamespacedKey key,
            boolean enabled
    ) {
        if (!enabled || key == null) {
            return key;
        }

        String namespace = key.getNamespace();
        String path = key.getKey();

        /*
         * 已经是 IA 实际模型路径，不重复处理。
         */
        if (path.startsWith("ia_auto/")) {
            return key;
        }

        /*
         * 如果用户写的是：
         * leafgrapple:wood_grapple
         *
         * 自动转换为：
         * leafgrapple:ia_auto/wood_grapple
         *
         * 注意：
         * 推荐 item-model 写 IA item id，而不是模型文件路径。
         */
        return new NamespacedKey(namespace, "ia_auto/" + path);
    }

    private static float getFloat(ConfigurationSection section, String path, float def) {
        if (section == null) {
            return def;
        }

        return (float) section.getDouble(path, def);
    }

    private static Material parseMaterial(String text, Material fallback) {
        if (text == null || text.isBlank()) {
            return fallback;
        }

        Material material = Material.matchMaterial(text.toUpperCase(Locale.ROOT));
        return material == null ? fallback : material;
    }

    private static NamespacedKey parseNamespacedKey(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        return NamespacedKey.fromString(text.toLowerCase(Locale.ROOT));
    }

    private static ItemSpec parseLegacyItemSpec(String text) {
        if (text == null || text.isBlank()) {
            return new ItemSpec(Material.PAPER, 0, false);
        }

        Material material = Material.PAPER;
        int customModelData = 0;

        String[] parts = text.split(":");

        if (parts.length >= 1) {
            Material parsed = Material.matchMaterial(parts[0].toUpperCase(Locale.ROOT));
            if (parsed != null) {
                material = parsed;
            }
        }

        if (parts.length >= 2) {
            try {
                customModelData = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {
                customModelData = 0;
            }
        }

        return new ItemSpec(material, customModelData, true);
    }

    private record ItemSpec(Material material, int customModelData, boolean hasValue) {
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public Material material() {
        return material;
    }

    public int customModelData() {
        return customModelData;
    }

    public NamespacedKey itemModel() {
        return itemModel;
    }

    public Material displayMaterial() {
        return displayMaterial;
    }

    public int displayCustomModelData() {
        return displayCustomModelData;
    }

    public NamespacedKey displayItemModel() {
        return displayItemModel;
    }

    public double maxDistance() {
        return maxDistance;
    }

    public double maxPullDistance() {
        return maxPullDistance;
    }

    public double launchSpeed() {
        return launchSpeed;
    }

    public double pullAcceleration() {
        return pullAcceleration;
    }

    public double maxPullSpeed() {
        return maxPullSpeed;
    }

    public int cooldownTicks() {
        return cooldownTicks;
    }

    public boolean durabilityEnabled() {
        return durabilityEnabled;
    }

    public int maxDurability() {
        return maxDurability;
    }

    public int durabilityCostOnFinish() {
        return durabilityCostOnFinish;
    }

    public boolean showTechnicalLore() {
        return showTechnicalLore;
    }

    public boolean displayGlowing() {
        return displayGlowing;
    }

    public float displayScaleX() {
        return displayScaleX;
    }

    public float displayScaleY() {
        return displayScaleY;
    }

    public float displayScaleZ() {
        return displayScaleZ;
    }

    public float displayOffsetX() {
        return displayOffsetX;
    }

    public float displayOffsetY() {
        return displayOffsetY;
    }

    public float displayOffsetZ() {
        return displayOffsetZ;
    }

    public float displayRotationAxisX() {
        return displayRotationAxisX;
    }

    public float displayRotationAxisY() {
        return displayRotationAxisY;
    }

    public float displayRotationAxisZ() {
        return displayRotationAxisZ;
    }

    public float displayRotationAngleDegrees() {
        return displayRotationAngleDegrees;
    }

    public EntityHookSettings entityHookSettings() {
        return entityHookSettings;
    }
}
