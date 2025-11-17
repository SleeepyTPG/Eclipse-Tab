package eclipsetab.eclipseTab;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TabManager implements Listener {

    private final EclipseTab plugin;
    private final MiniMessage mini = MiniMessage.miniMessage();
    private final Map<UUID, String> lastSentHeader = new HashMap<>();
    private final Map<UUID, String> lastSentFooter = new HashMap<>();

    public TabManager(EclipseTab plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        sendToPlayer(p);
    }

    public void refreshAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            try {
                if (!plugin.getToggleStore().isEnabled(p.getUniqueId())) continue; // respect toggle
                sendToPlayer(p);
            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to refresh tab for " + p.getName() + ": " + ex.getMessage());
            }
        }
    }

    public void sendToPlayer(Player p) {
        String header = plugin.getConfig().getString("header", "");
        String footer = plugin.getConfig().getString("footer", "");

        // Determine format by permission - simple approach: pick first matching high-priority format
        var formats = plugin.getConfig().getConfigurationSection("formats");
        if (formats != null) {
            var keys = formats.getKeys(false);
            int bestPriority = Integer.MIN_VALUE;
            for (String key : keys) {
                var section = formats.getConfigurationSection(key);
                if (section == null) continue;
                String perm = section.getString("permission", "");
                int priority = section.getInt("priority", 0);
                if (!perm.isEmpty()) {
                    boolean has = plugin.getLuckPermsHook().hasPermission(p, perm);
                    if (!has) continue;
                }
                if (priority >= bestPriority) {
                    bestPriority = priority;
                    header = section.getString("header", header);
                    footer = section.getString("footer", footer);
                }
            }
        }

        // Apply placeholders
        if (plugin.getPlaceholderHook().isEnabled()) {
            header = plugin.getPlaceholderHook().applyPlaceholders(p, header);
            footer = plugin.getPlaceholderHook().applyPlaceholders(p, footer);
        } else {
            header = header.replace("<player>", p.getName());
            header = header.replace("<player_count>", String.valueOf(Bukkit.getOnlinePlayers().size()));
            footer = footer.replace("<player>", p.getName());
            footer = footer.replace("<player_count>", String.valueOf(Bukkit.getOnlinePlayers().size()));
        }

        // Process gradients via FormatParser
        header = FormatParser.resolveGradients(plugin, header);
        footer = FormatParser.resolveGradients(plugin, footer);

        // If nothing changed, skip sending to reduce load
        UUID id = p.getUniqueId();
        if (header.equals(lastSentHeader.get(id)) && footer.equals(lastSentFooter.get(id))) return;

        lastSentHeader.put(id, header);
        lastSentFooter.put(id, footer);

        // Send header/footer using Adventure -> via reflection helper
        var componentHeader = mini.deserialize(header);
        var componentFooter = mini.deserialize(footer);

        PlayerListUtil.sendHeaderFooter(p, componentHeader, componentFooter, plugin);
    }

    public void clearAll() {
        var empty = mini.deserialize("");
        for (Player p : Bukkit.getOnlinePlayers()) {
            try {
                PlayerListUtil.sendHeaderFooter(p, empty, empty, plugin);
            } catch (Exception ignored) {
            }
        }
    }
}
