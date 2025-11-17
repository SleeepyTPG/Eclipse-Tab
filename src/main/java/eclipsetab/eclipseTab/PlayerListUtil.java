package eclipsetab.eclipseTab;

import org.bukkit.entity.Player;

public class PlayerListUtil {
    // Use reflection to call available methods for setting the player's tab header/footer to be compatible
    // with different server API versions.
    public static void sendHeaderFooter(Player p, Object headerComp, Object footerComp, EclipseTab plugin) {
        try {
            Class<?> compClass = null;
            try {
                compClass = Class.forName("net.kyori.adventure.text.Component");
            } catch (ClassNotFoundException ignored) {
            }

            // Try playerListHeader(Component) / playerListFooter(Component)
            if (compClass != null) {
                try {
                    var m1 = p.getClass().getMethod("playerListHeader", compClass);
                    var m2 = p.getClass().getMethod("playerListFooter", compClass);
                    m1.invoke(p, headerComp);
                    m2.invoke(p, footerComp);
                    return;
                } catch (NoSuchMethodException ignored) {
                }
            }

            // Try setPlayerListHeaderFooter(String, String)
            try {
                var m3 = p.getClass().getMethod("setPlayerListHeaderFooter", String.class, String.class);
                m3.invoke(p, String.valueOf(headerComp), String.valueOf(footerComp));
                return;
            } catch (NoSuchMethodException ignored) {
            }

            // Try setPlayerListHeader(String) and setPlayerListFooter(String)
            try {
                var m4 = p.getClass().getMethod("setPlayerListHeader", String.class);
                var m5 = p.getClass().getMethod("setPlayerListFooter", String.class);
                m4.invoke(p, String.valueOf(headerComp));
                m5.invoke(p, String.valueOf(footerComp));
                return;
            } catch (NoSuchMethodException ignored) {
            }

            // Nothing available; log once
            plugin.getLogger().warning("No supported method found to set player list header/footer for player: " + p.getName());
        } catch (Throwable t) {
            plugin.getLogger().warning("Failed to set player list header/footer: " + t.getMessage());
        }
    }
}
