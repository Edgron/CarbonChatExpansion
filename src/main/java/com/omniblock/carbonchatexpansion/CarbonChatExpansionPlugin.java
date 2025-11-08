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
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class CarbonChatExpansionPlugin extends JavaPlugin implements Listener, CommandExecutor {

    private CarbonChannelExpansion expansion;
    private boolean debugMode = false;

    // Cache de reflexión para optimización
    private Object cachedDecentImpl = null;
    private Field cachedHologramsField = null;
    private Method cachedHideAllMethod = null;
    private Method cachedSetShowPlayerMethod = null;

    // Anti-spam para party chat
    private final Map<UUID, Long> lastPartyChatTime = new HashMap<>();
    private static final long PARTY_CHAT_COOLDOWN = 100L; // 100ms entre mensajes

    @Override
    public void onEnable() {
        saveDefaultConfig();
        debugMode = getConfig().getBoolean("debug-mode", false);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            expansion = new CarbonChannelExpansion(this);
            if (expansion.register()) {
                getLogger().info("CarbonChatExpansion v1.0.6 enabled");
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

        // Limpieza periódica de cooldown map
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            long now = System.currentTimeMillis();
            lastPartyChatTime.entrySet().removeIf(entry -> (now - entry.getValue()) > 60000);
        }, 1200L, 1200L); // Cada 60 segundos
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("carbonchatexpansion.admin")) {
            sender.sendMessage("§cNo tienes permiso para usar este comando.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§6CarbonChatExpansion v1.0.6 §7- Comandos:");
            sender.sendMessage("§7- §e/cce reload §7- Recarga la configuración");
            sender.sendMessage("§7- §e/cce debug <on|off> §7- Activa/desactiva debug");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                reloadConfig();
                debugMode = getConfig().getBoolean("debug-mode", false);
                // Clear reflection cache
                cachedDecentImpl = null;
                cachedHologramsField = null;
                cachedHideAllMethod = null;
                cachedSetShowPlayerMethod = null;
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
     */
    private boolean isChatBubblesDisabledForPlayer(Player player) {
        try {
            org.bukkit.plugin.Plugin chatBubblesPlugin = Bukkit.getPluginManager().getPlugin("ChatBubbles");

            if (chatBubblesPlugin != null) {
                java.lang.reflect.Field togglePFField = chatBubblesPlugin.getClass().getDeclaredField("togglePF");
                togglePFField.setAccessible(true);
                Object togglePF = togglePFField.get(chatBubblesPlugin);

                java.lang.reflect.Method getBooleanMethod = togglePF.getClass().getMethod("getBoolean", String.class);
                Boolean isEnabled = (Boolean) getBooleanMethod.invoke(togglePF, player.getUniqueId().toString());

                if (debugMode) {
                    getLogger().info("[DEBUG] ChatBubbles togglePF for " + player.getName() + ": " + (isEnabled ? "ON" : "OFF"));
                }

                return !isEnabled;
            }
        } catch (Exception e) {
            if (debugMode) {
                getLogger().info("[DEBUG] Error accessing ChatBubbles togglePF: " + e.getMessage());
            }
        }

        return false;
    }

    /**
     * Obtener party del jugador mediante CarbonChat API
     */
    private Object getPlayerParty(Player player) {
        try {
            Class<?> carbonChatProviderClass = Class.forName("net.draycia.carbon.api.CarbonChatProvider");
            Method carbonChatMethod = carbonChatProviderClass.getMethod("carbonChat");
            Object carbonChat = carbonChatMethod.invoke(null);

            Method userManagerMethod = carbonChat.getClass().getMethod("userManager");
            Object userManager = userManagerMethod.invoke(carbonChat);

            Method userMethod = userManager.getClass().getMethod("user", java.util.UUID.class);
            Object userCompletableFuture = userMethod.invoke(userManager, player.getUniqueId());

            Method joinMethod = userCompletableFuture.getClass().getMethod("join");
            Object carbonPlayer = joinMethod.invoke(userCompletableFuture);

            if (carbonPlayer == null) return null;

            Method partyMethod = carbonPlayer.getClass().getMethod("party");
            return partyMethod.invoke(carbonPlayer);

        } catch (Exception e) {
            if (debugMode) {
                getLogger().warning("[DEBUG] Error getting player party: " + e.getMessage());
            }
            return null;
        }
    }

    /**
     * Obtener miembros de la party
     */
    private Set<Player> getPartyMembers(Object party) {
        Set<Player> members = new HashSet<>();
        if (party == null) return members;

        try {
            Method membersMethod = party.getClass().getMethod("members");
            @SuppressWarnings("unchecked")
            Set<UUID> memberIds = (Set<UUID>) membersMethod.invoke(party);

            for (UUID memberId : memberIds) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && member.isOnline()) {
                    members.add(member);
                }
            }
        } catch (Exception e) {
            if (debugMode) {
                getLogger().warning("[DEBUG] Error getting party members: " + e.getMessage());
            }
        }

        return members;
    }

    /**
     * Obtener identity hashcode de la party (ÚNICO E INMUTABLE)
     */
    private int getPartyHashCode(Object party) {
        if (party == null) return -1;
        return System.identityHashCode(party);
    }

    /**
     * LOWEST Priority - Filtrar recipients para party chat
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPartyChatFilter(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        if (sender == null) return;

        try {
            String channelName = expansion.getChannelName(sender);
            if (channelName == null || channelName.isEmpty()) return;

            List<String> partyChannels = getConfig().getStringList("party-channels");

            // Solo procesar si es un canal de party
            if (partyChannels.contains(channelName)) {
                // Anti-spam check
                long now = System.currentTimeMillis();
                Long last = lastPartyChatTime.get(sender.getUniqueId());
                if (last != null && (now - last) < PARTY_CHAT_COOLDOWN) {
                    if (debugMode) {
                        getLogger().info("[DEBUG] Party chat cooldown for " + sender.getName());
                    }
                    return;
                }
                lastPartyChatTime.put(sender.getUniqueId(), now);

                // Obtener party del sender
                Object senderParty = getPlayerParty(sender);
                if (senderParty == null) {
                    if (debugMode) {
                        getLogger().info("[DEBUG] " + sender.getName() + " is not in a party");
                    }
                    return;
                }

                int partyHashCode = getPartyHashCode(senderParty);
                if (partyHashCode == -1) return;

                // Filtrar recipients: solo party members
                Set<Player> partyMembers = getPartyMembers(senderParty);
                event.getRecipients().clear();
                event.getRecipients().addAll(partyMembers);

                // Guardar party hashcode para el listener MONITOR
                sender.setMetadata("cce_party_hash", new FixedMetadataValue(this, partyHashCode));
                sender.setMetadata("cce_party_channel", new FixedMetadataValue(this, channelName));

                if (debugMode) {
                    getLogger().info("[DEBUG] LOWEST: Party chat filtered for " + sender.getName());
                    getLogger().info("[DEBUG] LOWEST: Party hashcode: " + partyHashCode);
                    getLogger().info("[DEBUG] LOWEST: Party members: " + partyMembers.size());
                }
            }

            // Añadir prefijo para bubble channels (comportamiento normal)
            List<String> bubbleChannels = getConfig().getStringList("bubble-channels");
            if (bubbleChannels.contains(channelName)) {
                String originalMessage = event.getMessage();
                String prefix = getBubblePrefix();
                String newMessage = prefix + originalMessage;
                event.setMessage(newMessage);

                if (debugMode) {
                    getLogger().info("[DEBUG] LOWEST: Bubble prefix added for '" + channelName + "'");
                }
            }
        } catch (Exception e) {
            getLogger().warning("Error in LOWEST listener: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * MONITOR Priority - Programar filtrado de hologram
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPartyChatMonitor(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        if (sender == null) return;

        if (!sender.hasMetadata("cce_party_hash")) return;

        try {
            int partyHashCode = sender.getMetadata("cce_party_hash").get(0).asInt();

            if (debugMode) {
                getLogger().info("[DEBUG] MONITOR: Scheduling hologram filter for " + sender.getName());
                getLogger().info("[DEBUG] MONITOR: Party hashcode: " + partyHashCode);
            }

            // Programar filtrado de hologram (2 ticks después)
            Bukkit.getScheduler().runTaskLater(this, () -> {
                filterHologramVisibility(sender, partyHashCode);

                // Cleanup metadata
                sender.removeMetadata("cce_party_hash", this);
                sender.removeMetadata("cce_party_channel", this);
            }, 2L);

        } catch (Exception e) {
            if (debugMode) {
                getLogger().warning("[DEBUG] Error in MONITOR listener: " + e.getMessage());
            }
            // Cleanup en caso de error
            sender.removeMetadata("cce_party_hash", this);
            sender.removeMetadata("cce_party_channel", this);
        }
    }

    /**
     * Obtener DecentHologramsImplementation mediante reflexión dinámica
     */
    private Object getDecentHologramsImplementation(org.bukkit.plugin.Plugin chatBubblesPlugin) {
        try {
            if (cachedDecentImpl != null) {
                return cachedDecentImpl;
            }

            // Buscar el campo que contiene DecentHologramsImplementation
            Field[] allFields = chatBubblesPlugin.getClass().getDeclaredFields();

            for (Field field : allFields) {
                String fieldTypeName = field.getType().getName();

                // Buscar por nombre de clase
                if (fieldTypeName.contains("DecentHologramsImplementation")) {
                    field.setAccessible(true);
                    Object impl = field.get(chatBubblesPlugin);

                    if (impl != null) {
                        cachedDecentImpl = impl;
                        if (debugMode) {
                            getLogger().info("[DEBUG] Found DecentHologramsImplementation in field: " + field.getName());
                        }
                        return impl;
                    }
                }
            }

            if (debugMode) {
                getLogger().warning("[DEBUG] Could not find DecentHologramsImplementation in ChatBubbles fields");
            }
            return null;

        } catch (Exception e) {
            if (debugMode) {
                getLogger().warning("[DEBUG] Error finding DecentHologramsImplementation: " + e.getMessage());
            }
            return null;
        }
    }

    /**
     * Filtrar visibilidad del hologram via DecentHolograms API
     */
    private void filterHologramVisibility(Player sender, int partyHashCode) {
        try {
            org.bukkit.plugin.Plugin chatBubblesPlugin = Bukkit.getPluginManager().getPlugin("ChatBubbles");
            if (chatBubblesPlugin == null) {
                if (debugMode) {
                    getLogger().warning("[DEBUG] ChatBubbles plugin not found");
                }
                return;
            }

            // Obtener DecentHologramsImplementation dinámicamente
            Object decentImpl = getDecentHologramsImplementation(chatBubblesPlugin);
            if (decentImpl == null) {
                if (debugMode) {
                    getLogger().warning("[DEBUG] DecentHolograms implementation not found");
                }
                return;
            }

            // Obtener existingHolograms map
            if (cachedHologramsField == null) {
                cachedHologramsField = decentImpl.getClass().getDeclaredField("existingHolograms");
                cachedHologramsField.setAccessible(true);
            }

            @SuppressWarnings("unchecked")
            Map<UUID, List<?>> existingHolograms = (Map<UUID, List<?>>) cachedHologramsField.get(decentImpl);

            // Obtener el hologram recién creado
            List<?> senderHolograms = existingHolograms.get(sender.getUniqueId());
            if (senderHolograms == null || senderHolograms.isEmpty()) {
                if (debugMode) {
                    getLogger().info("[DEBUG] No holograms found for " + sender.getName());
                }
                return;
            }

            // El hologram más reciente
            Object hologram = senderHolograms.get(senderHolograms.size() - 1);

            // Cache de métodos
            if (cachedHideAllMethod == null) {
                cachedHideAllMethod = hologram.getClass().getMethod("hideAll");
            }
            if (cachedSetShowPlayerMethod == null) {
                cachedSetShowPlayerMethod = hologram.getClass().getMethod("setShowPlayer", Player.class);
            }

            // Ocultar para todos
            cachedHideAllMethod.invoke(hologram);

            if (debugMode) {
                getLogger().info("[DEBUG] Hologram hidden for all players");
            }

            // Mostrar solo a party members
            Object party = getPlayerParty(sender);
            if (party != null && System.identityHashCode(party) == partyHashCode) {
                Set<Player> partyMembers = getPartyMembers(party);
                for (Player member : partyMembers) {
                    cachedSetShowPlayerMethod.invoke(hologram, member);
                }

                if (debugMode) {
                    getLogger().info("[DEBUG] Hologram visible to " + partyMembers.size() + " party members");
                }
            } else {
                if (debugMode) {
                    getLogger().warning("[DEBUG] Party reference changed or invalid");
                }
            }

        } catch (NoSuchFieldException e) {
            if (debugMode) {
                getLogger().warning("[DEBUG] Field not found in DecentHologramsImplementation: " + e.getMessage());
            }
        } catch (Exception e) {
            if (debugMode) {
                getLogger().warning("[DEBUG] Error filtering hologram visibility: " + e.getMessage());
            }
        }
    }

    /**
     * HIGH Priority - Remueve el prefijo SI ChatBubbles está OFF
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
                        getLogger().info("[DEBUG] HIGH: ChatBubbles OFF - Prefix removed");
                    }
                }
            }
        } catch (Exception e) {
            getLogger().warning("Error in HIGH listener: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        lastPartyChatTime.clear();
        cachedDecentImpl = null;
        getLogger().info("CarbonChatExpansion v1.0.6 disabled");
    }
}
