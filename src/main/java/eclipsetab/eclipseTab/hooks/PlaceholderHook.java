package eclipsetab.eclipseTab.hooks;

import eclipsetab.eclipseTab.EclipseTab;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlaceholderHook {
    private final EclipseTab plugin;
    private boolean enabled = false;

    public PlaceholderHook(EclipseTab plugin) {
        this.plugin = plugin;
        try {
            var reg = Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI");
            if (reg != null && reg.isEnabled()) {
                enabled = true;
                plugin.getLogger().info("PlaceholderAPI hook enabled.");
            } else {
                plugin.getLogger().info("PlaceholderAPI not found. Placeholders will be limited.");
            }
        } catch (Throwable t) {
            plugin.getLogger().info("PlaceholderAPI not available.");
        }
    }

    public boolean isEnabled() {
        return enabled && plugin.getConfig().getBoolean("enable-papi", true);
    }

    public String applyPlaceholders(Player player, String input) {
        if (!isEnabled()) return input;
        try {
            // Call PlaceholderAPI.setPlaceholders(player, input) via reflection to avoid compile-time dependency
            Class<?> papi = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            var method = papi.getMethod("setPlaceholders", org.bukkit.OfflinePlayer.class, String.class);
            Object res = method.invoke(null, player, input);
            if (res instanceof String) return (String) res;
            return input;
        } catch (ClassNotFoundException cnf) {
            plugin.getLogger().warning("PlaceholderAPI class not found when applying placeholders.");
            return input;
        } catch (Throwable t) {
            plugin.getLogger().warning("PlaceholderAPI expansion failed: " + t.getMessage());
            return input;
        }
    }
}
