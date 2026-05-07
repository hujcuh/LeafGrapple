package me.LeafPixel.leafgrapple.hook;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.UseCooldownComponent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class HookItemService {

    private final Plugin plugin;
    private final HookKeys keys;
    private final Map<String, HookTier> tiers = new LinkedHashMap<>();

    public HookItemService(Plugin plugin, HookKeys keys) {
        this.plugin = plugin;
        this.keys = keys;
    }

    public void loadFromConfig() {
        tiers.clear();

        ConfigurationSection hooksSection = plugin.getConfig().getConfigurationSection("hooks");
        if (hooksSection == null) {
            plugin.getLogger().warning("No hooks section found in config.yml");
            return;
        }

        for (String id : hooksSection.getKeys(false)) {
            ConfigurationSection section = hooksSection.getConfigurationSection(id);
            if (section == null) {
                continue;
            }

            HookTier tier = HookTier.fromConfig(id, section);
            tiers.put(tier.id(), tier);
        }

        plugin.getLogger().info("Loaded " + tiers.size() + " hook tiers.");
    }

    public HookTier getTier(String id) {
        if (id == null) {
            return null;
        }

        return tiers.get(id.toLowerCase(Locale.ROOT));
    }

    public Collection<HookTier> getTiers() {
        return tiers.values();
    }

    public ItemStack createHookItem(HookTier tier) {
        ItemStack item = new ItemStack(tier.material());
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.displayName(Component.text(tier.displayName()));

        meta.lore(java.util.List.of(
                Component.text("类型: " + tier.id()),
                Component.text("材质: " + tier.material().name()),
                Component.text("物品模型: " + modelText(tier.itemModel())),
                Component.text("钩头材质: " + tier.displayMaterial().name()),
                Component.text("钩头模型: " + modelText(tier.displayItemModel())),
                Component.text("最大距离: " + tier.maxDistance()),
                Component.text("发射速度: " + tier.launchSpeed()),
                Component.text("最大拉扯速度: " + tier.maxPullSpeed()),
                Component.text("耐久: " + durabilityText(tier)),
                Component.text("冷却: " + tier.cooldownTicks() + " ticks")
        ));

        /*
         * 1.21.4+ Modern item_model.
         * ItemsAdder Modern graphics 生成的模型可以通过这个字段显示。
         */
        if (tier.itemModel() != null) {
            meta.setItemModel(tier.itemModel());
        }

        /*
         * 旧 CustomModelData 兼容。
         */
        if (tier.customModelData() > 0) {
            meta.setCustomModelData(tier.customModelData());
        }

        /*
         * 关键：
         * 给钩爪物品写入独立 cooldown group。
         * 这样之后 player.setCooldown(itemStack, ticks) 时，
         * 不会影响所有 PAPER，只会影响同 cooldown group 的钩爪。
         */
        applyUseCooldownComponent(meta, tier, tier.cooldownTicks());

        /*
         * 1.21.4+ 自定义最大耐久。
         */
        if (tier.durabilityEnabled() && tier.maxDurability() > 0 && meta instanceof Damageable damageable) {
            damageable.setMaxDamage(tier.maxDurability());
            damageable.setDamage(0);

            /*
             * 带耐久的物品不建议堆叠。
             */
            meta.setMaxStackSize(1);
        }

        /*
         * LeafGrapple 自己的 PDC 标记。
         * /leafgrapple give 生成的物品优先通过这个识别。
         */
        meta.getPersistentDataContainer().set(
                keys.hookTierKey(),
                PersistentDataType.STRING,
                tier.id()
        );

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * 获取钩爪类型。
     *
     * 识别顺序：
     * 1. PDC hook_tier
     * 2. 1.21.4+ item_model
     * 3. CustomModelData
     */
    public HookTier getHookTier(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        HookTier pdcTier = getHookTierByPdc(meta);
        if (pdcTier != null) {
            return pdcTier;
        }

        HookTier itemModelTier = getHookTierByItemModel(item, meta);
        if (itemModelTier != null) {
            return itemModelTier;
        }

        return getHookTierByCustomModelData(item, meta);
    }

    private HookTier getHookTierByPdc(ItemMeta meta) {
        String id = meta.getPersistentDataContainer().get(
                keys.hookTierKey(),
                PersistentDataType.STRING
        );

        if (id == null) {
            return null;
        }

        return getTier(id);
    }

    private HookTier getHookTierByItemModel(ItemStack item, ItemMeta meta) {
        if (!meta.hasItemModel()) {
            return null;
        }

        NamespacedKey itemModel = meta.getItemModel();
        if (itemModel == null) {
            return null;
        }

        for (HookTier tier : tiers.values()) {
            if (item.getType() != tier.material()) {
                continue;
            }

            if (tier.itemModel() == null) {
                continue;
            }

            if (tier.itemModel().equals(itemModel)) {
                return tier;
            }
        }

        return null;
    }

    private HookTier getHookTierByCustomModelData(ItemStack item, ItemMeta meta) {
        if (!meta.hasCustomModelData()) {
            return null;
        }

        int customModelData = meta.getCustomModelData();

        for (HookTier tier : tiers.values()) {
            if (item.getType() != tier.material()) {
                continue;
            }

            if (tier.customModelData() <= 0) {
                continue;
            }

            if (tier.customModelData() == customModelData) {
                return tier;
            }
        }

        return null;
    }

    public boolean isHook(ItemStack item) {
        return getHookTier(item) != null;
    }

    /**
     * 对当前玩家手上的对应钩爪施加冷却遮罩。
     *
     * 注意：
     * 这里使用 player.setCooldown(itemStack, ticks)，
     * 不再使用 player.setCooldown(Material.PAPER, ticks)。
     */
    public void applyHookCooldown(Player player, HookTier tier, int cooldownTicks) {
        if (player == null || tier == null || cooldownTicks <= 0) {
            return;
        }

        if (tryApplyHookCooldownInSlot(player, EquipmentSlot.HAND, tier, cooldownTicks)) {
            return;
        }

        tryApplyHookCooldownInSlot(player, EquipmentSlot.OFF_HAND, tier, cooldownTicks);
    }

    private boolean tryApplyHookCooldownInSlot(Player player, EquipmentSlot slot, HookTier tier, int cooldownTicks) {
        ItemStack item = slot == EquipmentSlot.OFF_HAND
                ? player.getInventory().getItemInOffHand()
                : player.getInventory().getItemInMainHand();

        HookTier itemTier = getHookTier(item);
        if (itemTier == null || !itemTier.id().equalsIgnoreCase(tier.id())) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        /*
         * 对 ItemsAdder /iaget 生成的物品也动态写入 cooldown group。
         * 这样即使 ItemsAdder 原物品没有这个组件，也能在使用时获得独立冷却遮罩。
         */
        applyUseCooldownComponent(meta, tier, cooldownTicks);
        item.setItemMeta(meta);

        if (slot == EquipmentSlot.OFF_HAND) {
            player.getInventory().setItemInOffHand(item);
        } else {
            player.getInventory().setItemInMainHand(item);
        }

        /*
         * 重点：
         * 按 ItemStack 冷却，而不是按 Material 冷却。
         */
        player.setCooldown(item, cooldownTicks);

        return true;
    }

    private void applyUseCooldownComponent(ItemMeta meta, HookTier tier, int cooldownTicks) {
        if (meta == null || tier == null) {
            return;
        }

        UseCooldownComponent cooldownComponent = meta.getUseCooldown();

        cooldownComponent.setCooldownGroup(getCooldownGroupKey(tier));

        /*
         * UseCooldownComponent 的单位是秒。
         */
        float cooldownSeconds = Math.max(0.05F, cooldownTicks / 20.0F);
        cooldownComponent.setCooldownSeconds(cooldownSeconds);

        meta.setUseCooldown(cooldownComponent);
    }

    private NamespacedKey getCooldownGroupKey(HookTier tier) {
        return new NamespacedKey(plugin, "hook_" + tier.id().toLowerCase(Locale.ROOT));
    }

    /**
     * 对玩家当前持有的钩爪扣耐久。
     */
    public void damageHookItem(Player player, HookTier tier, int amount) {
        if (player == null || tier == null) {
            return;
        }

        if (!tier.durabilityEnabled() || tier.maxDurability() <= 0 || amount <= 0) {
            return;
        }

        if (tryDamageInSlot(player, EquipmentSlot.HAND, tier, amount)) {
            return;
        }

        tryDamageInSlot(player, EquipmentSlot.OFF_HAND, tier, amount);
    }

    private boolean tryDamageInSlot(Player player, EquipmentSlot slot, HookTier tier, int amount) {
        ItemStack item = slot == EquipmentSlot.OFF_HAND
                ? player.getInventory().getItemInOffHand()
                : player.getInventory().getItemInMainHand();

        HookTier itemTier = getHookTier(item);
        if (itemTier == null || !itemTier.id().equalsIgnoreCase(tier.id())) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable damageable)) {
            return false;
        }

        int maxDamage = tier.maxDurability();
        int currentDamage = damageable.hasDamage() ? damageable.getDamage() : 0;
        int newDamage = currentDamage + amount;

        if (newDamage >= maxDamage) {
            breakItemInSlot(player, slot);
            return true;
        }

        damageable.setMaxDamage(maxDamage);
        damageable.setDamage(newDamage);
        item.setItemMeta(meta);

        if (slot == EquipmentSlot.OFF_HAND) {
            player.getInventory().setItemInOffHand(item);
        } else {
            player.getInventory().setItemInMainHand(item);
        }

        return true;
    }

    private void breakItemInSlot(Player player, EquipmentSlot slot) {
        if (slot == EquipmentSlot.OFF_HAND) {
            player.getInventory().setItemInOffHand(null);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
        player.sendActionBar(Component.text("钩爪已损坏"));
    }

    private String modelText(NamespacedKey key) {
        return key == null ? "无" : key.toString();
    }

    private String durabilityText(HookTier tier) {
        if (!tier.durabilityEnabled() || tier.maxDurability() <= 0) {
            return "无限";
        }

        return String.valueOf(tier.maxDurability());
    }
}
