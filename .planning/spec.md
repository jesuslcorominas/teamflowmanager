# Spec B5 — Tarjetas de cambios programados en `MatchScreen`

Issue #412. Rama `feature/412-pending-substitutions-ui` sobre `feature/412-scheduled-substitutions`.
Alcance: **`shared-ui` y nada más**. No se toca `viewmodel`, `usecase`, `domain` ni `data`.

---

## 1. Punto de partida verificado

Leído en el worktree, no supuesto:

- `MatchScreen.kt` (1159 líneas) pinta, dentro de `MatchUiState.Success`, una `Column`:
  `MatchTimeCard` → `PlayerSortOrderRow` → `LazyColumn` de jugadores (`weight(1f)`) → `BottomButtons`.
  Los diálogos y el overlay de spinner (`isSubstitutionInProgress`) cuelgan del `Box` raíz de `MatchScreen`.
- `SubstitutionCard.kt` es la tarjeta del **timeline de un partido terminado**: recibe `SubstitutionItem`,
  que exige `matchElapsedTimeMillis`. Un cambio *programado* no tiene minuto todavía.
- `shared-ui/src/commonTest` **no tiene compose-ui-test**: sólo `kotlin("test")`. Los dos tests que hay
  (`MatchScreenAggregateScorersTest`, `MatchTimeCardTest`) prueban **funciones puras `internal`**, no composables.
- `MatchSubstitutionCoordinator.isOnPitch(mode)` = `isRunning || isPaused` en `SCHEDULED`, `isRunning` en `LIVE`.
- `MatchDetailContent` hoy calcula `isPlaying = if (match.isInProgress) isRunning else false` y sólo habilita
  el `onClick` del jugador si `match.isInProgress`.

### 1.1 Hallazgo que afecta al alcance

El criterio *«en pausa se pueden programar cambios y se ve quién está en el campo»* **no está cubierto
hoy en la pantalla**, aunque el ViewModel sí lo soporte: con el partido en `PAUSED` ningún jugador se
pinta como "en el campo" y **ninguno es pulsable**, así que no hay forma de llegar a `selectPlayerOut`.
Es UI pura (`MatchScreen.kt`), luego está dentro de mi alcance y entra en este spec (§5). Lo señalo
explícitamente porque no venía desglosado en el briefing.

---

## 2. Decisiones de diseño

### 2.1 Dónde viven las tarjetas — sección sobre la lista de jugadores

Una sección propia entre `PlayerSortOrderRow` y el `LazyColumn` de jugadores, visible **sólo** si
`mode == SCHEDULED && !readOnly && pendingSubstitutions.isNotEmpty()`.

Razón: el gesto es *mirar la lista de jugadores → tocar dos → ver que aparece la tarjeta*. Un bottom
sheet obliga a un gesto extra y esconde justo lo que se acaba de producir. La lista de jugadores
conserva su `weight(1f)`, así que la sección crece a costa del espacio sobrante, no del suyo.

Altura acotada: `heightIn(max = 200.dp)` con scroll vertical propio (≈3 tarjetas visibles). Sin tope,
N tarjetas dejarían la lista de jugadores en nada — y N tarjetas simultáneas es criterio de aceptación.

### 2.2 Tarjeta propia, no reutilizar `SubstitutionCard`

`SubstitutionCard` exige `SubstitutionItem(playerOut, playerIn, matchElapsedTimeMillis)`. Un cambio
programado **no tiene minuto**, y además la tarjeta del timeline es alta (badge + barra + nombre en
columna) y no tiene acciones.

Nuevo composable `PendingSubstitutionCard` en `ui/matches/components/`, **fila compacta** (~64dp) que
hereda el vocabulario visual de la casa: `JerseyBadge` + `SubstitutionRed` para quien sale,
`SubstitutionGreen` para quien entra, sobre `AppCard`. A la derecha, dos `AppIconButton`:
`Icons.Filled.PlayArrow` → `executePendingSubstitution(item.pair)` y `Icons.Filled.Delete` →
`removePendingSubstitution(item.pair)`.

Si en review se prefiere extraer las piezas comunes a `SubstitutionCard`, se puede hacer después; hoy
comparten estilo, no estructura, y factorizarlas obligaría a tocar el timeline, que está fuera de alcance.

### 2.3 Cabecera de la sección: contador + acciones de lote

Fila de cabecera con el número de tarjetas y dos botones de texto:
`"Cambiar todos"` → `executeAllPendingSubstitutions()`, `"Borrar todos"` → confirmación → `clearPendingSubstitutions()`.

- **"Borrar todos" pide confirmación.** Es destructivo, irreversible y borra trabajo de varios minutos.
- **El borrado de una tarjeta suelta NO pide confirmación.** Es una unidad, se rehace en dos toques, y
  un diálogo por tarjeta convierte limpiar la cola en un calvario.

