package me.LeafPixel.leafgrapple.listener;

import me.LeafPixel.leafgrapple.service.CooldownService;
import me.LeafPixel.leafgrapple.service.GrappleService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerQuitListener implements Listener {

    private final GrappleService grappleService;
    private final CooldownService cooldownService;

    public PlayerQuitListener(GrappleService grappleService, CooldownService cooldownService) {
        this.grappleService = grappleService;
        this.cooldownService = cooldownService;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        grappleService.clearSession(event.getPlayer().getUniqueId());
        cooldownService.clearAll(event.getPlayer().getUniqueId());
    }
}