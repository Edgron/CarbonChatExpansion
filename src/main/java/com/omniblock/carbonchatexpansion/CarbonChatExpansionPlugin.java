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
                getLogger().info("CarbonChatExpansion v1.0 enabled");
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
            sender.sendMessage("§6CarbonChatExpansion v1.0 §7- Comandos:");
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
                String originalMessage = event.getMessage();
                String newMessage = getBubblePrefix() + originalMessage;
                event.setMessage(newMessage);

                if (debugMode) {
                    getLogger().info("[DEBUG] Canal: " + channelName + " | Mensaje: '" + originalMessage + "' -> '" + newMessage + "'");
                }
            }
        } catch (Exception e) {
            getLogger().warning("Error: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("CarbonChatExpansion v1.0 disabled");
    }
}
