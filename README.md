# RegistroIncidencias

Aplicación móvil Android para el registro rápido de incidencias (fallas, errores o reportes) mediante un título y una descripción breve. Proyecto desarrollado de forma incremental para la asignatura **Técnicas de Producción Industrial de Software I** — Universidad Tecnológica de El Salvador.

## Descripción

`RegistroIncidencias` permite a una persona usuaria capturar una incidencia indicando un **título** y una **descripción**, y registrarla mediante un botón principal. La pantalla valida los campos y muestra retroalimentación inmediata (estado vacío, mensaje de error o mensaje de éxito), además de listar las incidencias registradas durante la sesión.

Este es el **primer avance** del proyecto: no persiste datos en base de datos todavía; el registro vive en memoria mientras la app está abierta.

## Estado actual de la pantalla

- Campo de texto **Título de la incidencia** (una línea).
- Campo de texto **Descripción de la incidencia** (múltiples líneas).
- Botón principal **Registrar incidencia**.
- Retroalimentación visible:
  - **Estado vacío**: mensaje "Todavía no hay incidencias registradas." cuando aún no se ha registrado nada.
  - **Mensaje de error**: se muestra en rojo si el título o la descripción están vacíos al presionar el botón.
  - **Mensaje de éxito**: se muestra en verde ("Incidencia registrada correctamente.") cuando el registro es válido, y la incidencia se agrega a la lista visible en pantalla.

## Tecnologías

- Kotlin 2.2.10
- Jetpack Compose (Material 3)
- Android Gradle Plugin 9.3.1 / Gradle 9.5.0
- `minSdk` 24 · `targetSdk` / `compileSdk` 37

## Cómo ejecutar el proyecto

1. Abrir el proyecto en Android Studio (Meerkat o superior).
2. Sincronizar Gradle.
3. Ejecutar la configuración `app` en un emulador o dispositivo físico con Android 7.0 (API 24) o superior.

También puede compilarse desde la línea de comandos:

```bash
./gradlew assembleDebug
```

## Estructura relevante

```
app/src/main/java/com/example/registroincidencias/
├── MainActivity.kt              # Pantalla principal (Compose)
├── model/Incidencia.kt          # Modelo de datos de una incidencia
└── ui/theme/                    # Tema Material 3 de la app
```

## Dificultades encontradas

Al probar la pantalla en el emulador, el layout inicial no reservaba espacio para el teclado en pantallas pequeñas, lo que ocultaba el botón principal al escribir la descripción. Se resolvió agregando desplazamiento vertical (`verticalScroll`) al contenedor principal y configurando `windowSoftInputMode="adjustResize"` en el manifiesto, de modo que el contenido se ajusta cuando el teclado está visible.

## Próximos pasos

- Persistencia de incidencias en base de datos local (Room).
- Edición y eliminación de incidencias registradas.
- Validaciones adicionales y pantallas de detalle.
