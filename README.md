# CarbonChat Expansion v6.0

Control de ChatBubbles por canal usando backticks invisibles.

## Configuración

En ChatBubbles/config.yml establece:
```yaml
ChatBubble_Configuration_Mode: 6
```

En CarbonChatExpansion/config.yml edita:
```yaml
channels-with-bubbles:
  - chillar
  - local
```

## Cómo Funciona

Los canales en la lista mostrarán burbujas. Los demás no.

El plugin automáticamente añade un backtick (`) invisible al inicio de mensajes en canales sin burbuja, lo que hace que ChatBubbles en Modo 6 no cree burbuja. Luego remueve el backtick antes de mostrar el mensaje al usuario.

## Resultado

- Canales con burbuja: mensaje + burbuja visible
- Canales sin burbuja: solo mensaje, sin burbuja
- Usuario nunca ve caracteres especiales
