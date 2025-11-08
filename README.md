# CarbonChatExpansion v1.0.5

**Created for Omniblock by Edgron**

CarbonChatExpansion is a bridge plugin that connects **CarbonChat** and **ChatBubbles**, enhancing the chat experience by adding colored chat bubbles per channel, flexible control over bubble visibility, and **party-only chat bubbles with collision-safe identification**.

---

## 🎯 Features

### 1. PlaceholderAPI Integration
Provides placeholders for CarbonChat channels:
- `%carbonchat_channel_color%` - Channel color in HEX
- `%carbonchat_channel_name%` - Current channel name
- `%carbonchat_channel_key%` - Full channel key

### 2. Selective Bubble Generation
- Configure which channels generate bubbles
- Other channels show messages without bubbles

### 3. Party Chat Privacy (FIXED v1.0.5)
- Restrict chat bubbles to party members only
- Messages only visible to party members
- Holograms only visible to party members
- **Safe with multiple parties of same/different names**
- Uses `System.identityHashCode()` for unique party identification

### 4. Automatic Prefix Management
- Adds bubble-trigger prefix automatically
- Removes prefix when ChatBubbles is disabled

---

## 📋 Requirements

- **Minecraft**: 1.20.1+
- **Server**: Spigot/Paper
- **Dependencies**:
  - PlaceholderAPI
  - CarbonChat 3.0.0+
  - ChatBubbles (Mode 5)
  - DecentHolograms

---

## ⚙️ Configuration

```yaml
bubble-prefix: "."

bubble-channels:
  - chillar
  - gritar
  - partychat

party-channels:
  - partychat

channel-colors:
  partychat: "#ff88ff"

debug-mode: false
```

---

## 🚀 Usage

```bash
# Compile
mvn clean package

# Install
CarbonChatExpansion-1.0.5.jar

# Configure party-channels
# Restart server
# Create party: /carbon party create TestParty
# Write: /partychat Test message
```

---

## 🔒 Party Identification (v1.0.5)

**Problem Solved:** Multiple parties with same name

```java
// v1.0.4 (collision risk)
String partyName = "Team"  // ❌ Two "Team" parties would collide

// v1.0.5 (collision-safe)
int partyHashCode = System.identityHashCode(party)  // ✅ Unique per object instance
```

**Each party object has a unique identity hashcode that never collides**, even with identical names.

---

## 📝 Changelog

**v1.0.5** - Party Identification Fix
- FIXED: Multi-party collision risk using identityHashCode
- IMPROVED: Reliable party member filtering
- IMPROVED: Debug logging for hashcode tracking

**v1.0.4** - Party Chat Privacy
- NEW: Party chat filtering
- NEW: DecentHolograms visibility control

**v1.0.3** - Direct togglePF Access
- FIXED: Direct ChatBubbles toggle detection

**v1.0.2** - Toggle Detection Fix
- FIXED: Prefix removal when ChatBubbles OFF

**v1.0.0** - Initial Release
- PlaceholderAPI integration
- Bubble generation control
- Automatic prefix management

---

## 👨‍💻 Developer

**Created by:** Edgron  
**For:** Omniblock Network  
**Version:** 1.0.5  

---

## 🔗 Links

- **CarbonChat**: https://github.com/Hexaoxide/Carbon
- **ChatBubbles**: https://www.spigotmc.org/resources/chatbubbles.92068/
- **PlaceholderAPI**: https://github.com/PlaceholderAPI/PlaceholderAPI
- **DecentHolograms**: https://github.com/DecentSoftware-eu/DecentHolograms
