package me.LeafPixel.leafgrapple.hook;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

/*
    this class is responsible for managing the PDC keys used in the plugin.
*/
public final class HookKeys {

    private final NamespacedKey hookTierKey;

    public HookKeys(Plugin plugin) {
        this.hookTierKey = new NamespacedKey(plugin, "hook_tier");
    }

    public NamespacedKey hookTierKey() {
        return hookTierKey;
    }
}
