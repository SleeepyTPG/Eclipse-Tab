package eclipsetab.eclipseTab.hooks;

import eclipsetab.eclipseTab.EclipseTab;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LuckPermsHook {
    private final EclipseTab plugin;
    private boolean enabled = false;

    public LuckPermsHook(EclipseTab plugin) {
        this.plugin = plugin;
        try {
            // Check presence without directly referencing LuckPerms classes to remain compilation-safe
            var reg = Bukkit.getServicesManager().getRegistration((Class<?>) Class.forName("net.luckperms.api.LuckPerms"));
            if (reg != null) {
                enabled = true;
                plugin.getLogger().info("LuckPerms hook enabled.");
            } else {
                plugin.getLogger().info("LuckPerms not found. Falling back to Bukkit permissions.");
            }
        } catch (ClassNotFoundException e) {
            plugin.getLogger().info("LuckPerms API not available.");
        } catch (Throwable t) {
            plugin.getLogger().warning("Error checking LuckPerms: " + t.getMessage());
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean hasPermission(Player player, String node) {
        // If LuckPerms is present, Bukkit's permission system is typically synced; use Player#hasPermission as safe fallback.
        return player.hasPermission(node);
    }
}
