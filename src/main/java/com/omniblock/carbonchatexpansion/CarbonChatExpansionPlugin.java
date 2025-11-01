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

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            expansion = new CarbonChannelExpansion(this);
            if (expansion.register()) {
                getLogger().info("CarbonChat Expansion iniciado correctamente");
            } else {
                getLogger().warning("Error al registrar en PlaceholderAPI");
            }
        } else {
            getLogger().warning("PlaceholderAPI no encontrado");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(this, this);
    }

    /**
     * Obtiene el prefijo configurado para deshabilitar burbujas
     */
    private String getBubbleDisablePrefix() {
        return getConfig().getString("bubble-disable-prefix", "!");
    }

    /**
     * LOWEST Priority - Añade prefijo si no debe haber burbuja
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
                event.setMessage(getBubbleDisablePrefix() + event.getMessage());
            }
        } catch (Exception e) {
            getLogger().warning("Error en LOWEST: " + e.getMessage());
        }
    }

    /**
     * HIGH Priority - Remueve el prefijo antes de mostrar
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerChatHigh(AsyncPlayerChatEvent event) {
        String prefix = getBubbleDisablePrefix();
        String mensaje = event.getMessage();
        if (mensaje.startsWith(prefix)) {
            event.setMessage(mensaje.substring(prefix.length()));
        }
    }

    @Override
    public void onDisable() {
    }
}
