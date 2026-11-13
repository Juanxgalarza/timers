# Pixel Timers

Timers recurrentes y discretos con **patrón programable**: por ejemplo `45, 5, 45, 10`
avisa a los 45 min, 5 min después, 45 min después, 10 min después, y vuelve a empezar.
Cada aviso es una notificación silenciosa, una vibración breve, o ambas.
Kotlin + Jetpack Compose (solo `foundation`), minSdk 26.

## Obtener el APK
**Opción A – GitHub (sin instalar nada):** sube esta carpeta a un repo nuevo. La pestaña
*Actions* → *Build APK* compila y deja `PixelTimers-debug-apk` para descargar (ejecuta también los tests).

**Opción B – Android Studio:** abre la carpeta, deja sincronizar Gradle y pulsa
*Build ▸ Build APK(s)*. Sale en `app/build/outputs/apk/debug/app-debug.apk`.

Para instalarlo: copia el APK al móvil y permite "instalar apps desconocidas" para tu gestor de archivos.

## Estructura
- `Reminder`, `ReminderCodec`: modelo, patrón, validación y serialización (con tests).
- `ReminderScheduler`, `ReminderReceiver`, `BootReceiver`: alarmas encadenadas, sobreviven a reinicios.
- `Alerts`: notificación (canal IMPORTANCE_LOW, sin sonido ni popup) y vibración.
- `ui/`: componentes pixel art propios y pantalla.
