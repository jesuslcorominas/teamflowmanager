# Plan B5 — Tarjetas de cambios programados en `MatchScreen`

Spec: `.planning/spec.md`. Worktree `.claude/worktrees/b5-pending-ui`, rama
`feature/412-pending-substitutions-ui` sobre `feature/412-scheduled-substitutions`.

Siete pasos, cada uno con su commit. El paso 1 existe para tumbar pronto el único riesgo técnico
del plan; si falla, paro y aviso al coordinador antes de escribir la pantalla.

---

## Paso 1 — Lógica de presentación + su test (y comprobar el riesgo de `Res`)

**Crear** `shared-ui/src/commonMain/kotlin/.../ui/matches/PendingSubstitutionsPresentation.kt`
con las funciones puras `internal` de §4 del spec: `pendingCardsToShow`, `isOnPitchForDisplay`,
`canSelectPlayerForSubstitution`, `canExecutePendingSubstitutions`, `presentationFor`,
`discardReasonRes` y el enum `SubstitutionResultPresentation`.

**Crear** `shared-ui/src/commonTest/kotlin/.../ui/matches/PendingSubstitutionsPresentationTest.kt`
con los cinco bloques de cobertura del spec.

**Verificación del riesgo:** este paso compila `commonTest` contra `Res.string.*`. Es lo que decide si
`discardReasonRes` puede devolver `StringResource` o hay que caer al plan B del spec (§4). Se resuelve
aquí, con ~80 líneas escritas, y no a mitad de la pantalla.

**Ver fallar los tests antes de darlos por buenos:** rompo a mano `pendingCardsToShow`,
`discardReasonRes` y —por indicación del coordinador— `isOnPitchForDisplay` y
`canSelectPlayerForSubstitution`, que son las dos que tocan comportamiento existente; ejecuto
`:shared-ui:test --rerun-tasks`, confirmo el rojo, y restauro. Dejo constancia del rojo en el reporte.

`./gradlew :shared-ui:test --rerun-tasks --no-daemon`

> **Si `commonTest` no ve el `Res` generado, PARO Y AVISO al coordinador antes de aplicar el plan B.**
> Esa caída deja las cuatro traducciones sin test y cambia lo aprobado: la decisión es suya, no mía.

---

## Paso 2 — Strings en los dos idiomas

`shared-ui/src/commonMain/composeResources/values/strings.xml` (EN) y `values-es/strings.xml` (ES),
con las claves de §6 del spec. Los dos ficheros en el mismo commit, mismo orden, misma sección
comentada `<!-- Scheduled substitutions -->`.

Comprobación: `diff <(grep -o 'name="[^"]*"' values/strings.xml) <(grep -o 'name="[^"]*"' values-es/strings.xml)`
sin diferencias, para que no se escape ninguna clave sin traducir.

---

## Paso 3 — `PendingSubstitutionCard`

**Crear** `shared-ui/src/commonMain/kotlin/.../ui/matches/components/PendingSubstitutionCard.kt`:
fila compacta sobre `AppCard` con `JerseyBadge` + `SubstitutionRed` (sale) / `SubstitutionGreen` (entra),
nombre de cada uno, y a la derecha `AppIconButton` de play y de borrar con sus `contentDescription`.

Sin lógica: recibe `PendingSubstitutionItem`, `onExecute: () -> Unit`, `onRemove: () -> Unit`,
`enabled: Boolean`. Nada que probar aquí más allá de que compile.

---

## Paso 4 — Sección de pendientes en `MatchScreen`

**Modificar** `MatchScreen.kt`:

- `MatchScreen`: recoger `substitutionMode`, `pendingSubstitutions`, `pendingSubstitutionConflict`,
  `lastSubstitutionResult`; bajar `mode` y la lista por `SuccessState` → `MatchDetailContent`.
- **Nuevo** `PendingSubstitutionsSection` (privado, en el mismo fichero, como el resto de secciones):
  cabecera con contador + "Cambiar todos" + "Borrar todos", y `Column` con
  `heightIn(max = 200.dp).verticalScroll(...)` de `PendingSubstitutionCard`.
  Se pinta sólo si `pendingCardsToShow(...)` devuelve algo — la condición ya está probada en el paso 1.
- Insertarla entre `PlayerSortOrderRow` y el `LazyColumn` de jugadores.
- Diálogo de confirmación de "Borrar todos", con estado local `remember { mutableStateOf(false) }`.
- **"Cambiar todos" deshabilitado** cuando `!canExecutePendingSubstitutions(match)`, con la línea en
  `bodySmall` debajo explicando por qué (§2.4), siguiendo `RoleSelectorSection` de `SettingsScreen.kt`.
  Mismo `enabled` al botón de play de cada `PendingSubstitutionCard`.

