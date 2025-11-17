package eclipsetab.eclipseTab;

import eclipsetab.eclipseTab.commands.EtabCommand;
import eclipsetab.eclipseTab.hooks.LuckPermsHook;
import eclipsetab.eclipseTab.hooks.PlaceholderHook;
import eclipsetab.eclipseTab.storage.PlayerToggleStore;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class EclipseTab extends JavaPlugin {

    private TabManager tabManager;
    private LuckPermsHook luckPermsHook;
    private PlaceholderHook placeholderHook;
    private PlayerToggleStore toggleStore;
    private BukkitTask refreshTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Initialize stores and hooks
        this.toggleStore = new PlayerToggleStore(this);
        this.luckPermsHook = new LuckPermsHook(this);
        this.placeholderHook = new PlaceholderHook(this);

        // Initialize the TabManager
        this.tabManager = new TabManager(this);

        // Register command safely
        PluginCommand bCommand = getCommand("etab");
        if (bCommand != null) {
            var cmd = new EtabCommand(this);
            bCommand.setExecutor(cmd);
            bCommand.setTabCompleter(cmd);
        } else {
            getLogger().warning("Command 'etab' is not defined in plugin.yml. Command registration skipped.");
        }

        // Register events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(tabManager, this);

        // Start scheduled task for refreshing
        int interval = getConfig().getInt("refresh-interval-ticks", 100);
        this.refreshTask = Bukkit.getScheduler().runTaskTimer(this, tabManager::refreshAll, interval, interval);

        getLogger().info("eTab enabled. Refresh interval: " + interval + " ticks.");

        // Load persisted toggles
        if (this.toggleStore != null) this.toggleStore.load();

        // Initial refresh for online players
        Bukkit.getScheduler().runTask(this, tabManager::refreshAll);
    }

    @Override
    public void onDisable() {
        if (refreshTask != null) refreshTask.cancel();
        if (tabManager != null) tabManager.clearAll();
        if (toggleStore != null) toggleStore.save();
        getLogger().info("eTab disabled.");
    }

    public TabManager getTabManager() {
        return tabManager;
    }

    public LuckPermsHook getLuckPermsHook() {
        return luckPermsHook;
    }

    public PlaceholderHook getPlaceholderHook() {
        return placeholderHook;
    }

    public PlayerToggleStore getToggleStore() {
        return toggleStore;
    }
}
