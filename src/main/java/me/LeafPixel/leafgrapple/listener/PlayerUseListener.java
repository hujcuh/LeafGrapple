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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = event.getAction();
        Player player = event.getPlayer();

        ItemStack item = player.getInventory().getItemInMainHand();
        HookTier tier = hookItemService.getHookTier(item);

        if (tier == null) {
            return;
        }

        event.setUseItemInHand(Event.Result.DENY);
        event.setUseInteractedBlock(Event.Result.DENY);

        GrappleSession session = grappleService.getSession(player);

        /*
         * 左键取消。
         */
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);

            if (session != null) {
                grappleService.cancelByPlayer(player);
            }

            return;
        }

        /*
         * 非右键不处理。
         */
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        event.setCancelled(true);

        /*
         * IDLE -> FLYING
         */
        if (session == null) {
            if (cooldownService.isCoolingDown(player.getUniqueId(), "launch")) {
                int remainingTicks = cooldownService.getRemainingTicks(player.getUniqueId(), "launch");
                player.sendActionBar(Component.text("钩爪冷却中: " + remainingTicks + " ticks"));
                return;
            }

            grappleService.fire(player, tier);
            return;
        }

        /*
         * HOOKED -> PULLING
         */
        if (session.hooked() && !session.pulling()) {
            if (session.anchor() == null) {
                grappleService.clearSession(player.getUniqueId());
                return;
            }

            grappleService.startPull(player, session.anchor());
            return;
        }

        /*
         * FLYING -> 重新发射。
         */
        if (!session.pulling() && !session.hooked()) {
            int recastCooldownTicks = plugin.getConfig().getInt("settings.recast-cooldown-ticks", 5);

            if (cooldownService.isCoolingDown(player.getUniqueId(), "recast")) {
                int remainingTicks = cooldownService.getRemainingTicks(player.getUniqueId(), "recast");
                player.sendActionBar(Component.text("重新发射冷却中: " + remainingTicks + " ticks"));
                return;
            }

            cooldownService.start(player.getUniqueId(), "recast", recastCooldownTicks);

            if (recastCooldownTicks > 0) {
                hookItemService.applyHookCooldown(player, tier, recastCooldownTicks);
            }

            grappleService.fire(player, tier);
            player.sendActionBar(Component.text("重新发射钩爪"));
        }
    }
}