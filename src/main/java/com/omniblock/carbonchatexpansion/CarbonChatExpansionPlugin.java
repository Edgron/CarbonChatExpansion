package com.omniblock.carbonchatexpansion;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarbonChatExpansionPlugin extends JavaPlugin implements Listener, CommandExecutor {

    private CarbonChannelExpansion expansion;
    private boolean debugMode = false;

    /**
     * Clase interna para almacenar el estado en caché
     */
    private static class CachedToggle {
        final boolean disabled;
        final long timestamp;

        CachedToggle(boolean disabled) {
            this.disabled = disabled;
            this.timestamp = System.currentTimeMillis();
        }

        /**
         * Verifica si el caché ha expirado
         */
        boolean isExpired(long cacheMillis) {
            return System.currentTimeMillis() - timestamp > cacheMillis;
        }
    }

    // Caché sincronizado (thread-safe)
    private final Map<String, CachedToggle> toggleCache = 
        Collections.synchronizedMap(new HashMap<>());

    // Tiempo de caché en milisegundos (10 segundos)
    private static final long CACHE_DURATION_MS = 10000L;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        debugMode = getConfig().getBoolean("debug-mode", false);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            expansion = new CarbonChannelExpansion(this);
            if (expansion.register()) {
                getLogger().info("CarbonChatExpansion v1.0.4 enabled");
            } else {
                getLogger().warning("Failed to register PlaceholderAPI expansion");
            }
        } else {
            getLogger().warning("PlaceholderAPI not found - disabling");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("carbonchatexpansion").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("carbonchatexpansion.admin")) {
            sender.sendMessage("§cNo tienes permiso para usar este comando.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§6CarbonChatExpansion v1.0.4 §7- Comandos:");
            sender.sendMessage("§7- §e/cce reload §7- Recarga la configuración");
            sender.sendMessage("§7- §e/cce debug <on|off> §7- Activa/desactiva debug");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                reloadConfig();
                debugMode = getConfig().getBoolean("debug-mode", false);
                toggleCache.clear();
                sender.sendMessage("§aConfiguración recargada correctamente.");
                return true;

            case "debug":
                if (args.length < 2) {
                    sender.sendMessage("§cUso: /cce debug <on|off>");
                    return true;
                }

                if (args[1].equalsIgnoreCase("on")) {
                    debugMode = true;
                    getConfig().set("debug-mode", true);
                    saveConfig();
                    sender.sendMessage("§aModo debug activado.");
                } else if (args[1].equalsIgnoreCase("off")) {
                    debugMode = false;
                    getConfig().set("debug-mode", false);
                    saveConfig();
                    sender.sendMessage("§cModo debug desactivado.");
                } else {
                    sender.sendMessage("§cUso: /cce debug <on|off>");
                }
                return true;

            default:
                sender.sendMessage("§cComando desconocido. Usa /cce para ver la ayuda.");
                return true;
        }
    }

    private String getBubblePrefix() {
        return getConfig().getString("bubble-prefix", ".");
    }

    /**
     * Detecta si ChatBubbles está desactivado para el jugador
     * Utiliza caché sincronizado para máxima eficiencia
     */
    private boolean isChatBubblesDisabledForPlayer(Player player) {
        String uuid = player.getUniqueId().toString();

        // 1. Revisar caché primero (muy rápido)
        synchronized (toggleCache) {
            if (toggleCache.containsKey(uuid)) {
                CachedToggle cached = toggleCache.get(uuid);
                if (!cached.isExpired(CACHE_DURATION_MS)) {
                    if (debugMode) {
                        getLogger().info("[DEBUG] Cache hit for " + player.getName() + ": " + (cached.disabled ? "OFF" : "ON"));
                    }
                    return cached.disabled;
                }
                // El caché expiró, removerlo
                toggleCache.remove(uuid);
            }
        }

        // 2. Si no está en caché, hacer reflexión
        try {
            org.bukkit.plugin.Plugin chatBubblesPlugin = 
                Bukkit.getPluginManager().getPlugin("ChatBubbles");

            if (chatBubblesPlugin != null) {
                java.lang.reflect.Field togglePFField = 
                    chatBubblesPlugin.getClass().getDeclaredField("togglePF");
                togglePFField.setAccessible(true);
                Object togglePF = togglePFField.get(chatBubblesPlugin);

                java.lang.reflect.Method getBooleanMethod = 
                    togglePF.getClass().getMethod("getBoolean", String.class);
                Boolean isEnabled = (Boolean) getBooleanMethod.invoke(togglePF, uuid);

                // 3. Guardar en caché (thread-safe)
                boolean disabled = !isEnabled;
                synchronized (toggleCache) {
                    toggleCache.put(uuid, new CachedToggle(disabled));
                }

                if (debugMode) {
                    getLogger().info("[DEBUG] Reflection for " + player.getName() + ": " + (disabled ? "OFF" : "ON"));
                }

                return disabled;
            } else {
                if (debugMode) {
                    getLogger().info("[DEBUG] ChatBubbles plugin not found");
                }
            }
        } catch (NoSuchFieldException e) {
            if (debugMode) {
                getLogger().info("[DEBUG] togglePF field not found: " + e.getMessage());
            }
        } catch (Exception e) {
            if (debugMode) {
                getLogger().warning("[DEBUG] Error accessing togglePF: " + e.getMessage());
            }
        }

        return false;
    }

    /**
     * LOWEST Priority - Añade prefijo SIEMPRE si el canal requiere burbujas
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

            List<String> bubbleChannels = getConfig().getStringList("bubble-channels");

            if (bubbleChannels.contains(channelName)) {
                String originalMessage = event.getMessage();
                String prefix = getBubblePrefix();
                String newMessage = prefix + originalMessage;
                event.setMessage(newMessage);

                if (debugMode) {
                    getLogger().info("[DEBUG] LOWEST: Canal '" + channelName + "' - '" + originalMessage + "' -> '" + newMessage + "'");
                }
            }
        } catch (Exception e) {
            getLogger().warning("Error in LOWEST listener: " + e.getMessage());
        }
    }

    /**
     * HIGH Priority - Remueve el prefijo SI ChatBubbles está OFF
     * Se ejecuta DESPUÉS de ChatBubbles para limpiar el prefijo si es necesario
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerChatHigh(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        try {
            String channelName = expansion.getChannelName(player);
            if (channelName == null || channelName.isEmpty()) {
                return;
            }

            List<String> bubbleChannels = getConfig().getStringList("bubble-channels");
            String prefix = getBubblePrefix();
            String message = event.getMessage();

            if (bubbleChannels.contains(channelName) && message.startsWith(prefix)) {
                if (isChatBubblesDisabledForPlayer(player)) {
                    String cleanMessage = message.substring(prefix.length());
                    event.setMessage(cleanMessage);

                    if (debugMode) {
                        getLogger().info("[DEBUG] HIGH: Removed prefix for " + player.getName());
                    }
                }
            }
        } catch (Exception e) {
            getLogger().warning("Error in HIGH listener: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        toggleCache.clear();
        getLogger().info("CarbonChatExpansion v1.0.4 disabled");
    }
}
