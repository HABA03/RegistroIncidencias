# RegistroIncidencias

Aplicación móvil Android para el registro rápido de incidencias (fallas, errores o reportes) mediante un título, una descripción breve y una prioridad. Proyecto desarrollado de forma incremental para la asignatura **Técnicas de Producción Industrial de Software I** — Universidad Tecnológica de El Salvador.

## Descripción

`RegistroIncidencias` permite a una persona usuaria capturar una incidencia indicando un **título**, una **descripción** y una **prioridad**, y registrarla mediante un botón principal. La pantalla valida los datos y muestra retroalimentación inmediata (estado vacío, mensaje de error o mensaje de éxito), además de listar las incidencias registradas durante la sesión.

La app incluye además una pestaña **Diagnóstico** que revisa los sensores del equipo (acelerómetro, luz y proximidad) e interpreta sus lecturas en lenguaje natural, útil para describir incidencias de hardware.

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

## Avance semana 12 — Diagnóstico del dispositivo (sensores)

Pestaña **Diagnóstico del dispositivo** (`ui/diagnostico/DiagnosticoDispositivoScreen.kt`), accesible desde la barra superior junto a **Registro**. El formulario se conserva al cambiar de pestaña (`rememberSaveableStateHolder` + `rememberSaveable`).

### Flujo implementado

| Paso | Implementación |
|---|---|
| Comprobar disponibilidad | `SensorManager.getDefaultSensor(tipo)` para `TYPE_ACCELEROMETER`, `TYPE_LIGHT` y `TYPE_PROXIMITY`. Si devuelve `null`, la tarjeta muestra **"No disponible"** y un mensaje claro; si ningún sensor existe, el botón de monitoreo se desactiva. |
| Escuchar cambios | `SensorEventListener` registrado con `SENSOR_DELAY_UI` (≈ 15 lecturas/s), suficiente para una pantalla y con menor consumo que `FASTEST`. |
| Actualizar estado | `LecturaSensorState` (estado observable de Compose) guarda la última lectura, si el listener está activo y cuántos eventos llegaron. |
| Interpretar | `InterpretacionSensor` convierte valores crudos en frases: *Dispositivo en reposo / Movimiento leve / Movimiento brusco*, *Ambiente oscuro … Luz solar directa*, *Objeto cerca / Sin objetos cerca*. Los números se muestran solo como apoyo, con su unidad. |
| Controlar el ciclo | `LifecycleResumeEffect` registra el listener en `ON_RESUME` y lo libera en `ON_PAUSE` o al salir de la composición (cambio de pestaña). El botón **Pausar lectura** también libera los sensores. |

### Criterios de interpretación

| Sensor | Regla | Frase |
|---|---|---|
| Acelerómetro | Desviación de la magnitud respecto a la gravedad (9.81 m/s²): < 0.6 · < 3.0 · ≥ 3.0 | Reposo · Movimiento leve · Movimiento brusco |
| Luz | < 10 · < 200 · < 1 000 · < 10 000 · ≥ 10 000 lx | Oscuro · Tenue · Interior adecuada · Mucha luz · Luz solar directa |
| Proximidad | Distancia < mín(rango máximo, 5 cm) | Objeto cerca · Sin objetos cerca |

### Evidencias (emulador Pixel 10a · Android 17 / API 37)

Los valores se simularon con la consola del emulador (`adb emu sensor set acceleration|light|proximity …`), equivalente a *Extended controls › Virtual sensors*.

| Captura | Qué demuestra |
|---|---|
| [`12_registro_con_pestanas.png`](evidencia/12_registro_con_pestanas.png) | Nueva navegación por pestañas. |
| [`13_diagnostico_reposo_luz_interior.png`](evidencia/13_diagnostico_reposo_luz_interior.png) | 3 de 3 sensores disponibles y escuchando; reposo e iluminación interior. |
| [`14_diagnostico_proximidad_lejos.png`](evidencia/14_diagnostico_proximidad_lejos.png) | Proximidad sin objetos cerca. |
| [`15_diagnostico_movimiento_luz_cerca.png`](evidencia/15_diagnostico_movimiento_luz_cerca.png) | Tras cambiar los valores: movimiento brusco, luz solar directa y objeto cerca. |
| [`16_diagnostico_pausado_liberado.png`](evidencia/16_diagnostico_pausado_liberado.png) | Monitoreo en pausa: sensores liberados. |
| [`17_diagnostico_pausado_ultima_lectura.png`](evidencia/17_diagnostico_pausado_ultima_lectura.png) | Última lectura conservada; el contador de eventos deja de aumentar. |
| [`18_diagnostico_sin_sensores.png`](evidencia/18_diagnostico_sin_sensores.png) | Mensaje claro cuando ningún sensor está disponible. |
| [`19_registro_conserva_formulario.png`](evidencia/19_registro_conserva_formulario.png) | El título escrito se conserva al volver de la pestaña Diagnóstico. |
| [`20_logcat_ciclo_sensores.txt`](evidencia/20_logcat_ciclo_sensores.txt) | Logcat del ciclo registro → pausa (liberación) → reanudación → cambio de pestaña. |

