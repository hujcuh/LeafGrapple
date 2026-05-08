package me.LeafPixel.leafgrapple;

import me.LeafPixel.leafgrapple.command.LeafGrappleCommand;
import me.LeafPixel.leafgrapple.entityhook.EntityHookService;
import me.LeafPixel.leafgrapple.hook.HookItemService;
import me.LeafPixel.leafgrapple.hook.HookKeys;
import me.LeafPixel.leafgrapple.listener.PlayerQuitListener;
import me.LeafPixel.leafgrapple.listener.PlayerUseListener;
import me.LeafPixel.leafgrapple.service.CooldownService;
import me.LeafPixel.leafgrapple.service.GrappleService;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class LeafGrapplePlugin extends JavaPlugin {

    private static LeafGrapplePlugin instance;

    private HookKeys hookKeys;
    private HookItemService hookItemService;
    private GrappleService grappleService;
    private CooldownService cooldownService;
    private EntityHookService entityHookService;

    public static LeafGrapplePlugin getInstance() {
        return instance;
    }

    public HookItemService hookItemService() {
        return hookItemService;
    }

    public GrappleService grappleService() {
        return grappleService;
    }

    public CooldownService cooldownService() {
        return cooldownService;
    }

    public EntityHookService entityHookService() {
        return entityHookService;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.hookKeys = new HookKeys(this);

        this.hookItemService = new HookItemService(this, hookKeys);
        this.hookItemService.loadFromConfig();

        this.cooldownService = new CooldownService();

        this.entityHookService = new EntityHookService(
                this,
                hookItemService,
                cooldownService
        );

        this.grappleService = new GrappleService(
                this,
                cooldownService,
                hookItemService,
                entityHookService
        );

        registerCommands();
        registerListeners();

        getLogger().info("LeafGrapple enabled.");
        getLogger().info("Paper/Folia compatibility mode initialized.");
    }

    @Override
    public void onDisable() {
        if (grappleService != null) {
            grappleService.clearAll();
        }

        if (entityHookService != null) {
            entityHookService.clearAll();
        }

        if (cooldownService != null) {
            cooldownService.clearAll();
        }

        getLogger().info("LeafGrapple disabled.");
    }

    /**
     * Reload LeafGrapple configuration safely.
     *
     * This will:
     * - clear active grapple sessions
     * - clear active entity hook sessions
     * - reload config.yml
     * - reload hook tiers
     *
     * It does not clear cooldowns by default.
     */
    public void reloadPlugin() {
        if (grappleService != null) {
            grappleService.clearAll();
        }

        if (entityHookService != null) {
            entityHookService.clearAll();
        }

        reloadConfig();

        if (hookItemService != null) {
            hookItemService.loadFromConfig();
        }

        getLogger().info("LeafGrapple configuration reloaded.");
    }

    private void registerCommands() {
        LeafGrappleCommand commandExecutor = new LeafGrappleCommand(hookItemService);

        PluginCommand command = getCommand("leafgrapple");
        if (command == null) {
            getLogger().severe("Command leafgrapple is not defined in plugin.yml");
            return;
        }

        command.setExecutor(commandExecutor);
        command.setTabCompleter(commandExecutor);
    }

    private void registerListeners() {
        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(
                new PlayerUseListener(this, hookItemService, grappleService, cooldownService),
                this
        );

        pluginManager.registerEvents(
                new PlayerQuitListener(grappleService, cooldownService, entityHookService),
                this
        );
    }
}
