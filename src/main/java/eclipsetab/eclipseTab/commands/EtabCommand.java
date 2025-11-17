package eclipsetab.eclipseTab.commands;

import eclipsetab.eclipseTab.EclipseTab;
import eclipsetab.eclipseTab.storage.PlayerToggleStore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EtabCommand implements CommandExecutor, TabCompleter {

    private final EclipseTab plugin;

    public EtabCommand(EclipseTab plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§aeTab v" + plugin.getDescription().getVersion());
            sender.sendMessage("§eUse /eTab reload/info/toggle");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "reload":
                if (!hasPermission(sender, "etab.reload")) {
                    sender.sendMessage(color(plugin.getConfig().getString("messages.no-permission","&cYou do not have permission to use this command.")));
                    return true;
                }
                plugin.reloadConfig();
                plugin.getTabManager().clearAll();
                plugin.getTabManager().refreshAll();
                sender.sendMessage(color(plugin.getConfig().getString("messages.reload","&aConfiguration reloaded.")));
                return true;
            case "info":
                sender.sendMessage("§aEclipseTab v" + plugin.getDescription().getVersion());
                sender.sendMessage("PlaceholderAPI: " + (plugin.getPlaceholderHook().isEnabled() ? "§aEnabled" : "§cDisabled"));
                sender.sendMessage("LuckPerms: " + (plugin.getLuckPermsHook().isEnabled() ? "§aEnabled" : "§cDisabled"));
                sender.sendMessage("Refresh interval: §e" + plugin.getConfig().getInt("refresh-interval-ticks", 100) + " ticks");
                return true;
            case "toggle":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can toggle their personal tab updates.");
                    return true;
                }
                Player p = (Player) sender;
                if (!hasPermission(sender, "etab.toggle")) {
                    sender.sendMessage(color(plugin.getConfig().getString("messages.no-permission","&cYou do not have permission to use this command.")));
                    return true;
                }
                PlayerToggleStore store = plugin.getToggleStore();
                boolean enabled = store.toggle(p.getUniqueId());
                sender.sendMessage(color(plugin.getConfig().getString(enabled ? "messages.toggled-on" : "messages.toggled-off", enabled ? "&aTablist updates enabled." : "&cTablist updates disabled.")));
                plugin.getTabManager().sendToPlayer(p);
                return true;
            default:
                sender.sendMessage("Unknown subcommand. Use /eTab reload/info/toggle");
                return true;
        }
    }

    private boolean hasPermission(CommandSender sender, String node) {
        if (sender.hasPermission(node)) return true;
        if (sender instanceof Player) {
            return plugin.getLuckPermsHook().hasPermission((Player) sender, node);
        }
        return sender.isOp();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            if (hasPermission(sender, "etab.reload")) subs.add("reload");
            if (hasPermission(sender, "etab.info")) subs.add("info");
            if (hasPermission(sender, "etab.toggle")) subs.add("toggle");
            return subs;
        }
        return Collections.emptyList();
    }

    private String color(String s) {
        return s == null ? "" : s.replace('&', '§');
    }
}