### 2.4 Con el partido parado — botón deshabilitado y una línea que explica por qué

Corrección del coordinador sobre la versión anterior de este spec, que proponía un diálogo de aviso:
**se deshabilita, no se avisa.** Con el partido en pausa *todos* los jugadores del campo están en
`PAUSED`, así que ninguna pareja puede prosperar — no es improbable, es imposible. Un diálogo que
pregunta «¿ejecutar igualmente?» ofrece un camino muerto cuya única respuesta sensata es «no».

Se sigue el precedente del propio proyecto, `RoleSelectorSection` en `SettingsScreen.kt`:
**control deshabilitado + texto en `bodySmall` debajo**, porque un control gris sin explicación se lee
como un bug. El texto además tiene algo útil que decir, no sólo una negativa:

> «Con el partido en pausa los cambios no pueden aplicarse todavía: se aplicarán automáticamente al reanudar.»

Se aplica con el mismo criterio a **"Cambiar todos"** y al **botón de play de cada tarjeta**, que tienen
exactamente el mismo problema.

**Esto es decisión de UI, no de comportamiento.** El ViewModel no se toca y lo fijado con test sigue
intacto: si el lote llegara a ejecutarse en pausa, seguiría descartándose entero por
`PLAYER_OUT_NOT_PLAYING` conservando las tarjetas. La pantalla simplemente deja de ofrecer un camino
que no lleva a ningún sitio.

Predicado, función pura y probada: `canExecutePendingSubstitutions(match) = match.isInProgress`.

### 2.5 Feedback del resultado — snackbar para lo trivial, diálogo para lo que no se puede perder

`lastSubstitutionResult` no es un caso, son tres, y merecen medios distintos:

| Caso | Medio | Por qué |
|---|---|---|
| `MANUAL` y `discarded` vacío | **Snackbar** | El entrenador acaba de pulsar y ve la lista cambiar delante. Un diálogo es fricción sobre algo que ya ha visto. |
| `MANUAL` con algún descarte | **Diálogo** | Hay que leer **una razón por pareja**, con nombres. Eso no cabe en un snackbar, y las tarjetas siguen ahí para arreglarlas: es accionable. |
| `RESUME` (siempre) | **Diálogo** | **Ocurrió sin el entrenador delante**, al reanudar tras el descanso. Y en `RESUME` las tarjetas descartadas **se borran** del almacén: este aviso es literalmente la única constancia que quedará de que su equipo cambió. Un snackbar que se va solo en 4 s puede perderse mirando al campo. |

**Redacción propia para `RESUME`**, no una variante del texto manual: allí el sujeto no es "has hecho",
es "se han aplicado al reanudar". Título distinto y cuerpo que dice que se ejecutaron solos.

Contenido del diálogo, en este orden: línea de resumen (`N aplicados, M descartados`), lista de aplicados
(`12 Pérez → 7 García`) y lista de descartados con su razón (`12 Pérez → 7 García: no está convocado`).

**Consumo del resultado.** Se copia a estado local de la pantalla y se llama a
`consumeLastSubstitutionResult()` **inmediatamente**, antes de pintar. Si no, el diálogo se quedaría sin
estado que lo sostenga en cuanto se consumiera. Efecto colateral deseado: un resultado ya visto no puede
reaparecer al reentrar en la pantalla en iOS.

### 2.6 Diálogo de conflicto

`pendingSubstitutionConflict != null` → diálogo que **nombra** las parejas de `displaced` que se perderán
si confirma. Confirmar → `confirmPendingSubstitutionConflict()`; cancelar/descartar →
`dismissPendingSubstitutionConflict()`. Se reutiliza `AlertDialog` con `Column` (como
`InvalidSubstitutionAlertDialog`) porque hay que listar parejas, no un párrafo: `AppAlertDialog` sólo
acepta `message: String`.

### 2.7 Estado colgado en iOS — `LaunchedEffect(Unit)`

Patrón conocido del proyecto: en iOS el VM se cachea en el `ViewModelStore` raíz y no se limpia al
navegar atrás, así que `pendingSubstitutionConflict` y `lastSubstitutionResult` pueden sobrevivir a la
salida de la pantalla y reaparecer al reentrar.

Un único `LaunchedEffect(Unit)` que **primero limpia y después observa**, en el mismo bloque, para que no
haya carrera entre dos efectos independientes:

```kotlin
LaunchedEffect(Unit) {
    viewModel.dismissPendingSubstitutionConflict()
    viewModel.consumeLastSubstitutionResult()
    viewModel.lastSubstitutionResult.collect { result ->
        if (result == null) return@collect
        viewModel.consumeLastSubstitutionResult()
        pendingResultToShow = result
    }
}
```

