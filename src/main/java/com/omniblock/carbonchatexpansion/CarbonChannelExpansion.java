package com.omniblock.carbonchatexpansion;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public class CarbonChannelExpansion extends PlaceholderExpansion {

    private final CarbonChatExpansionPlugin plugin;

    public CarbonChannelExpansion(CarbonChatExpansionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "carbonchat";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Omniblock";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        try {
            String channelName = getChannelName(player);

            if (channelName == null || channelName.isEmpty()) {
                return "";
            }

            if (params.equalsIgnoreCase("channel_color")) {
                String color = getColorForChannel(channelName);
                return color;
            }

            if (params.equalsIgnoreCase("channel_name")) {
                return channelName;
            }

            if (params.equalsIgnoreCase("channel_key")) {
                return "carbon:" + channelName;
            }

        } catch (Exception e) {
            plugin.getLogger().fine("Error: " + e.getMessage());
        }

        return "";
    }

    public String getChannelName(Player player) {
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

            if (carbonPlayer == null) {
                return null;
            }

            Method selectedChannelMethod = carbonPlayer.getClass().getMethod("selectedChannel");
            Object selectedChannel = selectedChannelMethod.invoke(carbonPlayer);

            if (selectedChannel == null) {
                return null;
            }

            Method keyMethod = selectedChannel.getClass().getMethod("key");
            Object key = keyMethod.invoke(selectedChannel);

            Method asStringMethod = key.getClass().getMethod("asString");
            asStringMethod.setAccessible(true);
            String channelKey = (String) asStringMethod.invoke(key);

            if (channelKey != null && channelKey.startsWith("carbon:")) {
                return channelKey.substring(7);
            }

            return channelKey;

        } catch (Exception e) {
            plugin.getLogger().fine("Error obteniendo canal: " + e.getMessage());
            return null;
        }
    }

    private String getColorForChannel(String channelName) {
        String configPath = "channel-colors." + channelName;
        String color = plugin.getConfig().getString(configPath);

        if (color != null && !color.isEmpty()) {
            return color;
        }

        return plugin.getConfig().getString("channel-colors.default", "<white>");
    }
}
