package me.LeafPixel.leafgrapple.command;

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
import java.util.Arrays;
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
            sender.sendMessage(Component.text("用法: /" + label + " give <wood|iron|gold|netherite>"));
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            handleGive(sender, label, args);
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            handleList(sender);
            return true;
        }

        sender.sendMessage(Component.text("未知子命令。用法: /" + label + " give <wood|iron|gold|netherite>"));
        return true;
    }

    private void handleGive(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("这个命令只能由玩家执行。"));
            return;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("leafgrapple.admin")) {
            player.sendMessage(Component.text("你没有权限使用这个命令。"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("用法: /" + label + " give <wood|iron|gold|netherite>"));
            return;
        }

        String tierId = args[1].toLowerCase(Locale.ROOT);
        HookTier tier = hookItemService.getTier(tierId);

        if (tier == null) {
            player.sendMessage(Component.text("不存在的钩爪类型: " + tierId));
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

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (args.length == 1) {
            return filter(Arrays.asList("give", "list"), args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
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
