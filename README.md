# CarbonChatExpansion v1.0

**Created for Omniblock by Edgron**

CarbonChatExpansion is a bridge plugin that connects **CarbonChat** and **ChatBubbles**, enhancing the chat experience by adding colored chat bubbles per channel and providing flexible control over which channels display bubbles.

---

## 🎯 Features

### 1. PlaceholderAPI Integration
Provides a placeholder for CarbonChat channels that can be used to colorize chat bubbles:

- **`%carbonchat_channel_color%`** - Returns the configured HEX color for the current channel
- **`%carbonchat_channel_name%`** - Returns the current channel name
- **`%carbonchat_channel_key%`** - Returns the full channel key (e.g., `carbon:global`)

**Use Case:** The color placeholder is designed to be used in the **ChatBubbles message format**, not for player names. This allows bubbles to have different colors based on the channel being used.

### 2. Selective Bubble Generation
Configure which CarbonChat channels generate chat bubbles:

- Channels listed in `bubble-channels` will trigger chat bubble creation
- Other channels will show messages in chat without bubbles
- Channel names must match exactly as they are defined in CarbonChat

### 3. Automatic Prefix Management
The plugin automatically adds the bubble-trigger prefix (default: `.`) to messages in configured channels. ChatBubbles then detects this prefix and creates the bubble, removing the prefix automatically.

### 4. ChatBubbles Toggle Detection
The plugin intelligently detects when a player has ChatBubbles disabled:

- If a player has ChatBubbles toggled OFF, the prefix is **NOT added**
- This prevents the bubble prefix from appearing in chat when bubbles are disabled
- Works seamlessly with ChatBubbles' built-in toggle system

---

## 📋 Requirements

- **Minecraft**: 1.20.1+
- **Server**: Spigot/Paper
- **Dependencies**:
  - PlaceholderAPI
  - CarbonChat 3.0.0+
  - ChatBubbles (Mode 5)

---

## 🔧 Installation

1. Download `CarbonChatExpansion-1.0.0.jar`
2. Place in your server's `plugins/` folder
3. Configure ChatBubbles (see below)
4. Configure CarbonChatExpansion (see below)
5. Restart your server

---

## ⚙️ Configuration

### CarbonChatExpansion Config

**`plugins/CarbonChatExpansion/config.yml`:**

```yaml
# Bubble prefix (must match ChatBubbles config)
bubble-prefix: "."

# Channels that generate bubbles (must match CarbonChat channel names)
bubble-channels:
  - chillar
  - gritar

# Channel colors in HEX format (#RRGGBB)
channel-colors:
  default: "#ffffff"
  global: "#ffff55"
  chillar: "#ff7d86"
  gritar: "#ff0000"
  local: "#55ff55"

# Enable debug logging
debug-mode: false
```

**Important Notes:**
- `bubble-prefix` must match `ConfigFive_Prefix_Characters` in ChatBubbles config
- `bubble-channels` names must match exactly as defined in CarbonChat
- `channel-colors` use HEX format for precise color control

### ChatBubbles Config

**`plugins/ChatBubbles/config.yml`:**

```yaml
ChatBubble_Configuration_Mode: 5

ConfigFive_Prefix_Characters:
  - "."

ChatBubble_Send_Original_Message: true

ChatBubble_Message_Format:
 - "[%player_name%]"
 - "%carbonchat_channel_color%%chatbubble_message%"
```

**Key Settings:**
- **Mode 5**: Messages starting with `.` become bubbles
- **Prefix Characters**: Must match `bubble-prefix` in CarbonChatExpansion config
- **Message Format**: Use `%carbonchat_channel_color%` to colorize the bubble text

---

## 🎮 Commands

| Command | Permission | Description |
|---------|-----------|-------------|
| `/cce` | `carbonchatexpansion.admin` | Show plugin help |
| `/cce reload` | `carbonchatexpansion.admin` | Reload configuration |
| `/cce debug <on|off>` | `carbonchatexpansion.admin` | Toggle debug mode |

**Aliases:** `/carbonchatexpansion`

---

## 🔍 Placeholders

Use these in ChatBubbles or other PlaceholderAPI-compatible plugins:

| Placeholder | Description | Example Output |
|------------|-------------|----------------|
| `%carbonchat_channel_color%` | Channel color (HEX) | `#ff7d86` |
| `%carbonchat_channel_name%` | Channel name | `chillar` |
| `%carbonchat_channel_key%` | Full channel key | `carbon:chillar` |

---

## 💡 How It Works

1. **Player sends message** in a channel (e.g., "chillar")
2. **CarbonChatExpansion** checks if "chillar" is in `bubble-channels`
3. **ChatBubbles toggle check**: If player has bubbles disabled, skip prefix
4. If enabled, adds prefix: `.mensaje`
5. **ChatBubbles** detects the `.` prefix and creates a bubble
6. **ChatBubbles** automatically removes the `.` from the displayed text
7. **Placeholder** `%carbonchat_channel_color%` applies the pink color (`#ff7d86`)
8. **Result**: Pink bubble appears above player's head with the message

---

## 📊 Example Setup

### Scenario: Two channels with different behaviors

**CarbonChat Channels:**
- `global` - General chat (no bubbles)
- `chillar` - Local chat with pink bubbles

**CarbonChatExpansion Config:**
```yaml
bubble-channels:
  - chillar

channel-colors:
  global: "#ffff55"
  chillar: "#ff7d86"
```

**Result:**
- Messages in `global` → Chat only, no bubble
- Messages in `chillar` → Chat + pink bubble
- If player disables bubbles: No prefix added, clean chat

---

## 🐛 Troubleshooting

### Bubbles not appearing
1. Verify ChatBubbles is in Mode 5
2. Check `bubble-prefix` matches in both configs
3. Ensure channel names match CarbonChat exactly
4. Check player has ChatBubbles enabled (`/bubble`)
5. Enable debug mode: `/cce debug on`

### Prefix appearing in chat
1. Check if player has ChatBubbles disabled
2. Enable debug mode to see detailed logs
3. Verify plugin version is up-to-date

### Colors not working
1. Verify PlaceholderAPI is installed
2. Check placeholder syntax in ChatBubbles config
3. Use HEX format in `channel-colors` (#RRGGBB)

### Debug Mode
Enable with `/cce debug on` to see detailed logging:
- Channel detection
- Message modification
- ChatBubbles toggle status
- Prefix addition/removal decisions

---

## 📝 Version History

**v1.0.0** - Initial Release
- PlaceholderAPI integration for channel colors
- Selective bubble generation per channel
- Automatic prefix management
- ChatBubbles toggle detection
- Debug mode and reload command

---

## 👨‍💻 Developer

**Created by:** Edgron  
**For:** Omniblock Network  
**Version:** 1.0.0  
**License:** Custom - Created exclusively for Omniblock

---

## 🔗 Links

- **CarbonChat**: https://github.com/Hexaoxide/Carbon
- **ChatBubbles**: https://www.spigotmc.org/resources/chatbubbles.92068/
- **PlaceholderAPI**: https://github.com/PlaceholderAPI/PlaceholderAPI

---

## 📧 Support

For issues or questions, contact the Omniblock development team.
