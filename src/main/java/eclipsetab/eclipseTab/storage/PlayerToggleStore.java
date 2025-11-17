package eclipsetab.eclipseTab.storage;

import eclipsetab.eclipseTab.EclipseTab;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerToggleStore {
    private final EclipseTab plugin;
    private final Map<UUID, Boolean> toggles = new HashMap<>();

    public PlayerToggleStore(EclipseTab plugin) {
        this.plugin = plugin;
    }

    public void load() {
        FileConfiguration cfg = plugin.getConfig();
        if (cfg.isConfigurationSection("toggles")) {
            var section = cfg.getConfigurationSection("toggles");
            if (section == null) return;
            for (String key : section.getKeys(false)) {
                try {
                    UUID id = UUID.fromString(key);
                    boolean v = section.getBoolean(key, true);
                    toggles.put(id, v);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    public void save() {
        if (!plugin.getConfig().getBoolean("persist-toggles", true)) return;
        for (Map.Entry<UUID, Boolean> e : toggles.entrySet()) {
            plugin.getConfig().set("toggles." + e.getKey().toString(), e.getValue());
        }
        plugin.saveConfig();
    }

    public boolean isEnabled(UUID id) {
        return toggles.getOrDefault(id, true);
    }

    public boolean toggle(UUID id) {
        boolean current = toggles.getOrDefault(id, true);
        boolean next = !current;
        toggles.put(id, next);
        return next;
    }
}
