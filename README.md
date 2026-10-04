# RegistroIncidencias

Aplicación móvil Android para el registro rápido de incidencias (fallas, errores o reportes) mediante un título, una descripción breve y una prioridad. Proyecto desarrollado de forma incremental para la asignatura **Técnicas de Producción Industrial de Software I** — Universidad Tecnológica de El Salvador.

## Descripción

`RegistroIncidencias` permite a una persona usuaria capturar una incidencia indicando un **título**, una **descripción** y una **prioridad**, y registrarla mediante un botón principal. La pantalla valida los datos y muestra retroalimentación inmediata (estado vacío, mensaje de error o mensaje de éxito), además de listar las incidencias registradas durante la sesión.

El registro todavía vive en memoria mientras la app está abierta; la persistencia está planificada para un avance posterior.

## Estado actual de la pantalla

- Campo de texto **Título de la incidencia** (una línea, máximo 60 caracteres con contador visible).
- Campo de texto **Descripción de la incidencia** (múltiples líneas).
- Selector táctil de **Prioridad** (Baja · Media · Alta).
- Botón principal **Registrar incidencia**.
- Retroalimentación visible:
  - **Estado vacío**: "Todavía no hay incidencias registradas." mientras no se ha registrado nada.
  - **Mensaje de error** (rojo) si falta el título, la descripción o la prioridad.
  - **Mensaje de éxito** (verde) que indica la prioridad registrada; la incidencia aparece en la lista con su etiqueta de prioridad.

## Avance semana 10 — Teclado contextual e interacción táctil

### Entrada de texto con `KeyboardOptions`

| Campo | Tipo de teclado | Capitalización | Autocorrección | Acción IME | Comportamiento al pulsar la acción |
|---|---|---|---|---|---|
| Título | `KeyboardType.Text` | `Sentences` | Activada | `ImeAction.Next` (→\|) | Mueve el foco a la descripción (`focusManager.moveFocus(FocusDirection.Next)`). |
| Descripción | `KeyboardType.Text` | `Sentences` | Activada | `ImeAction.Done` (✓) | Cierra el teclado (`focusManager.clearFocus()`) y deja a la vista la prioridad y el botón. |

Además, el título muestra un *placeholder* de ejemplo y un contador `n / 60` que se actualiza al escribir.

### Interacción táctil: selector de prioridad

- Componente `PrioridadSelector` (`ui/components/PrioridadSelector.kt`): tres tarjetas (`Card`) en una fila, cada una tocable en toda su superficie (≈ 115 × 108 dp, por encima del mínimo recomendado de 48 × 48 dp).
- Se implementa con `Modifier.selectable(role = Role.RadioButton)` dentro de un `Modifier.selectableGroup()`: es un modificador de alto nivel (variante de `clickable` para selección) que además comunica a TalkBack si la opción está seleccionada.
- Retroalimentación al tocar: borde y fondo con el color de la prioridad (animados con `animateColorAsState`), `RadioButton` marcado, texto "Prioridad seleccionada: Alta · Atención inmediata" y vibración háptica breve (`HapticFeedbackType.SegmentTick`).
- La prioridad forma parte del modelo `Incidencia`, se valida antes de registrar y se muestra como etiqueta de color en cada incidencia de la lista.
- No se usó `pointerInput` porque no se necesita un gesto personalizado: un toque simple está cubierto por `selectable`.

### Evidencias (emulador Pixel 10a · Android 17 / API 37)

| Captura | Qué demuestra |
|---|---|
| [`04_pantalla_inicial.png`](evidencia/04_pantalla_inicial.png) | Pantalla con el nuevo selector de prioridad. |
| [`05_teclado_titulo_accion_siguiente.png`](evidencia/05_teclado_titulo_accion_siguiente.png) | Teclado del título con Shift automático y tecla de acción **Siguiente**. |
| [`06_titulo_escrito_contador.png`](evidencia/06_titulo_escrito_contador.png) | Título escrito, contador 32/60 y sugerencias de autocorrección. |
| [`07_teclado_descripcion_accion_listo.png`](evidencia/07_teclado_descripcion_accion_listo.png) | Foco en la descripción tras pulsar Siguiente; tecla de acción **Listo**. |
| [`08_validacion_prioridad.png`](evidencia/08_validacion_prioridad.png) | Teclado cerrado tras Listo y error al registrar sin prioridad. |
| [`09_prioridad_seleccionada.png`](evidencia/09_prioridad_seleccionada.png) | Toque en "Alta": tarjeta resaltada y texto de retroalimentación. |
| [`10_registro_exito_prioridad.png`](evidencia/10_registro_exito_prioridad.png) | Registro exitoso con la prioridad elegida. |
| [`11_lista_con_prioridades.png`](evidencia/11_lista_con_prioridades.png) | Lista de incidencias con etiquetas de prioridad. |

El documento de entrega de la semana es [`Evidencias_Semana10_Teclado_Tactil_RegistroIncidencias.docx`](evidencia/Evidencias_Semana10_Teclado_Tactil_RegistroIncidencias.docx).

### Pruebas instrumentadas

`app/src/androidTest/.../RegistroIncidenciasScreenTest.kt` verifica, sobre el emulador, que:

1. El título declara `ImeAction.Next` y la descripción `ImeAction.Done`.
2. La acción Siguiente del título mueve el foco a la descripción.
3. Tocar una prioridad la marca como seleccionada y muestra la retroalimentación.
4. Registrar sin prioridad muestra el mensaje de error.
5. Un registro completo agrega la incidencia a la lista con su prioridad y reinicia el selector.

```bash
./gradlew connectedDebugAndroidTest
```

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
├── MainActivity.kt                    # Pantalla principal (Compose) y teclado contextual
├── model/
│   ├── Incidencia.kt                  # Modelo de datos de una incidencia
│   └── Prioridad.kt                   # Prioridades disponibles (Baja, Media, Alta)
└── ui/
    ├── components/PrioridadSelector.kt # Selector táctil y etiqueta de prioridad
    └── theme/                         # Tema Material 3 y colores de prioridad
```

## Dificultades encontradas

**Semana 10.** En la versión anterior, la descripción usaba la acción IME por defecto (salto de línea), así que al terminar de escribir el teclado seguía abierto y tapaba el nuevo selector de prioridad y el botón principal. Se configuró `ImeAction.Done` en la descripción con `KeyboardActions(onDone = { focusManager.clearFocus() })`, de modo que la tecla ✓ cierra el teclado y deja visible el resto del formulario; el título usa `ImeAction.Next` para encadenar ambos campos.

Durante las pruebas también se observó que Gboard no activa Shift en la primera letra de la descripción (campo multilínea vacío), aunque sí lo hace en el título. Con `adb shell dumpsys input_method` se comprobó que la configuración llega correctamente al teclado (`inputType=0x2c001`: texto + `CAP_SENTENCES` + autocorrección + multilínea; `imeOptions` con `IME_ACTION_DONE`) y que Gboard sí capitaliza después de cada punto, por lo que se trata de un comportamiento del teclado y no de la app.

**Primer avance.** Al probar la pantalla en el emulador, el layout inicial no reservaba espacio para el teclado en pantallas pequeñas, lo que ocultaba el botón principal al escribir la descripción. Se resolvió agregando desplazamiento vertical (`verticalScroll`) al contenedor principal y configurando `windowSoftInputMode="adjustResize"` en el manifiesto, de modo que el contenido se ajusta cuando el teclado está visible.

## Próximos pasos

- Persistencia de incidencias en base de datos local (Room).
- Edición y eliminación de incidencias registradas.
- Filtro de la lista por prioridad.