### 2.8 Modo `LIVE` — no-regresión, no variante

En `LIVE` no se pinta la sección, ni la cabecera, ni ningún diálogo nuevo. Las expresiones que toco en
§5 se reducen **literalmente** a la expresión actual cuando `mode == LIVE`; lo fijo con test.

### 2.9 Fuera de alcance, consciente

- **#420** — un lote que revienta por excepción no publica `lastSubstitutionResult`: el entrenador ve el
  spinner ir y venir y no hay aviso. Tocaría el ViewModel. **No se arregla aquí.** El diseño de §2.5 no
  lo empeora ni lo tapa.
- Drag & drop (KMP-27 / #275).
- **Render de composables.** No hay compose-ui-test en `commonTest` (§4) y no se añade: se prueba la
  lógica de presentación, no el render, así que un fallo de composición o de layout **no lo atrapa esta
  suite**. Va con epígrafe propio en el cuerpo de la PR para que el revisor lo sepa de entrada.
- Rediseñar el flujo de selección en dos pasos: se reutiliza tal cual.

---

## 3. Traducción de `SubstitutionDiscardReason` a lenguaje de entrenador

| Constante | ES | EN |
|---|---|---|
| `PLAYER_OUT_NOT_PLAYING` | no estaba en el campo | was not on the pitch |
| `PLAYER_IN_ALREADY_PLAYING` | ya estaba jugando | was already on the pitch |
| `PLAYER_IN_NOT_IN_MATCH` | no está convocado | is not in the squad call-up |
| `PLAYER_ALREADY_SUBSTITUTED_IN_BATCH` | ya estaba en otro cambio de esta tanda | was already part of another change in this batch |

Sin jerga (`batch`, `player time`, `pitch predicate`) y sin id ninguno: el entrenador lee nombres y dorsales.

---

## 4. Estrategia de test — y por qué la UI se prueba por funciones puras

`commonTest` **no tiene compose-ui-test**. Añadirlo significaría meter infra de test de Compose para
Android + iOS en `shared-ui` por una pantalla: desproporcionado y fuera de lo que pide el briefing.

Se sigue el patrón que ya usa el módulo (`aggregateScorers`, `calculateFinishedPeriodElapsedTime`):
**la decisión se extrae a función pura `internal` y se prueba; el composable sólo la consume.**

Nuevo fichero `ui/matches/PendingSubstitutionsPresentation.kt`:

```kotlin
internal fun pendingCardsToShow(
    mode: SubstitutionMode, readOnly: Boolean, items: List<PendingSubstitutionItem>,
): List<PendingSubstitutionItem>

internal fun isOnPitchForDisplay(
    mode: SubstitutionMode, match: Match, item: PlayerTimeItem,
): Boolean

internal fun canSelectPlayerForSubstitution(
    mode: SubstitutionMode, match: Match, readOnly: Boolean,
): Boolean

internal fun canExecutePendingSubstitutions(match: Match): Boolean

internal enum class SubstitutionResultPresentation { SNACKBAR, DIALOG }
internal fun presentationFor(result: SubstitutionExecutionResult): SubstitutionResultPresentation

internal fun discardReasonRes(reason: SubstitutionDiscardReason): StringResource
```

Cobertura mínima:

1. `pendingCardsToShow` → vacío en `LIVE` aun con N items; vacío con `readOnly`; los N items en `SCHEDULED`.
2. `isOnPitchForDisplay` → en `LIVE` idéntico a hoy (`isRunning` si `IN_PROGRESS`, si no `false`);
   en `SCHEDULED` con `PAUSED`, un jugador `isPaused` sale como en el campo.
3. `canSelectPlayerForSubstitution` → falso con `readOnly`; falso en `LIVE` + `PAUSED`;
   cierto en `SCHEDULED` + `PAUSED`.
4. `canExecutePendingSubstitutions` → cierto sólo con `IN_PROGRESS`; falso con `PAUSED`, `TIMEOUT`,
   `SCHEDULED` y `FINISHED`.
5. `presentationFor` → `MANUAL` limpio → SNACKBAR; `MANUAL` con descarte → DIALOG; `RESUME` limpio → DIALOG.
6. `discardReasonRes` → **las cuatro** constantes mapean a cuatro `StringResource` distintos
   (`assertEquals` contra el recurso esperado, más un test de que no hay dos iguales).

**Los tests se verán fallar.** Antes de darlos por buenos comprobaré que cada uno falla contra una
implementación deliberadamente rota. Se falsan, por indicación del coordinador, **`pendingCardsToShow`**,
**`discardReasonRes`** y sobre todo **`isOnPitchForDisplay`** y **`canSelectPlayerForSubstitution`**, que son
las dos que tocan comportamiento ya existente y las que más duelen si se rompen. Un test verde que nunca ha estado rojo no prueba nada — costó un
P0 en una tarea anterior de este mismo issue.

**Riesgo técnico conocido:** el `Res` generado por Compose Resources es `internal` al módulo. `commonTest`
es una compilación asociada a `commonMain`, así que debería verlo. Se comprueba **en el primer commit**.
Si no lo viera, `discardReasonRes` devolverá un `internal enum DiscardReasonMessage` (probable en test) y
el mapeo enum→`StringResource` se hará en el composable, que no se prueba. Se avisaría al coordinador.

---

## 5. Cambio en la lista de jugadores (criterio «en pausa se ve quién está en el campo»)

En `MatchDetailContent`, sustituir las dos expresiones actuales por las funciones puras de §4:

```kotlin
// hoy: if (state.match.isInProgress) playerTimeItem.isRunning else false
val isPlaying = isOnPitchForDisplay(mode, state.match, playerTimeItem)

// hoy: if (state.match.isInProgress && !readOnly) { ... } else null
onClick = if (canSelectPlayerForSubstitution(mode, state.match, readOnly)) { ... } else null
```

con

```kotlin
internal fun isOnPitchForDisplay(mode, match, item) = when {
    mode == SubstitutionMode.SCHEDULED && match.isStarted -> item.isRunning || item.isPaused
    match.isInProgress -> item.isRunning
    else -> false
}

internal fun canSelectPlayerForSubstitution(mode, match, readOnly) = when {
    readOnly -> false
    mode == SubstitutionMode.SCHEDULED -> match.isStarted
    else -> match.isInProgress
}
```

`isOnPitchForDisplay` **refleja exactamente** `MatchSubstitutionCoordinator.isOnPitch(mode)`, de modo que
lo que se pinta como "en el campo" es justo lo que el ViewModel aceptará como jugador que sale.
`isStarted` incluye `TIMEOUT`: durante un tiempo muerto en modo programado también se puede encolar, que
es coherente con el ViewModel, al que el status le da igual.

En `LIVE` ambas se reducen a la expresión de hoy, carácter por carácter. Test 2 y 3 de §4 lo fijan.

---

## 6. Strings nuevos (`values/strings.xml` **y** `values-es/strings.xml`, los dos siempre)

Prefijos `pending_substitutions_*` y `substitution_result_*`, siguiendo `settings_substitution_*`.

```
pending_substitutions_title                     "Cambios programados (%1$d)"
pending_substitutions_execute_all               "Cambiar todos"
pending_substitutions_clear_all                 "Borrar todos"
pending_substitutions_execute_one               "Aplicar este cambio"        (contentDescription)
pending_substitutions_remove_one                "Borrar este cambio"         (contentDescription)

pending_substitutions_clear_all_title           "¿Borrar todos los cambios?"
pending_substitutions_clear_all_message         "Se perderán los %1$d cambios programados. Esta acción no se puede deshacer."

pending_substitutions_paused_hint               "Con el partido en pausa los cambios no pueden aplicarse todavía: se aplicarán automáticamente al reanudar."

pending_substitutions_conflict_title            "Este jugador ya tiene un cambio"
pending_substitutions_conflict_message          "Si programas %1$s, se descartarán estos cambios:"
pending_substitutions_conflict_confirm          "Programar igualmente"

substitution_result_manual_title                "Cambios aplicados"
substitution_result_resume_title                "Cambios aplicados al reanudar"
substitution_result_resume_intro               "Estos cambios se ejecutaron solos al reanudar el partido:"
substitution_result_applied_header              "Aplicados (%1$d)"
substitution_result_discarded_header            "No aplicados (%1$d)"
substitution_result_snackbar_applied            "%1$d cambios aplicados"
substitution_result_pair                        "%1$d %2$s → %3$d %4$s"
substitution_result_pair_discarded              "%1$d %2$s → %3$d %4$s: %5$s"

substitution_discard_reason_out_not_playing     "no estaba en el campo"
substitution_discard_reason_in_already_playing  "ya estaba jugando"
substitution_discard_reason_in_not_in_match     "no está convocado"
substitution_discard_reason_already_in_batch    "ya estaba en otro cambio de esta tanda"
```

(Arriba el ES; el EN de §3 y su equivalente para el resto va en `values/strings.xml`.)

---

## 7. Definition of Done

- `./gradlew :shared-ui:test --no-daemon --stacktrace`
- `./gradlew ktlintCheck --no-daemon --stacktrace`
- `./gradlew :app:assembleDevDebug --no-daemon`
- `./gradlew :iosApp:compileKotlinIosSimulatorArm64 --no-daemon`
- Cambios en disco, confirmados con `git status` / `git diff`; `git add` por ruta explícita.
- PR contra `feature/412-scheduled-substitutions` **sólo cuando el coordinador lo indique**. Sin merge.
