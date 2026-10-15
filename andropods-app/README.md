# Andropods — Companion App for AirPods on Android (mejora de CAPod 5.5.0)

Fork/rediseño de [CAPod](https://github.com/d4rken-org/capod) (GPLv3) con:

## Mejoras vs CAPod
1. **Gestos 100% configurables** — single/double/triple/long por lado, en TODOS los modelos
   (CAPod solo habilita `hasStemConfig` en 4 modelos Pro). Los taps básicos usan AVRCP,
   squeeze/long usan AAP `SetStemConfig`.
2. **UI renovada** — Compose Material3 Expressive, dashboard con anillos de batería,
   dark/light dinámico, animación de case.
3. **Acciones nuevas**: asistente de voz, abrir app, leer notificaciones, bajo retardo.
4. **Arquitectura modular**: `:ble-parser`, `:reactions-api`, `:app`.

## Módulo gestos
Ver `andropods-app/app/src/main/java/com/andropods/`.

- `gestures/GestureAction.kt` — 16 acciones (12 de CAPod + 4 nuevas)
- `gestures/GestureConfig.kt` — 8 slots + preset por modelo
- `gestures/GestureEngine.kt` — resuelve evento → acción (port de `StemPressReaction.kt`)
- `ui/GesturesScreen.kt` — pantalla Compose M3 Expressive
- `ui/theme/` — tema Andropods

## Licencia
GPLv3 (derivado de CAPod). Ver `capod-main/LICENSE`.
