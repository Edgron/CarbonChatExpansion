package com.omniblock.carbonchatexpansion;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class CarbonChatExpansionPlugin extends JavaPlugin implements Listener, CommandExecutor {

    private CarbonChannelExpansion expansion;
    private boolean debugMode = false;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        debugMode = getConfig().getBoolean("debug-mode", false);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            expansion = new CarbonChannelExpansion(this);
            if (expansion.register()) {
                getLogger().info("CarbonChatExpansion v1.0.2 enabled");
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
            sender.sendMessage("§6CarbonChatExpansion v1.0.2 §7- Comandos:");
            sender.sendMessage("§7- §e/cce reload §7- Recarga la configuración");
            sender.sendMessage("§7- §e/cce debug <on|off> §7- Activa/desactiva debug");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                reloadConfig();
                debugMode = getConfig().getBoolean("debug-mode", false);
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
     * Usa múltiples métodos de detección para máxima compatibilidad
     */
    private boolean isChatBubblesDisabledForPlayer(Player player) {
        // Método 1: Intenta usar ChatBubbles API directamente
        try {
            Class<?> apiClass = Class.forName("me.neznamy.chatbubbles.api.ChatBubblesAPI");
            java.lang.reflect.Method method = apiClass.getMethod("isEnabled", Player.class);
            Object result = method.invoke(null, player);

            if (result instanceof Boolean) {
                boolean isEnabled = (Boolean) result;
                if (debugMode) {
                    getLogger().info("[DEBUG] ChatBubbles API status for " + player.getName() + ": " + (isEnabled ? "ON" : "OFF"));
                }
                return !isEnabled;
            }
        } catch (ClassNotFoundException e) {
            if (debugMode) {
                getLogger().info("[DEBUG] ChatBubbles API class not found - trying alternative");
            }
        } catch (Exception e) {
            if (debugMode) {
                getLogger().info("[DEBUG] Error with API method: " + e.getMessage());
            }
        }

        // Método 2: Revisar metadata del jugador
        try {
            if (player.hasMetadata("chatbubble_disabled")) {
                boolean disabled = player.getMetadata("chatbubble_disabled").get(0).asBoolean();
                if (debugMode) {
                    getLogger().info("[DEBUG] ChatBubbles metadata for " + player.getName() + ": " + (disabled ? "OFF" : "ON"));
                }
                return disabled;
            }
        } catch (Exception e) {
            if (debugMode) {
                getLogger().info("[DEBUG] No metadata found: " + e.getMessage());
            }
        }

        // Si no podemos detectar, asumir que está activado (no remover prefijo)
        if (debugMode) {
            getLogger().info("[DEBUG] Could not detect ChatBubbles status for " + player.getName() + " - assuming ON");
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
                    getLogger().info("[DEBUG] LOWEST: Canal '" + channelName + "' requiere burbujas");
                    getLogger().info("[DEBUG] LOWEST: '" + originalMessage + "' -> '" + newMessage + "'");
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

            // Solo procesar si:
            // 1. El canal requiere burbujas
            // 2. El mensaje aún tiene el prefijo
            if (bubbleChannels.contains(channelName) && message.startsWith(prefix)) {

                // Verificar si ChatBubbles está desactivado
                if (isChatBubblesDisabledForPlayer(player)) {
                    String cleanMessage = message.substring(prefix.length());
                    event.setMessage(cleanMessage);

                    if (debugMode) {
                        getLogger().info("[DEBUG] HIGH: ChatBubbles está OFF para " + player.getName());
                        getLogger().info("[DEBUG] HIGH: Removiendo prefijo: '" + message + "' -> '" + cleanMessage + "'");
                    }
                } else {
                    if (debugMode) {
                        getLogger().info("[DEBUG] HIGH: ChatBubbles está ON - prefijo mantenido para procesamiento");
                    }
                }
            }
        } catch (Exception e) {
            getLogger().warning("Error in HIGH listener: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("CarbonChatExpansion v1.0.2 disabled");
    }
}
