# CarbonChat Expansion v6.0 FINAL - CHATBUBBLES MODO 6

Versión que usa ChatBubbles Modo 6 para controlar burbujas de forma elegante.

## ✅ Cambios en v6.0

- ✨ **ÚNICO LISTENER SIMPLE:** Solo LOWEST y HIGH
- ✨ **CHATBUBBLES MODO 6:** Usa el sistema nativo de ChatBubbles
- ✨ **"!" INVISIBLE:** El usuario nunca ve el prefijo
- ✨ **LIMPIO Y EFICIENTE:** Sin cancelaciones de eventos

## Cómo Funciona

```
1. LOWEST (Mi Plugin)
   Detecta "global" NO en lista
   Añade "!" → "!hola"

2. CarbonChat
   Procesa "!hola" normalmente

3. ChatBubbles (MODO 6)
   Ve el "!" al inicio
   NO crea burbuja ✅

4. HIGH (Mi Plugin)
   Remueve el "!" → "hola"

5. Resultado Visible
   Jugador ve: "hola" (sin burbuja)
```

## Instalación CRÍTICA

1. **PRIMERO:** Configura ChatBubbles en Modo 6:

   ```yaml
   # ChatBubbles/config.yml
   ChatBubble_Configuration_Mode: 6
   ```

2. Instala el JAR del plugin

3. Edita `CarbonChatExpansion/config.yml`:
   ```yaml
   channels-with-bubbles:
     - chillar
     - local
   ```

4. Reinicia servidor

## Resultado Final

| Canal | Mensaje | Burbuja | Usuario ve |
|---|---|---|---|
| **global** | ✅ Aparece | ❌ NO aparece | "hola" (limpio) |
| **chillar** | ✅ Aparece | ✅ Aparece | "hola" + burbuja |

## Por Qué Esta Versión es Perfecta

- ✅ Simple y elegante
- ✅ Usa ChatBubbles como fue diseñado
- ✅ El usuario nunca ve caracteres especiales
- ✅ Sin complejidad innecesaria
- ✅ Sin bucles infinitos
- ✅ Sin problemas de sincronización

## Versión

6.0.0 - FINAL Y ELEGANTE
ChatBubbles Modo 6 Integration
Compatible con Minecraft 1.20.1+
