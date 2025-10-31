package com.omniblock.carbonchatexpansion;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CarbonChatExpansionPlugin extends JavaPlugin implements Listener {

    private CarbonChannelExpansion expansion;
    private String bubbleDisablePrefix;
    private boolean debugMode = false;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfiguration();

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            expansion = new CarbonChannelExpansion(this);
            if (expansion.register()) {
                log("✅ Expansión registrada en PlaceholderAPI");
            } else {
                log("❌ Error al registrar en PlaceholderAPI");
            }
        } else {
            log("❌ PlaceholderAPI no encontrado!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(this, this);
        log("✅ CarbonChat Expansion iniciado correctamente");
    }

    private void loadConfiguration() {
        reloadConfig();
        bubbleDisablePrefix = getConfig().getString("bubble-disable-prefix", "¸");
        log("📋 Configuración cargada - Prefijo: '" + bubbleDisablePrefix + "'");
    }

    private void log(String message) {
        getLogger().info(message);
    }

    private void debugLog(String message) {
        if (debugMode) {
            getLogger().info("[DEBUG] " + message);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerChatLowest(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String mensaje = event.getMessage();

        if (player == null) return;

        debugLog("📨 [LOWEST] Chat detectado - Jugador: " + player.getName() + " | Mensaje: " + mensaje);

        try {
            String channelName = expansion.getChannelName(player);
            debugLog("🔍 Canal detectado: " + (channelName == null ? "NULL" : channelName));

            if (channelName == null || channelName.isEmpty()) {
                debugLog("⚠️  No se pudo obtener el canal");
                return;
            }

            List<String> allowedChannels = getConfig().getStringList("channels-with-bubbles");
            debugLog("📋 Canales con burbujas: " + allowedChannels);

            if (!allowedChannels.contains(channelName)) {
                String newMessage = bubbleDisablePrefix + mensaje;
                event.setMessage(newMessage);
                debugLog("❌ NO debe haber burbuja - Mensaje modificado");
            } else {
                debugLog("✅ SÍ permite burbujas");
            }

        } catch (Exception e) {
            log("❌ Error en LOWEST listener:");
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerChatHigh(AsyncPlayerChatEvent event) {
        String mensaje = event.getMessage();

        if (mensaje.startsWith(bubbleDisablePrefix)) {
            String mensajeSinPrefijo = mensaje.substring(1);
            event.setMessage(mensajeSinPrefijo);
            debugLog("🧹 [HIGH] Prefijo removido - Antes: '" + mensaje + "' | Después: '" + mensajeSinPrefijo + "'");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("cbereload")) {
            if (!sender.hasPermission("carbonchatexpansion.reload")) {
                sender.sendMessage("§c❌ No tienes permiso para usar este comando");
                return true;
            }

            loadConfiguration();
            sender.sendMessage("§a✅ Configuración recargada correctamente");
            log("Configuración recargada por " + sender.getName());
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("cbedebug")) {
            if (!sender.hasPermission("carbonchatexpansion.debug")) {
                sender.sendMessage("§c❌ No tienes permiso para usar este comando");
                return true;
            }

            debugMode = !debugMode;
            String estado = debugMode ? "§a✅ ACTIVADO" : "§c❌ DESACTIVADO";
            sender.sendMessage("§e🐛 Modo Debug " + estado);
            log("Modo Debug " + (debugMode ? "ACTIVADO" : "DESACTIVADO") + " por " + sender.getName());
            return true;
        }

        return false;
    }

    @Override
    public void onDisable() {
        log("CarbonChat Expansion desactivado");
    }

    public boolean isDebugMode() {
        return debugMode;
    }
}
