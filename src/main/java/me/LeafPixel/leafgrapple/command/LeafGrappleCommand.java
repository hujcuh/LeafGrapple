package me.LeafPixel.leafgrapple.command;

import me.LeafPixel.leafgrapple.LeafGrapplePlugin;
import me.LeafPixel.leafgrapple.hook.HookItemService;
import me.LeafPixel.leafgrapple.hook.HookTier;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class LeafGrappleCommand implements CommandExecutor, TabCompleter {

    private final HookItemService hookItemService;

    public LeafGrappleCommand(HookItemService hookItemService) {
        this.hookItemService = hookItemService;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        String subCommand = args[0].toLowerCase(Locale.ROOT);

        if (subCommand.equals("give")) {
            handleGive(sender, label, args);
            return true;
        }

        if (subCommand.equals("list")) {
            handleList(sender);
            return true;
        }

        if (subCommand.equals("reload")) {
            handleReload(sender);
            return true;
        }

        if (subCommand.equals("help")) {
            sendHelp(sender, label);
            return true;
        }

        sender.sendMessage(Component.text("未知子命令。"));
        sendHelp(sender, label);
        return true;
    }

    private void handleGive(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("这个命令只能由玩家执行。"));
            return;
        }

        if (!player.hasPermission("leafgrapple.admin")) {
            player.sendMessage(Component.text("你没有权限使用这个命令。"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("用法: /" + label + " give <类型>"));
            sendAvailableTypes(player);
            return;
        }

        String tierId = args[1].toLowerCase(Locale.ROOT);
        HookTier tier = hookItemService.getTier(tierId);

        if (tier == null) {
            player.sendMessage(Component.text("不存在的钩爪类型: " + tierId));
            sendAvailableTypes(player);
            return;
        }

        ItemStack item = hookItemService.createHookItem(tier);
        player.getInventory().addItem(item);

        player.sendMessage(Component.text("已获得: " + tier.displayName()));
    }

    private void handleList(CommandSender sender) {
        sender.sendMessage(Component.text("可用钩爪类型:"));

        for (HookTier tier : hookItemService.getTiers()) {
            sender.sendMessage(Component.text("- " + tier.id() + " / " + tier.displayName()));
        }
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("leafgrapple.admin")) {
            sender.sendMessage(Component.text("你没有权限使用这个命令。"));
            return;
        }

        LeafGrapplePlugin plugin = LeafGrapplePlugin.getInstance();

        if (plugin == null) {
            sender.sendMessage(Component.text("LeafGrapple 实例不可用，无法重载。"));
            return;
        }

        plugin.reloadPlugin();

        int loaded = hookItemService.getTiers().size();

        sender.sendMessage(Component.text("LeafGrapple 配置已重载。"));
        sender.sendMessage(Component.text("已加载钩爪类型数量: " + loaded));
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(Component.text("LeafGrapple 命令:"));
        sender.sendMessage(Component.text("/" + label + " give <类型> - 获取钩爪"));
        sender.sendMessage(Component.text("/" + label + " list - 查看钩爪类型"));
        sender.sendMessage(Component.text("/" + label + " reload - 重载配置"));
        sender.sendMessage(Component.text("/" + label + " help - 查看帮助"));
    }

    private void sendAvailableTypes(CommandSender sender) {
        List<String> ids = new ArrayList<>();

        for (HookTier tier : hookItemService.getTiers()) {
            ids.add(tier.id());
        }

        if (ids.isEmpty()) {
            sender.sendMessage(Component.text("当前没有可用钩爪类型。"));
            return;
        }

        sender.sendMessage(Component.text("可用类型: " + String.join(", ", ids)));
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>();

            subCommands.add("give");
            subCommands.add("list");
            subCommands.add("help");

            if (sender.hasPermission("leafgrapple.admin")) {
                subCommands.add("reload");
            }

            return filter(subCommands, args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("leafgrapple.admin")) {
                return Collections.emptyList();
            }

            List<String> ids = new ArrayList<>();

            for (HookTier tier : hookItemService.getTiers()) {
                ids.add(tier.id());
            }

            return filter(ids, args[1]);
        }

        return Collections.emptyList();
    }

    private List<String> filter(List<String> input, String prefix) {
        String lowerPrefix = prefix.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();

        for (String value : input) {
            if (value.toLowerCase(Locale.ROOT).startsWith(lowerPrefix)) {
                result.add(value);
            }
        }

        return result;
    }
}