---

## Paso 5 — Selección de jugadores en pausa (§5)

**Modificar** `MatchDetailContent`: cambiar `isPlaying` y el gate de `onClick` por
`isOnPitchForDisplay(...)` y `canSelectPlayerForSubstitution(...)`.

Son dos líneas y son las que cubren el criterio *«en pausa se pueden programar cambios y se ve quién
está en el campo»*. Van en commit propio, separado de la sección, porque son el único punto donde toco
comportamiento ya existente: si algo regresa en `LIVE`, se revierte este commit y nada más.

---

## Paso 6 — Diálogo de conflicto y feedback del resultado

**Modificar** `MatchScreen.kt`:

- `PendingSubstitutionConflictDialog`: `AlertDialog` con `Column` listando `displaced` por nombre
  (`AppAlertDialog` no vale, sólo acepta un `String`). Confirmar/cancelar al ViewModel.
- `SubstitutionResultDialog`: título según `trigger` (redacción propia para `RESUME`), resumen,
  aplicados y descartados con su razón vía `discardReasonRes`.
- `SnackbarHost` alineado `Alignment.BottomCenter` dentro del `Box` raíz, con
  `LocalContentBottomPadding`, igual que `ClubSettingsScreen`.
- El `LaunchedEffect(Unit)` de §2.7: limpia conflicto y resultado colgados y **después** observa
  `lastSubstitutionResult`, copiando a estado local y llamando a `consumeLastSubstitutionResult()`
  antes de pintar. `presentationFor(...)` decide snackbar o diálogo.

El mensaje del snackbar se resuelve con `stringResource` en el cuerpo del composable y se dispara con
`LaunchedEffect(mensaje)`; **no** se usa `getString` suspend, para no depender de una API cuya
disponibilidad en esta versión de CMP no he verificado.

---

## Paso 7 — Verificación completa y reporte

```bash
./gradlew :shared-ui:test --rerun-tasks --no-daemon --stacktrace
./gradlew ktlintCheck --no-daemon --stacktrace
./gradlew :app:assembleDevDebug --no-daemon
./gradlew :iosApp:compileKotlinIosSimulatorArm64 --no-daemon
```

`ktlintFormat` si hace falta, y se revuelve a pasar `ktlintCheck`. `git status` / `git diff --stat`
para confirmar que todo está en disco. Reporte al coordinador **contra los ocho criterios de
aceptación del #412**, diciendo explícitamente qué queda fuera (#420, drag & drop) y qué no he
podido probar (render de composables, por falta de compose-ui-test).

**No creo la PR hasta que el coordinador lo indique. No mergeo.**

---

## Ficheros tocados — cierre de alcance

| Fichero | Acción |
|---|---|
| `shared-ui/.../ui/matches/PendingSubstitutionsPresentation.kt` | nuevo |
| `shared-ui/.../ui/matches/components/PendingSubstitutionCard.kt` | nuevo |
| `shared-ui/.../ui/matches/MatchScreen.kt` | modificado |
| `shared-ui/src/commonMain/composeResources/values/strings.xml` | modificado |
| `shared-ui/src/commonMain/composeResources/values-es/strings.xml` | modificado |
| `shared-ui/src/commonTest/.../ui/matches/PendingSubstitutionsPresentationTest.kt` | nuevo |

**Ni un fichero fuera de `shared-ui`.** Si en algún paso creyera necesitar algo del ViewModel que no
expone, paro y se lo digo al coordinador: esa decisión es suya.

---

## Riesgos

| Riesgo | Mitigación |
|---|---|
| `Res` generado no visible desde `commonTest` | Se comprueba en el paso 1, con lo mínimo escrito. Plan B en spec §4. Aviso al coordinador. |
| La sección come espacio a la lista de jugadores | `heightIn(max = 200.dp)` + scroll propio; la lista conserva su `weight(1f)`. |
| Regresión en `LIVE` al tocar §5 | Commit aislado (paso 5) + tests 2 y 3 del paso 1, que fijan que en `LIVE` las expresiones se reducen a las de hoy. |
| `:data:remote` falla con `Could not write XML test results` | Contención de Gradle con otra sesión, no un test roto: reintento en solitario. |
| Un `UP-TO-DATE` que no prueba nada | `--rerun-tasks` en las pasadas que cuentan. |
