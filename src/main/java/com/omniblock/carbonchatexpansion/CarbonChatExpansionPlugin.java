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
    private static final String BUBBLE_PREFIX = ".";

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
     * LOWEST Priority - Añade "." si el canal debe tener burbuja
     * ChatBubbles (Modo 5) removerá automáticamente el "."
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        try {
            String channelName = expansion.getChannelName(player);
            if (channelName == null || channelName.isEmpty()) {
                return;
            }

            List<String> bubbleChannels = getConfig().getStringList("bubble-channels");
            if (bubbleChannels.contains(channelName)) {
                event.setMessage(BUBBLE_PREFIX + event.getMessage());
            }
        } catch (Exception e) {
            getLogger().warning("Error: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
    }
}
