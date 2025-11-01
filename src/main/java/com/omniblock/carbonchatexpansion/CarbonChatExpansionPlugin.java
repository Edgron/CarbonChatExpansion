package com.omniblock.carbonchatexpansion;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class CarbonChatExpansionPlugin extends JavaPlugin implements Listener {

    private CarbonChannelExpansion expansion;
    private static final String BUBBLE_DISABLE_PREFIX = "`";

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getLogger().info("CarbonChat Expansion v6.0 - Iniciado");

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            expansion = new CarbonChannelExpansion(this);
            if (expansion.register()) {
                getLogger().info("Expansión PlaceholderAPI registrada");
            } else {
                getLogger().warning("Error al registrar en PlaceholderAPI");
            }
        } else {
            getLogger().warning("PlaceholderAPI no encontrado - desactivando");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(this, this);
    }

    /**
     * LOWEST Priority - Añade backtick si no debe haber burbuja
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerChatLowest(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (player == null) return;

        try {
            String channelName = expansion.getChannelName(player);
            if (channelName == null || channelName.isEmpty()) {
                return;
            }

            List<String> allowedChannels = getConfig().getStringList("channels-with-bubbles");

            if (!allowedChannels.contains(channelName)) {
                event.setMessage(BUBBLE_DISABLE_PREFIX + event.getMessage());
            }
        } catch (Exception e) {
            getLogger().warning("Error: " + e.getMessage());
        }
    }

    /**
     * HIGH Priority - Remueve el backtick antes de mostrar
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerChatHigh(AsyncPlayerChatEvent event) {
        String mensaje = event.getMessage();
        if (mensaje.startsWith(BUBBLE_DISABLE_PREFIX)) {
            event.setMessage(mensaje.substring(1));
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("CarbonChat Expansion desactivado");
    }
}