Documento de entrega: [`Evidencias_Semana12_Diagnostico_Sensores_RegistroIncidencias.docx`](evidencia/Evidencias_Semana12_Diagnostico_Sensores_RegistroIncidencias.docx).

### Pruebas

- **Unitarias** (`app/src/test/.../sensores/InterpretacionSensorTest.kt`): 10 casos sobre los umbrales de movimiento, luz y proximidad (incluye límites y caída libre).
- **Instrumentadas**:
  - `DiagnosticoDispositivoScreenTest`: mensaje claro sin sensores (botón desactivado), pausa/reanudación del monitoreo y conservación del formulario al cambiar de pestaña.
  - `CicloVidaSensoresTest`: con `ActivityScenario.moveToState(STARTED)` verifica que cada listener se libera al perder el primer plano y se registra de nuevo en `RESUMED`.

```bash
./gradlew testDebugUnitTest connectedDebugAndroidTest
```

### Limitaciones

- En el emulador los sensores son virtuales: los valores se inyectan manualmente y no hay ruido real, por lo que los umbrales deben calibrarse en un dispositivo físico.
- El sensor de proximidad del emulador es binario (0 o 1 cm de rango máximo), igual que en muchos teléfonos; no permite medir distancias intermedias.
- El acelerómetro incluye la gravedad: girar el equipo lentamente no se interpreta como movimiento, solo los cambios bruscos de aceleración.
- Con aceleración de GPU del host, `adb shell screencap` del emulador devuelve imágenes negras; las capturas se obtuvieron con `captureToImage()` de Compose Test.

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
├── MainActivity.kt                    # Pestañas Registro/Diagnóstico y formulario de registro
├── model/
│   ├── Incidencia.kt                  # Modelo de datos de una incidencia
│   ├── Prioridad.kt                   # Prioridades disponibles (Baja, Media, Alta)
│   └── RecursoSensor.kt               # Sensores revisados en el diagnóstico
├── sensores/
│   ├── InterpretacionSensor.kt        # Valores crudos → frase con contexto (lógica pura)
│   └── LecturaSensor.kt               # Registro/liberación del listener según el ciclo de vida
└── ui/
    ├── components/PrioridadSelector.kt # Selector táctil y etiqueta de prioridad
    ├── diagnostico/                   # Pantalla "Diagnóstico del dispositivo"
    └── theme/                         # Tema Material 3 y colores
```

## Dificultades encontradas

**Semana 12.** Al agregar las pestañas, el formulario de registro perdía lo escrito al ir a Diagnóstico y volver, porque su estado vivía en `remember` y la pestaña salía de la composición. Se resolvió con `rememberSaveableStateHolder` y `rememberSaveable` (con un `listSaver` para la lista de incidencias), lo que además conserva los datos al rotar la pantalla. También se comprobó que Compose Test no puede leer la interfaz mientras la Activity está en pausa, por lo que la prueba de ciclo de vida verifica la liberación de los listeners a través de Logcat.

**Semana 10.** En la versión anterior, la descripción usaba la acción IME por defecto (salto de línea), así que al terminar de escribir el teclado seguía abierto y tapaba el nuevo selector de prioridad y el botón principal. Se configuró `ImeAction.Done` en la descripción con `KeyboardActions(onDone = { focusManager.clearFocus() })`, de modo que la tecla ✓ cierra el teclado y deja visible el resto del formulario; el título usa `ImeAction.Next` para encadenar ambos campos.

Durante las pruebas también se observó que Gboard no activa Shift en la primera letra de la descripción (campo multilínea vacío), aunque sí lo hace en el título. Con `adb shell dumpsys input_method` se comprobó que la configuración llega correctamente al teclado (`inputType=0x2c001`: texto + `CAP_SENTENCES` + autocorrección + multilínea; `imeOptions` con `IME_ACTION_DONE`) y que Gboard sí capitaliza después de cada punto, por lo que se trata de un comportamiento del teclado y no de la app.

**Primer avance.** Al probar la pantalla en el emulador, el layout inicial no reservaba espacio para el teclado en pantallas pequeñas, lo que ocultaba el botón principal al escribir la descripción. Se resolvió agregando desplazamiento vertical (`verticalScroll`) al contenedor principal y configurando `windowSoftInputMode="adjustResize"` en el manifiesto, de modo que el contenido se ajusta cuando el teclado está visible.

## Próximos pasos

- Persistencia de incidencias en base de datos local (Room).
- Edición y eliminación de incidencias registradas.
- Filtro de la lista por prioridad.
- Adjuntar automáticamente el resultado del diagnóstico a una incidencia de hardware.
