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
                getLogger().info("CarbonChatExpansion v1.0.3 enabled");
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
            sender.sendMessage("§6CarbonChatExpansion v1.0.3 §7- Comandos:");
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
     * Accede directamente al togglePF de ChatBubbles
     */
    private boolean isChatBubblesDisabledForPlayer(Player player) {
        try {
            // Método 1: Acceder directamente al plugin ChatBubbles
            org.bukkit.plugin.Plugin chatBubblesPlugin = Bukkit.getPluginManager().getPlugin("ChatBubbles");

            if (chatBubblesPlugin != null) {
                // Acceder al campo togglePF mediante reflexión
                java.lang.reflect.Field togglePFField = chatBubblesPlugin.getClass().getDeclaredField("togglePF");
                togglePFField.setAccessible(true);
                Object togglePF = togglePFField.get(chatBubblesPlugin);

                // Llamar al método getBoolean con el UUID del jugador
                java.lang.reflect.Method getBooleanMethod = togglePF.getClass().getMethod("getBoolean", String.class);
                Boolean isEnabled = (Boolean) getBooleanMethod.invoke(togglePF, player.getUniqueId().toString());

                if (debugMode) {
                    getLogger().info("[DEBUG] ChatBubbles togglePF for " + player.getName() + ": " + (isEnabled ? "ON" : "OFF"));
                }

                // Retorna true si está DESACTIVADO
                return !isEnabled;
            } else {
                if (debugMode) {
                    getLogger().info("[DEBUG] ChatBubbles plugin not found");
                }
            }
        } catch (NoSuchFieldException e) {
            if (debugMode) {
                getLogger().info("[DEBUG] togglePF field not found in ChatBubbles: " + e.getMessage());
            }
        } catch (Exception e) {
            if (debugMode) {
                getLogger().info("[DEBUG] Error accessing ChatBubbles togglePF: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Si no podemos detectar, asumir que está activado (no remover prefijo por seguridad)
        if (debugMode) {
            getLogger().info("[DEBUG] Could not determine ChatBubbles status for " + player.getName() + " - assuming ON");
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
                        getLogger().info("[DEBUG] HIGH: ChatBubbles OFF para " + player.getName());
                        getLogger().info("[DEBUG] HIGH: Removiendo prefijo: '" + message + "' -> '" + cleanMessage + "'");
                    }
                } else {
                    if (debugMode) {
                        getLogger().info("[DEBUG] HIGH: ChatBubbles ON - prefijo mantenido");
                    }
                }
            }
        } catch (Exception e) {
            getLogger().warning("Error in HIGH listener: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("CarbonChatExpansion v1.0.3 disabled");
    }
}
