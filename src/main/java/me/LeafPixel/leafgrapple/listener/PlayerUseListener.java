package me.LeafPixel.leafgrapple.listener;

import me.LeafPixel.leafgrapple.hook.GrappleSession;
import me.LeafPixel.leafgrapple.hook.HookItemService;
import me.LeafPixel.leafgrapple.hook.HookTier;
import me.LeafPixel.leafgrapple.service.CooldownService;
import me.LeafPixel.leafgrapple.service.GrappleService;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public final class PlayerUseListener implements Listener {

    private final Plugin plugin;
    private final HookItemService hookItemService;
    private final GrappleService grappleService;
    private final CooldownService cooldownService;

    public PlayerUseListener(
            Plugin plugin,
            HookItemService hookItemService,
            GrappleService grappleService,
            CooldownService cooldownService
    ) {
        this.plugin = plugin;
        this.hookItemService = hookItemService;
        this.grappleService = grappleService;
        this.cooldownService = cooldownService;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onUse(PlayerInteractEvent event) {
        EquipmentSlot hand = event.getHand();

        if (hand == null) {
            return;
        }

        Player player = event.getPlayer();
        Action action = event.getAction();

        ItemStack item = getItemInUsedHand(player, hand);
        HookTier tier = hookItemService.getHookTier(item);

        if (tier == null) {
            return;
        }

        /*
         * 只要当前触发手拿的是钩爪，就阻止原版交互。
         * 这可以避免右键方块时开门、放置、触发其他行为。
         */
        event.setUseItemInHand(Event.Result.DENY);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setCancelled(true);

        if (isLeftClick(action)) {
            handleLeftClick(player);
            return;
        }

        if (isRightClick(action)) {
            handleRightClick(player, tier);
        }
    }

    private void handleLeftClick(Player player) {
        if (!grappleService.hasSession(player)) {
            return;
        }

        grappleService.cancelByPlayerSafely(player);
    }

    private void handleRightClick(Player player, HookTier tier) {
        /*
         * recast 防抖。
         * 避免某些客户端/主副手/交互情况下短时间重复触发。
         */
        if (cooldownService.isCoolingDown(player.getUniqueId(), "recast")) {
            return;
        }

        int recastCooldownTicks = plugin.getConfig().getInt("settings.recast-cooldown-ticks", 4);
        cooldownService.start(player.getUniqueId(), "recast", recastCooldownTicks);

        GrappleSession session = grappleService.getSession(player);

        /*
         * 已有会话时：
         * - 如果钩住了，右键开始拉回。
         * - 如果正在飞行或正在拉回，忽略。
         */
        if (session != null) {
            if (session.hooked() && !session.pulling()) {
                grappleService.startPullSafely(player, session.anchor());
            }

            return;
        }

        /*
         * 没有会话时，检查发射冷却。
         */
        if (cooldownService.isCoolingDown(player.getUniqueId(), "launch")) {
            int remainingTicks = cooldownService.getRemainingTicks(player.getUniqueId(), "launch");
            player.sendActionBar(Component.text("钩爪冷却中: " + remainingTicks + " ticks"));
            return;
        }

        grappleService.fireSafely(player, tier);
    }

    private ItemStack getItemInUsedHand(Player player, EquipmentSlot hand) {
        if (hand == EquipmentSlot.OFF_HAND) {
            return player.getInventory().getItemInOffHand();
        }

        return player.getInventory().getItemInMainHand();
    }

    private boolean isRightClick(Action action) {
        return action == Action.RIGHT_CLICK_AIR
                || action == Action.RIGHT_CLICK_BLOCK;
    }

    private boolean isLeftClick(Action action) {
        return action == Action.LEFT_CLICK_AIR
                || action == Action.LEFT_CLICK_BLOCK;
    }
}
