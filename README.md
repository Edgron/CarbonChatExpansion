# CarbonChat Expansion

Control inteligente de ChatBubbles por canal para servidores CarbonChat.

## ¿Qué Hace?

CarbonChat Expansion permite controlar en qué canales de CarbonChat se muestran burbujas de ChatBubbles. Por ejemplo:

- **Canal "global"**: Mensajes en chat, **sin burbuja**
- **Canal "chillar"**: Mensajes en chat **con burbuja** (color rosa)
- **Canal "local"**: Mensajes en chat **con burbuja** (color verde)

## Instalación

### 1. Requisitos Previos

- CarbonChat 3.0.0+
- ChatBubbles (cualquier versión)
- PlaceholderAPI

### 2. Configurar ChatBubbles

Abre `plugins/ChatBubbles/config.yml` y establece:

```yaml
ChatBubble_Configuration_Mode: 6

ConfigSix_Prefix_Characters:
  - "¸"
```

### 3. Instalar el Plugin

1. Compila o descarga el JAR
2. Coloca en `plugins/`
3. Reinicia el servidor
4. Edita `plugins/CarbonChatExpansion/config.yml`:

```yaml
channels-with-bubbles:
  - chillar
  - local
```

## Comandos

### `/cbereload`
Recarga la configuración sin reiniciar.

```
/cbereload
```

**Permiso:** `carbonchatexpansion.reload` (por defecto: OP)

### `/cbedebug`
Activa o desactiva el modo debug para ver información detallada.

```
/cbedebug
```

**Permiso:** `carbonchatexpansion.debug` (por defecto: OP)

## Configuración

### `bubble-disable-prefix`
Carácter usado para indicar a ChatBubbles que NO cree burbuja.
**Por defecto:** `¸`

### `channels-with-bubbles`
Lista de canales en los que SÍ se mostrarán burbujas.
```yaml
channels-with-bubbles:
  - chillar
  - local
```

### `channel-colors`
Colores por canal (para uso en formatos de chat).
```yaml
channel-colors:
  global: "<yellow>"
  chillar: "#ff7d86"
```

## Cómo Funciona

1. El plugin detecta el canal del jugador
2. Si el canal NO está en la lista de burbujas:
   - Añade un carácter especial (`¸`) al inicio del mensaje
   - ChatBubbles lo ve y NO crea burbuja
   - El carácter se remueve antes de mostrar al jugador
3. Si el canal SÍ está en la lista:
   - El mensaje se procesa normalmente
   - ChatBubbles crea la burbuja

## Créditos

Creado por **Edgron** para **Omniblock**

## Soporte

Para reportar bugs o sugerencias, contacta con el administrador del servidor.
