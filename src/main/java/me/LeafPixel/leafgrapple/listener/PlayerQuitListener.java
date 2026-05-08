package me.LeafPixel.leafgrapple.listener;

import me.LeafPixel.leafgrapple.entityhook.EntityHookService;
import me.LeafPixel.leafgrapple.service.CooldownService;
import me.LeafPixel.leafgrapple.service.GrappleService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerQuitListener implements Listener {

    private final GrappleService grappleService;
    private final CooldownService cooldownService;
    private final EntityHookService entityHookService;

    public PlayerQuitListener(
            GrappleService grappleService,
            CooldownService cooldownService,
            EntityHookService entityHookService
    ) {
        this.grappleService = grappleService;
        this.cooldownService = cooldownService;
        this.entityHookService = entityHookService;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        grappleService.clearSession(event.getPlayer().getUniqueId());
        entityHookService.clearByOwner(event.getPlayer().getUniqueId());
        entityHookService.clearByTarget(event.getPlayer().getUniqueId());
        cooldownService.clearAll(event.getPlayer().getUniqueId());
    }
}
