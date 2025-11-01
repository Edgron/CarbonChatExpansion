# CarbonChat Expansion v7.0

Plugin de integración entre CarbonChat y ChatBubbles usando ChatBubbles Modo 5.

## Características

- ✅ **Simple:** Un solo listener que añade "."
- ✅ **Automático:** ChatBubbles maneja el resto
- ✅ **Configurable:** Elige qué canales generan burbujas
- ✅ **Sin Debug:** Código limpio y eficiente

## Requisitos

- Minecraft 1.20.1+
- Spigot/Paper
- PlaceholderAPI
- CarbonChat 3.0.0+
- ChatBubbles (Modo 5)

## Instalación

1. Clona este repositorio
2. Compila con: `mvn clean package`
3. El JAR estará en: `target/CarbonChatExpansion-7.0.0.jar`
4. Coloca en `plugins/`
5. Configura según tu servidor
6. Reinicia

## Configuración

**config.yml:**
```yaml
bubble-channels:
  - chillar
  - gritar
```

**ChatBubbles/config.yml:**
```yaml
ChatBubble_Configuration_Mode: 5
ConfigFive_Prefix_Characters:
  - "."
ChatBubble_Send_Original_Message: true
```

## Funcionamiento

- Mensajes en canales de `bubble-channels` → Añade "." automáticamente
- ChatBubbles detecta el "." → Crea burbuja
- ChatBubbles remueve el "." → Usuario ve mensaje limpio

## Versión

7.0.0 - FINAL Y PRODUCCIÓN LISTA

## Licencia

MIT
